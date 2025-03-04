package component;

import java.net.Socket;

public class ParticipantInfo {
    private final String id;
    private final String ip;
    private final int port;
    private final Socket socket;
    private boolean isOnline;

    public ParticipantInfo(String id, String ip, int port, Socket socket, boolean isOnline) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.socket = socket;
        this.isOnline = isOnline;
    }

    public String getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
