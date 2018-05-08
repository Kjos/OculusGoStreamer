package net.kajos;

/**
 * Created by kajos on 22-8-17.
 */
public class Util {
    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }
}
