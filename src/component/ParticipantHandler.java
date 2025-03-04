package component;

import util.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class ParticipantHandler implements Runnable {

    private final Socket socket;
    private final Map<String, ParticipantInfo> participants;

    public ParticipantHandler(Socket socket, Map<String, ParticipantInfo> participants) {
        this.socket = socket;
        this.participants = participants;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream());
        ) {
            System.out.println("ACK Connected to Coordinator");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.printf("Received: %s%n", message);
                this.handleCommand(message, out);
            }

        } catch (IOException e) {
            System.out.printf(
                    "Connection Error with Participant: %s%s%n",
                    Constants.surroundColor(Constants.COLOR_GREEN, this.socket.getInetAddress().toString()),
                    Constants.surroundColor(Constants.COLOR_RED, e.getMessage())
            );
        } finally {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleCommand(String message, PrintWriter out) {
        String[] parts = message.split(" ");
        if (parts.length < 2) {
            System.out.println("Invalid Command");
            return;
        }

        String command = parts[0].toUpperCase();
        switch (command) {
            case "REGISTER":
                if (parts.length < 3) {
                    out.println("ERROR REGISTER requires <id> <port>");
                    return;
                }

                String id = parts[1];
                int PORT = Integer.parseInt(parts[2]);

                if (participants.containsKey(id)) {
                    out.println("ERROR Participant ID already registered");
                } else {
                    participants.put(id, new ParticipantInfo(id, socket.getInetAddress().getHostAddress(), PORT, socket));
                    out.println("ACK Registered as " + id);
                    System.out.println("Participant Registered: " + id);
                }
                break;

            default:
                out.println("ERROR Unknown command");
        }
    }
}
