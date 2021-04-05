package chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ChatServer {
    private final AuthenticationService authenticationService;
    private final Set<ClientHandler> loggedClients;

    public ChatServer() {
        authenticationService = new AuthenticationService();
        loggedClients = new HashSet<>();

        try {
            ServerSocket socket = new ServerSocket(8080);
            System.out.println("Server is running up...");
            while (true) {
                System.out.println("Waiting for a connection...");
                Socket clientSocket = socket.accept();
                new ClientHandler(clientSocket, this);
            }
        } catch (IOException e) {
            throw new ChatServerException("Something went wrong", e);
        }
    }

    public AuthenticationService getAuthenticationService() {

        return authenticationService;
    }

    public void broadcast(String message) {
        for (ClientHandler clientHandler : loggedClients) {
            clientHandler.sendMessage(message);
        }
    }

    public void subscribe(ClientHandler clientHandler) {

        loggedClients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler) {

        loggedClients.remove(clientHandler);
    }

    public boolean isLoggedIn(String name) {

        return loggedClients.stream()
                .filter(client -> client.getName().equals(name))
                .findFirst()
                .isPresent();
    }

    public String sendPrivateMessage(ClientHandler clientHandler, String name, String msg) {
        String message = String.format("[ %s ] private: %s", clientHandler.getName(), msg);
        for (ClientHandler c : loggedClients) {
            if (c.getName().equals(name)) {
                c.sendMessage(message);
            }
        }
        return message;
    }
}
