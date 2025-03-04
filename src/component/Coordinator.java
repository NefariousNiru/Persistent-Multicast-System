package component;

import util.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Coordinator {
    private final int PORT;
    private final ConcurrentHashMap<String, Participant> participants = new ConcurrentHashMap<>();


    public Coordinator(int port) {
        this.PORT = port;
    }

    public void start() {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(this.PORT)) {
            System.out.printf("Coordinator established at Port: %s%n", Constants.surroundColor(Constants.COLOR_BLUE, String.valueOf(this.PORT)));
            while (true) {
                Socket participantSocket = serverSocket.accept();
                System.out.printf("New Participant Connected: %s%n", Constants.surroundColor(Constants.COLOR_GREEN, participantSocket.getInetAddress().toString()));

                // Handle participant in a separate Thread from a ThreadPool
                threadPool.execute(new ParticipantHandler(participantSocket, participants));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}
