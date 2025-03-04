package component;

import util.Constants;
import util.Methods;
import util.ParticipantHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ParticipantHandler implements Runnable {

    private final Socket socket;
    private final Map<String, Participant> participants;
    private final Map<String, Queue<Message>> pendingMessages;
    private String userId;
    private ParticipantHelper helper;

    public ParticipantHandler(Socket socket, Map<String, Participant> participants, Map<String, Queue<Message>> pendingMessages) {
        this.socket = socket;
        this.participants = participants;
        this.pendingMessages = pendingMessages;
        this.userId = null;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        ) {
            helper = new ParticipantHelper(socket, out);
            helper.sendAck("Connected to Coordinator");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.printf("Received: %s%n", message);
                this.handleCommand(message);
            }

        } catch (IOException e) {
            helper.logConnectionError(e, userId);
        } finally {
            helper.closeSocket();
        }
    }

    private void handleCommand(String message) {
        String[] parts = message.split(" ", 2);
        String command = parts[0].toUpperCase();
        String content = (parts.length > 1) ? parts[1].trim() : "";

        Methods method = Methods.fromString(command);

        if (method == null) {
            helper.sendError("Unknown command");
            return;
        }

        switch (method) {
            case REGISTER -> register(content);
            case DEREGISTER -> deregister();
            case DISCONNECT -> disconnect();
            case RECONNECT-> reconnect();
            case MSEND -> sendMessage(content);
        }
    }

    private void register(String id) {
        if (userId != null) {
            helper.sendError("Already registered as " + userId);
            return;
        }

        if (participants.containsKey(id)) {
            helper.sendError("Participant ID already registered");
        } else {
            this.userId = id;
            participants.put(id, new Participant(id, socket.getInetAddress().getHostAddress(), socket, true));
            helper.sendAck("Registered as " + id);
            System.out.println("Participant Registered: " + id);
        }
    }

    private void deregister() {
        if (helper.isNotRegistered(userId)) return;

        participants.remove(userId);
        helper.sendAck("Deregistered " + userId);
        System.out.println("Participant Deregistered: " + userId);
    }

    private void disconnect() {
        if (helper.isNotRegistered(userId)) return;

        participants.get(userId).setOnline(false);
        participants.get(userId).setLastDisconnectedTime(System.currentTimeMillis());
        helper.sendAck("Disconnected");
        System.out.println("Participant Disconnected: " + userId);
    }

    private void reconnect() {
        if (helper.isNotRegistered(userId)) return;

        Participant participant = participants.get(userId);
        if (participant == null || participant.getSocket() == null) {
            helper.sendError("Cannot reconnect, please REGISTER again.");
            return;
        }

        long offlineTime = (System.currentTimeMillis() - participant.getLastDisconnectedTime()) / 1000;
        if (offlineTime > Constants.TEMPORAL_BOUND_SECONDS) {
            helper.sendError("Reconnection time exceeded. Please REGISTER again.");
            participants.remove(userId);
            System.out.println("Participant " + userId + " removed due to expired reconnection.");
        } else {
            participant.setOnline(true);
            participant.setSocket(socket);
            helper.sendAck("Reconnected");
            System.out.println("Participant Reconnected: " + userId);
            deliverPendingMessages(participant);
        }
    }

    private void sendMessage(String content) {
        if (helper.isNotRegistered(userId)) return;
        if (content.isEmpty()) {
            helper.sendError("Message cannot be empty");
            return;
        }

        Message msg = new Message(userId, content);
        broadcastMessage(msg);
        helper.sendAck("Message Sent");
    }

    private void broadcastMessage(Message msg) {
        for (Map.Entry<String, Participant> entry : participants.entrySet()) {
            Participant participant = entry.getValue();

            if (participant.isOnline()) {
                try {
                    PrintWriter participantOut = new PrintWriter(participant.getSocket().getOutputStream(), true);
                    String ack = Constants.surroundColor(Constants.COLOR_BLUE, "MSG " + userId +": ");
                    participantOut.println(ack + msg.getContent());
                } catch (IOException e) {
                    helper.logError("Error sending message to " + participant.getId(), e);
                }
            } else {
                pendingMessages.putIfAbsent(participant.getId(), new ConcurrentLinkedQueue<>());
                pendingMessages.get(participant.getId()).add(msg);
            }
        }
    }

    private void deliverPendingMessages(Participant participant) {
        Queue<Message> messages = pendingMessages.get(userId);
        if (messages == null || messages.isEmpty()) return;

        try (PrintWriter participantOut = new PrintWriter(participant.getSocket().getOutputStream(), true)) {
            long now = System.currentTimeMillis();
            while (!messages.isEmpty()) {
                Message msg = messages.peek();
                // Remove expired or null messages
                if (msg == null || (now - msg.getTimestamp()) / 1000 > Constants.TEMPORAL_BOUND_SECONDS) {
                    messages.poll();
                    continue;
                }
                String message = Constants.surroundColor(Constants.COLOR_BLUE, "MSG " + userId +": ") + msg.getContent();
                participantOut.println(message);
                messages.poll(); // Remove after sending
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        pendingMessages.remove(userId);
    }
}
