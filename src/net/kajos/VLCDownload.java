package net.kajos;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class VLCDownload {

    public static void main(String[] args) {
        run();
    }

    private static boolean download(String url, Path path) {
        try {
            System.out.println("Downloading new VLC copy..");
            URL website = new URL(url);
            InputStream in = website.openStream();
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            in.close();
            System.out.println("Download completed.");
            return true;
        } catch (IOException e) {
            System.out.println("Couldn't download VLC.");
            return false;
        }
    }

    public static final String VLC = "2.2.6";
    public static final String VLC_DIR = "vlc-override";

    private static final String MAC = "http://download.videolan.org/pub/videolan/vlc/2.2.6/macosx/";
    private static final String LINUX = "http://download.videolan.org/pub/videolan/vlc/" +
            VLC + "/vlc-" + VLC + ".tar.xz";
    private static final String WINDOWS_32 = "http://download.videolan.org/pub/videolan/vlc/" +
            VLC + "/win32/vlc-" + VLC + "-win32.zip";
    private static final String WINDOWS_64 = "http://download.videolan.org/pub/videolan/vlc/" +
            VLC + "/win64/vlc-" + VLC + "-win64.zip";
    public static void run() {
        String downloadUrl = null;
        String version = System.getProperty("java.version");
        System.out.println("Java version: " + version);
        System.out.println("Note: Required 1.6 or higher.");

        String arch = System.getProperty("os.arch");
        System.out.println("Java architecture: " + arch);
        System.out.println();

        boolean archIs64 = arch.contains("64");
        if (archIs64) {
            System.out.println("Note: Need VLC 64 bit version for this JRE.");
        } else {
            System.out.println("Note: Need VLC 32 bit version for this JRE.");
        }
        System.out.println();

        File zipFile = new File("vlc-compressed.zip");
        if (!zipFile.exists()) {

            if (Util.isMac() || Util.isLinux()) {
                System.out.println("Note: Make sure VLC matches architecture type (32/64 bit)!");
                System.out.println();

                String vlcVersion = executeBashCommand("file $(which vlc)");
                System.out.println("VLC version info:");
                System.out.println(vlcVersion);
                System.out.println("Note: VLC version required >2.1");
                System.out.println();

                if ((vlcVersion.contains("64") && archIs64) ||
                        (!vlcVersion.contains("64") && !archIs64)) {
                    System.out.println("Seems VLC and Java architecture already match.");
                } else {
                    System.out.println("Seems VLC and Java architecture don't match!");
                    System.out.println("Continue on your own peril.");
                }
                if (archIs64) {
                    System.out.println("Download and install 64-bit VLC from here:");
                } else {
                    System.out.println("Download and install 32-bit VLC from here:");
                }
                System.out.println(Util.isMac() ? MAC : LINUX);
                System.out.println("or try a " + (archIs64 ? "32" : "64") + "bit JRE.");
                return;

            } else if (Util.isWindows()) {
                if (archIs64) {
                    downloadUrl = WINDOWS_64;
                } else {
                    downloadUrl = WINDOWS_32;
                }
            } else {
                System.out.println("Unknown system! Don't know what VLC to download.");
                return;
            }

            if (!download(downloadUrl, zipFile.toPath())) return;
        }

        System.out.println("Uncompressing VLC zip file..");
        try{
            extractFolder(zipFile);
            System.out.println("Done uncompressing..");
            zipFile.delete();
            System.out.println("Deleted zip.");
        }catch (Exception e) {
            System.out.println("Couldn't uncompress VLC.");
            e.printStackTrace();
            System.exit(1);
        }

        File extractedDir = new File("vlc-" + VLC);
        if (extractedDir.exists()) {
            extractedDir.renameTo(new File(VLC_DIR));
        }
    }

    private static void extractFolder(File file) throws IOException
    {
        int BUFFER = 2048;

        ZipFile zip = new ZipFile(file);
        String newPath = "./";

        new File(newPath).mkdir();
        Enumeration zipFileEntries = zip.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements())
        {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(newPath, currentEntry);
            //destFile = new File(newPath, destFile.getName());
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            destinationParent.mkdirs();

            if (!entry.isDirectory())
            {
                BufferedInputStream is = new BufferedInputStream(zip
                        .getInputStream(entry));
                int currentByte;
                // establish buffer for writing file
                byte data[] = new byte[BUFFER];

                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos,
                        BUFFER);

                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }
        }
    }

    public static String executeBashCommand(String command) {
        Runtime r = Runtime.getRuntime();
        // Use bash -c so we can handle things like multi commands separated by ; and
        // things like quotes, $, |, and \. My tests show that command comes as
        // one argument to bash, so we do not need to quote it to make it one thing.
        // Also, exec may object if it does not have an executable file as the first thing,
        // so having bash here makes it happy provided bash is installed and in path.
        String[] commands = {"bash", "-c", command};
        String output = "";
        try {
            Process p = r.exec(commands);

            p.waitFor();
            BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";

            while ((line = b.readLine()) != null) {
                output += line;
            }

            b.close();
        } catch (Exception e) {
            System.err.println("Failed to execute bash with command: " + command);
            e.printStackTrace();
        }
        return output;
    }
}
