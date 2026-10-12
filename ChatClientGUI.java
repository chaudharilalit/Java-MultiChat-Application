package client;

import javax.swing.*;
import java.awt.*;
//import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClientGUI extends JFrame {

    private JTextArea chatArea = new JTextArea();
    private JTextField messageField = new JTextField();
    private JButton sendButton = new JButton("Send");

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ChatClientGUI() {
        setTitle("Client Chat");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        chatArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(chatArea);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(scroll, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.setEnabled(false);
        messageField.setEnabled(false);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        new Thread(this::startClient).start();
    }

    private void startClient() {
        try {
            socket = new Socket("localhost", 12345);
            appendMessage("Connected to server");

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            SwingUtilities.invokeLater(() -> {
                sendButton.setEnabled(true);
                messageField.setEnabled(true);
                messageField.requestFocus();
            });

            String line;
            while ((line = reader.readLine()) != null) {
                appendMessage("Server: " + line);
            }

        } catch (IOException e) {
            appendMessage("Client error: " + e.getMessage());
        } finally {
            closeConnections();
        }
    }

    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty()) {
            writer.println(msg);
            appendMessage("Client: " + msg);
            messageField.setText("");
        }
    }

    private void appendMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(msg + "\n");
        });
    }

    private void closeConnections() {
        try {
            if (reader != null)
                reader.close();
            if (writer != null)
                writer.close();
            if (socket != null)
                socket.close();
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatClientGUI clientGUI = new ChatClientGUI();
            clientGUI.setVisible(true);
        });
    }
}
