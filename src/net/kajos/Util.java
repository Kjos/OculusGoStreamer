package net.kajos;

/**
 * Created by kajos on 22-8-17.
 */
public class Util {
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static boolean isMac() {
        String os = System.getProperty("os.name");
        return (os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0);
    }

    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("nux");
    }
}
