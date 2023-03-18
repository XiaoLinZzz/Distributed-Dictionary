import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Arrays;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private HashMap<String, List<String>> dictionary;
    private BufferedReader in;
    private PrintWriter out;
    private String threadName;
    private int clientNumber;

    public ClientHandler(Socket clientSocket, HashMap<String, List<String>> dictionary, String threadName, int clientNumber) {
        this.clientSocket = clientSocket;
        this.dictionary = dictionary;
        this.threadName = threadName;
        this.clientNumber = clientNumber;
    }

    @Override
    public void run() {
        try {
            
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String[] command = inputLine.split(" ", 2);
                System.out.println(threadName + ": Processing command: " + Arrays.toString(command));

                // Check if the client wants to exit
                if (command.length > 0) {
                    if (command[0].equalsIgnoreCase("exit")) {
                        break;
                    }
                }

                if (command.length < 2) {
                    out.println("Invalid command");
                    continue;
                }

                String action = command[0].toLowerCase();
                String word = command[1];

                switch (action) {
                    case "add":
                        addWord(word);
                        break;
                    case "delete":
                        deleteWord(word);
                        break;
                    case "search":
                        searchWord(word);
                        break;
                    case "update":
                        updateWord(word);
                        break;
                    default:
                        out.println("Invalid command");
                }
            }
        } catch (IOException e) {
            System.err.println("Error communicating with client: " + e.getMessage());
        } finally {
            System.out.println("Thread" + clientNumber + ": Thread exiting...");
            System.out.println("Thread" + clientNumber + " Successfully closed connection with client");
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    private void addWord(String input) {
        String[] parts = input.split(" ", 2);
        if (parts.length < 2) {
            out.println("Invalid command (you need to provide a word and a meaning)");
            return;
        }

        String word = parts[0];
        String meaning = parts[1];

        if (dictionary.containsKey(word)) {
            // List<String> meanings = dictionary.get(word);
            // meanings.add(meaning);
            // dictionary.put(word, meanings);
            out.println("The word already exists");
        } else {
            List<String> meanings = new ArrayList<>();
            meanings.add(meaning);
            dictionary.put(word, meanings);
            out.println("Word and meaning added");
        }
    }

    private void deleteWord(String word) {
        if (dictionary.containsKey(word)) {
            dictionary.remove(word);
            out.println("Word deleted");
        } else {
            out.println("Word not found");
        }
    }

    private void searchWord(String word) {
        if (dictionary.containsKey(word)) {
            List<String> meanings = dictionary.get(word);
            for (String meaning : meanings) {
                out.println(meaning);
            }
        } else {
            out.println("Word not found");
        }
    }

    private void updateWord(String input) {
        String[] parts = input.split(" ", 2);
        if (parts.length < 2) {
            out.println("Invalid command");
            return;
        }

        String word = parts[0];
        String newMeaning = parts[1];

        if (dictionary.containsKey(word)) {
            List<String> meanings = dictionary.get(word);
            meanings.clear();
            meanings.add(newMeaning);
            dictionary.put(word, meanings);
            out.println("Word updated");
        } else {
            out.println("Word not found");
        }
    }
}
