package net.kajos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Created by kajos on 22-8-17.
 */
public class Util {
    public static boolean isAlphaNumeric(String value) {
        if (value == null || value.isEmpty()) return false;

        Pattern p = Pattern.compile("[^a-zA-Z0-9]");
        return !p.matcher(value).find();
    }

    public static boolean isNumeric(String value) {
        if (value == null || value.isEmpty()) return false;

        Pattern p = Pattern.compile("[^\\-0-9]");
        return !p.matcher(value).find();
    }
}
