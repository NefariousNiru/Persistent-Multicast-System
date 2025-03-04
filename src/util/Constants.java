package util;

public class Constants {
    public static final String COLOR_RED = "\u001B[31m";
    public static final String COLOR_GREEN = "\u001B[32m";
    public static final String COLOR_BLUE = "\u001B[34m";
    public static final String COLOR_RESET = "\u001B[0m";
    public static final int PORT = 9000;
    public static final int THREAD_POOL_SIZE = (2 * Runtime.getRuntime().availableProcessors()) + 4;

    public static String surroundColor(String color, String message) {
        return color + message + COLOR_RESET;
    }
}
