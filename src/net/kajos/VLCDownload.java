package net.kajos;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
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
            e.printStackTrace();
            return false;
        }
    }

    private static final String WINDOWS_32 = "http://download.videolan.org/pub/videolan/vlc/3.0.1/win32/vlc-3.0.1-win32.zip";
    private static final String WINDOWS_64 = "http://download.videolan.org/pub/videolan/vlc/3.0.1/win64/vlc-3.0.1-win64.zip";
    public static void run() {
        File zipFile = new File("vlc-compressed.zip");
        if (!zipFile.exists()) {
            String arch = System.getProperty("os.arch");
            if (arch.contains("64")) {
                System.out.println("Need 64 bit version.");
                if (!download(WINDOWS_64, zipFile.toPath())) return;
            } else {
                System.out.println("Need 32 bit version.");
                if (!download(WINDOWS_32, zipFile.toPath())) return;
            }
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

}
