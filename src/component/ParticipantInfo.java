package component;

import java.net.Socket;

public record ParticipantInfo(String id, String ip, int port, Socket socket) {}
