package chat.client.gui;

import chat.client.gui.api.Receiver;
import chat.client.gui.api.Sender;

import javax.swing.*;
import java.awt.*;

public class ChatFrame extends Frame {
    private final JTextArea chatArea;

    public ChatFrame(Sender sender) {
        setTitle("Chat v1.0");
        setBounds(0, 0, 400, 500);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        top.add(chatArea, BorderLayout.CENTER);


        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout());

        JTextField inputField = new JTextField();
        bottom.add(inputField, BorderLayout.CENTER);
        JButton submitBtn = new JButton("Submit");
        submitBtn.addActionListener(new SubmitButtonListener(inputField, sender));
        bottom.add(submitBtn, BorderLayout.EAST);

        add(top, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void setDefaultCloseOperation(int exitOnClose) {
    }

    public Receiver getReceiver() {
        return (message) -> {
            if (!message.isBlank()) {
                chatArea.append(message);
                chatArea.append("\n");
            }
        };
    }
}
