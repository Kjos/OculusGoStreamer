package net.kajos;

import net.kajos.Handlers.FilterHandler;
import net.kajos.Manager.Manager;
import net.kajos.Handlers.ControlsHandler;
import org.webbitserver.*;
import org.webbitserver.handler.StaticFileHandler;

import java.awt.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.Executors;

/**
 * Created by kajos on 6-8-17.
 */
public class Server {
    private WebServer webServer;
    private ControlsHandler controlsHandler;

    private Manager manager;
    private ScreenRecorder recorder;

    public Server() {
    }

    public void resize(int width, int height) {
        if (width != recorder.videoWidth || height != recorder.videoHeight) {
            recorder.stop();
            recorder.start(width, height);
        }
    }

    public void start() throws InterruptedException, AWTException {
        manager = new Manager();

        controlsHandler = new ControlsHandler(this, manager);

        webServer = WebServers.createWebServer(Executors.newFixedThreadPool(Constants.THREADS), Config.WEB_PORT);
        webServer.add(new FilterHandler());
        webServer.add("/control", controlsHandler);
        webServer.add("/websocket", manager);
        webServer.add(new StaticFileHandler("website/"));
        webServer.start();

        System.out.println("OculusGo DesktopStreamer v1 by Kaj Toet");
        System.out.println("------------------------------------------------");

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
