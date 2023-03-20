import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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

        String address = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try {
            socket = new Socket(address, portNumber);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            showErrorDialog("Error connecting to server: " + e.getMessage());
            System.exit(1);
        } catch (UnknownError e) {
            showErrorDialog("Error connecting to server: Unknown host: " + e.getMessage());
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> Client_GUI());
    }

    private static void showErrorDialog(String errorMessage) {
        JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void Client_GUI() {
        // Frame
        JFrame frame = new JFrame("My Dictionary");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame.add(mainPanel);

        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel("Dictionary Client");
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

        JLabel inputLabel = new JLabel("Enter Command (add, delete, search, update):");
        inputPanel.add(inputLabel);

        JTextField commandArea = new JTextField();
        commandArea.setPreferredSize(new Dimension(400, 30));
        inputPanel.add(commandArea);

        JButton sendCommandButton = new JButton("Send Command");
        inputPanel.add(sendCommandButton);

        JButton clearButton = new JButton("Clear Response");
        inputPanel.add(clearButton);

        sendCommandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = commandArea.getText().trim();
                if (!command.isEmpty()) {
                    out.println(command);
                    try {
                        String response;
                        while ((response = in.readLine()) != null) {
                            if (response.equals("END")) {
                                break;
                            }
                            textArea.append("Server response: " + response + "\n");
                        }
                    } catch (IOException ex) {
                        showErrorDialog("Error reading response from server: " + ex.getMessage());
                    }
                }
                commandArea.setText("");
            }
        });
    
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
            }
        });
    
        frame.setVisible(true);
    }
}
    
