package net.kajos;

import com.sun.jna.NativeLibrary;
import net.kajos.Handlers.FilterHandler;
import net.kajos.Manager.Manager;
import net.kajos.Handlers.ControlsHandler;
import net.kajos.Manager.Viewer;
import org.webbitserver.*;
import org.webbitserver.handler.StaticFileHandler;

import java.awt.*;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.Executors;

/**
 * Created by kajos on 6-8-17.
 */
public class Server {
    private Config mem;

    private WebServer webServer;
    private ControlsHandler controlsHandler;

    private Manager manager;
    private ScreenRecorder recorder;

    public Server() {
    }

    public void resize(int clientWidth, int clientHeight) {
        mem = Config.load();

        float displayAspect = (float)Config.get().get().SCREEN_WIDTH /
                (float)Config.get().SCREEN_HEIGHT;
        float clientAspect = (float)clientWidth / (float)clientHeight;
        float diffAspect = displayAspect / clientAspect;
        if (diffAspect < 1f) {
            clientWidth = (int)((float)clientWidth * diffAspect);
        } else {
            clientHeight = (int)((float)clientHeight / diffAspect);
        }

        if (clientWidth != recorder.videoWidth || clientHeight != recorder.videoHeight) {
            recorder.stop();
            manager.getViewer().reset();
            recorder.start(clientWidth, clientHeight);
        }
    }

    private static void webDirectoryCheck() {
        File dir = new File("website");
        if (!dir.exists()) {
            System.out.println("Webserver contents are missing!");
            System.out.println("Place the 'website' directory next to the JAR executable.");
            System.exit(1);
        }
    }

    private static void javaVersionCheck() {
        String version = System.getProperty("java.version");
        System.out.println("Java version: " + version);
        System.out.println("Note: Required 1.6 or higher.");

        String arch = System.getProperty("os.arch");
        System.out.println("Architecture: " + arch);
        System.out.println();

        System.out.println("Note: Make sure VLC matches architecture type (32/64)!");
        System.out.println("Note: VLC version required >2.1");
        System.out.println();
    }

    private static void windowsHelper() {
        if (!Util.isWindows()) {
            return;
        }

        String dirname = "vlc-3.0.1";
        File folder = new File(dirname);
        if (folder.exists()) {
            System.out.println("VLC already downloaded.");
        } else {
            VLCDownload.run();
        }

        NativeLibrary.addSearchPath("libvlc", dirname);
    }

    public void start() throws InterruptedException, AWTException {
        System.out.println("------------------------------------------------");
        System.out.println("OculusGo DesktopStreamer beta by Kaj Toet");
        System.out.println("------------------------------------------------");

        javaVersionCheck();
        webDirectoryCheck();
        windowsHelper();

        mem = Config.load();
        manager = new Manager();

        controlsHandler = new ControlsHandler(this, manager);

        webServer = WebServers.createWebServer(Executors.newFixedThreadPool(Constants.THREADS), Config.get().WEB_PORT);
        webServer.add(new FilterHandler());
        webServer.add("/control", controlsHandler);
        webServer.add("/websocket", manager);
        webServer.add(new StaticFileHandler("website/"));
        webServer.start();

        try {
            System.out.print("Address: " + getLocalHostLANAddress().toString());
            System.out.println(":" + webServer.getPort());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        recorder = new ScreenRecorder(manager);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                recorder.stop();
            }
        });

        while(true) Thread.sleep(1000);
    }

    private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // Iterate all IP addresses assigned to each card...
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {

                        if (inetAddr.isSiteLocalAddress()) {
                            // Found non-loopback site-local address. Return it immediately...
                            return inetAddr;
                        }
                        else if (candidateAddress == null) {
                            // Found non-loopback address, but not necessarily site-local.
                            // Store it as a candidate to be returned if site-local address is not subsequently found...
                            candidateAddress = inetAddr;
                            // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                            // only the first. For subsequent iterations, candidate will be non-null.
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                // We did not find a site-local address, but we found some other non-loopback address.
                // Server might have a non-site-local address assigned to its NIC (or it might be running
                // IPv6 which deprecates the "site-local" concept).
                // Return this non-loopback candidate address...
                return candidateAddress;
            }
            // At this point, we did not find a non-loopback address.
            // Fall back to returning whatever InetAddress.getLocalHost() returns...
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        }
        catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }
}
