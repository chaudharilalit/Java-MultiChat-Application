
import javax.swing.*;
import java.awt.*;
//import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatServerGUI extends JFrame {

    private JTextArea chatArea = new JTextArea();
    private JTextField messageField = new JTextField();
    private JButton sendButton = new JButton("Send");

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ChatServerGUI() {
        setTitle("Server Chat");
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

        new Thread(this::startServer).start();
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(12345);
            appendMessage("Server started. Waiting for client...");
            clientSocket = serverSocket.accept();
            appendMessage("Client connected: " + clientSocket);

            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);

            SwingUtilities.invokeLater(() -> {
                sendButton.setEnabled(true);
                messageField.setEnabled(true);
                messageField.requestFocus();
            });

            String line;
            while ((line = reader.readLine()) != null) {
                appendMessage("Client: " + line);
            }

        } catch (IOException e) {
            appendMessage("Server error: " + e.getMessage());
        } finally {
            closeConnections();
        }
    }

    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty()) {
            writer.println(msg);
            appendMessage("Server: " + msg);
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
            if (clientSocket != null)
                clientSocket.close();
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatServerGUI serverGUI = new ChatServerGUI();
            serverGUI.setVisible(true);
        });
    }
}
