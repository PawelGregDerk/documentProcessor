package by.vstu.isit.documentprocessor.utils;

public class FileUtils {

    public static String sanitize(String name) {
        if (name == null) return null;
        return name.replaceAll("[/\\\\:*?\"<>|]", "_");
    }
}
