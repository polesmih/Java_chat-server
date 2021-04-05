package chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Optional;

public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ChatServer chatServer;
    private String name;

    public ClientHandler(Socket socket, ChatServer chatServer) {
        this.socket = socket;
        this.chatServer = chatServer;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new ChatServerException("Something went wrong during client establishing.", e);
        }

        new Thread(() -> {
            // start of code change
            try {
                socket.setSoTimeout(120000);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            try {
                doAuthentication();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // finish of code change

            listen();
        })
                .start();
    }


    public String getName() {
        return name;
    }

    private void listen() {
        receiveMessage();
    }


    private void doAuthentication() throws IOException {
        sendMessage("Welcome! \nPlease do authentication - use command: auth your_login your_pass.");
        while (true) {
            try {

                String message = in.readUTF();

                if (message.startsWith("auth")) {
                    String[] credentialsStruct = message.split("\\s");
                    String login = credentialsStruct[1];
                    String password = credentialsStruct[2];

                    Optional<AuthenticationService.Entry> mayBeCredentials = chatServer.getAuthenticationService()
                            .findEntryByCredentials(login, password);

                    if (mayBeCredentials.isPresent()) {
                        AuthenticationService.Entry credentials = mayBeCredentials.get();
                        if (!chatServer.isLoggedIn(credentials.getName())) {
                            name = credentials.getName();
                            chatServer.broadcast(String.format("User[%s] entered the chat", name));
                            chatServer.subscribe(this);

                            // start of code change
                            socket.setSoTimeout(0);
                            // finish of code change

                            return;

                        } else {
                            sendMessage(String.format("User with name %s is already logged in", credentials.getName()));
                        }
                    } else {
                        sendMessage("Incorrect login or password.");
                    }
                } else {
                    sendMessage("Incorrect authentication message. " +
                            "Please use valid command: -auth your_login your_pass");
                }

                // start of code change
            } catch (SocketTimeoutException e) {
                sendMessage("Your login time has expired. Connected is broken.");
                socket.close();
                break;
                // finish of code change

            } catch (IOException e) {
                throw new ChatServerException("Something went wrong during client authentication.", e);
            }
    }
    }

    public void receiveMessage() {
        while (true) {
            try {
                String message = in.readUTF();

                if (message.startsWith("/w")) {
                    String[] split = message.split("\\s", 3);
                    chatServer.sendPrivateMessage(this, split[1], split[2]);
                    sendMessage("to [ " + split[1] + " ]: " + split[2]);
                } else {
                    chatServer.broadcast(String.format("%s: %s", name, message));
                }

            } catch (IOException e) {
                throw new ChatServerException("Something went wrong during receiving the message.", e);
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            throw new ChatServerException("Something went wrong during sending the message.", e);
        }
    }
}
