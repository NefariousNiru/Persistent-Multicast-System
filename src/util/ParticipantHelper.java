package util;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ParticipantHelper {

    private Socket sock;
    private PrintWriter out;

    public ParticipantHelper(Socket socket, PrintWriter out) {
        this.sock = socket;
        this.out = out;
    }

    public boolean isNotRegistered(String userId) {
        if (userId == null) {
            sendError("You must REGISTER first");
            return true;
        }
        return false;
    }

    /** Helper: Send ACK */
    public void sendAck(String message) {
        String ack = Constants.surroundColor(Constants.COLOR_GREEN, "ACK: " + message);
        this.out.println(ack);
    }

    /** Helper: Send Message */
    public void sendMessage(String message, String userId) {

    }

    /** Helper: Send Error */
    public void sendError(String message) {
        String error = Constants.surroundColor(Constants.COLOR_RED, "ERROR: " + message);
        this.out.println(error);
    }

    /** Helper: Log errors */
    public void logError(String msg, Exception e) {
        System.out.printf("❌ %s: %s%n", msg, e.getMessage());
    }

    /** Helper: Close socket */
    public void closeSocket() {
        try {
            this.sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logConnectionError(Exception e, String userId) {
        System.out.printf(
                "Connection Error with Participant: %s%s%sn",
                Constants.surroundColor(Constants.COLOR_GREEN, this.sock.getInetAddress().toString()),
                " UserID: " + Constants.surroundColor(Constants.COLOR_GREEN, userId),
                Constants.surroundColor(Constants.COLOR_RED, e.getMessage())
        );
    }
}
