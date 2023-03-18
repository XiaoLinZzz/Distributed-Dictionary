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
                // print the command received from the client
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
                        out.println("END");
                        break;
                    case "delete":
                        deleteWord(word);
                        out.println("END");
                        break;
                    case "search":
                        searchWord(word);
                        break;
                    case "update":
                        updateWord(word);
                        out.println("END");
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

    // Query the meaning(s) of a given word
    private void searchWord(String word) {
        if (dictionary.containsKey(word)) {
            List<String> meanings = dictionary.get(word);
            out.println(word + " has " + meanings.size() + " meanings");
            int i = 1;
            for (String meaning : meanings) {
                out.println(i + ". " + meaning);
                i++;
            }
            out.println("END");
        } else {
            out.println("Word not found");
            out.println("END");
        }
    }

    // Add a new word
    private void addWord(String input) {
        String[] word_meaning = input.split(" ", 2);
        if (word_meaning.length < 2) {
            out.println("Invalid command (you need to provide a word and a meaning)");
            return;
        }

        String word = word_meaning[0];
        String meaning = word_meaning[1];

        if (dictionary.containsKey(word)) {
            List<String> meanings = dictionary.get(word);
            meanings.add(meaning);
            dictionary.put(word, meanings);
            out.println("The word already exists, success in adding a new meaning");
        } else {
            List<String> meanings = new ArrayList<>();
            meanings.add(meaning);
            dictionary.put(word, meanings);
            out.println("Word and meaning added");
        }
    }

    // Remove an existing word
    private void deleteWord(String word) {
        if (dictionary.containsKey(word)) {
            dictionary.remove(word);
            out.println("Word deleted");
        } else {
            out.println("Word not found");
        }
    }

    // Update meaning of an existing word
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
