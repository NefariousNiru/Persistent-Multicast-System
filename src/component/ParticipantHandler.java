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
        String id = parts[1];

        switch (command) {
            case "REGISTER":
                this.register(parts, id);
                break;
            case "DEREGISTER":
                this.deregister(id);
                break;
            case "DISCONNECT":
                this.disconnect(id);
                break;
            case "RECONNECT":
                break;
            case "MSEND":
                break;
            default:
                out.println("ERROR Unknown command");
        }
    }

    private void register(String[] parts, String id) {
        if (parts.length < 3) {
            System.out.println("ERROR REGISTER requires <id> <port>");
            return;
        }

        int PORT = Integer.parseInt(parts[2]);

        if (participants.containsKey(id)) {
            System.out.println("ERROR Participant ID already registered");
        } else {
            participants.put(id, new ParticipantInfo(id, socket.getInetAddress().getHostAddress(), PORT, socket, true));
            System.out.println("ACK Registered as " + id);
            System.out.println("Participant Registered: " + id);
        }
    }

    private void deregister(String id) {
        if (!participants.containsKey(id)) {
            System.out.println("Particpant ID not found");
        } else {
            participants.remove(id);
            System.out.println("ACK Deregistered " + id);
            System.out.println("Participant Deregistered: " + id);
        }
    }

    private void disconnect(String id) {
        if (!participants.containsKey(id)) {
            System.out.println("ERROR Participant ID not found");
        } else {
            participants.get(id).setOnline(false);
            System.out.println("ACK Disconnected " + id);
            System.out.println("Participant Disconnected: " + id);
        }
    }
}
