import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DictionaryClient {
    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java DictionaryClient <server-address> <port_number>");
            System.exit(1);
        }

        // test
        // String address = "localhost";
        // int portNumber = 8080;

        String address = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try {
            socket = new Socket(address, portNumber);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> Client_GUI());
    }


    private static void Client_GUI() {
        // Frame
        JFrame frame = new JFrame("My Dictionary");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 600);

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame.add(mainPanel);

        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel("My Dictionary");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);

        // Output area
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(outputPanel, BorderLayout.CENTER);

        JLabel outputLabel = new JLabel("Server Response:");
        outputPanel.add(outputLabel, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(textArea);
        outputPanel.add(scrollPane, BorderLayout.CENTER);

        // Input area
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        JLabel inputLabel = new JLabel("Enter Command (add, delete, search, update or exit):");
        inputPanel.add(inputLabel);

        JTextField commandArea = new JTextField();
        commandArea.setPreferredSize(new Dimension(300, 30));
        commandArea.addKeyListener(new KeyAdapter() {
            
            // allow user to press enter to send command
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendCommand(commandArea, textArea);
                }
            }
        });
        inputPanel.add(commandArea);

        JButton sendCommandButton = new JButton("Send Command");
        inputPanel.add(sendCommandButton);

        JButton clearButton = new JButton("Clear Response");
        inputPanel.add(clearButton);

        sendCommandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendCommand(commandArea, textArea);
            }
        });
        
        // clear response button
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
            }
        });
    
        frame.setVisible(true);
    }

    private static void sendCommand(JTextField commandArea, JTextArea textArea) {
        String command = commandArea.getText().trim();
        if (!command.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            textArea.append("[" + timestamp + "] " + "User input: " + command + "\n");
            out.println(command);
            try {
                String response;
                if (command.equals("exit")) {
                    socket.close();
                    System.exit(0);
                }

                while ((response = in.readLine()) != null) {
                    if (response.equals("END")) {
                        textArea.append("\n");
                        break;
                    }
                    textArea.append("Server response --> " + response + "\n");
                }

                if (response == null) {
                    textArea.append("Error: Server may have been closed unexpectedly.\n");
                    socket.close();
                    // System.exit(1); // Close the client
                }

            } catch (SocketException ex) {
                textArea.append("Error: Server may have been closed unexpectedly.\n");
                ex.printStackTrace();
                // System.exit(0);
            } catch (IOException ex) {
                System.err.println("Error reading response from server: " + ex.getMessage());
                System.exit(0);
            }
        }
        commandArea.setText("");
    }
}