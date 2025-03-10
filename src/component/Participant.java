package component;

import java.net.Socket;

public class Participant {
    private final String id;
    private final String ip;
    private Socket socket;
    private boolean isOnline;
    private long lastDisconnectedTime;

    public Participant(String id, String ip, Socket socket, boolean isOnline) {
        this.id = id;
        this.ip = ip;
        this.socket = socket;
        this.isOnline = isOnline;
    }

    public String getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public long getLastDisconnectedTime() {
        return lastDisconnectedTime;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public void setLastDisconnectedTime(long lastDisconnectedTime) {
        this.lastDisconnectedTime = lastDisconnectedTime;
    }
}
