package util;

public enum Methods {
    REGISTER,
    DEREGISTER,
    RECONNECT,
    DISCONNECT,
    MSEND;

    public static Methods fromString(String str) {
        try {
            return Methods.valueOf(str.toUpperCase()); // Converts input to Enum
        } catch (IllegalArgumentException e) {
            return null; // Handle unknown commands
        }
    }
}
