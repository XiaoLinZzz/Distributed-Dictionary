import java.io.*;
import java.net.*;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

public class DictionaryServer {
    private static final AtomicInteger clientCount = new AtomicInteger(0);
    private static ConcurrentHashMap<String, List<String>> dictionary = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java DictionaryServer <port_number> <dictionary_file>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        String dictionaryFilePath = args[1];

        dictionary = loadDictionary(dictionaryFilePath);

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Dictionary Server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                String threadName = "ClientHandler-" + clientCount.incrementAndGet();
                System.out.println(threadName + ": Connection established");

                ClientHandler clientHandler = new ClientHandler(clientSocket, dictionary, threadName);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    private static ConcurrentHashMap<String, List<String>> loadDictionary(String filePath) {
        ConcurrentHashMap<String, List<String>> dictionary = new ConcurrentHashMap<>();

        try {
            String jsonString = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject jsonObject = new JSONObject(jsonString);

            for (String key : jsonObject.keySet()) {
                JSONArray meaningsArray = jsonObject.getJSONArray(key);
                List<String> meanings = new ArrayList<>();

                for (int i = 0; i < meaningsArray.length(); i++) {
                    meanings.add(meaningsArray.getString(i));
                }

                dictionary.put(key, meanings);
            }
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
        }

        return dictionary;
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private int clientId;
        private ConcurrentHashMap<String, List<String>> dictionary;
        private BufferedReader in;
        private PrintWriter out;
        private String sucess_status = "[SUCCESS]";
        private String fail_status = "[FAIL]";

        public ClientHandler(Socket clientSocket, ConcurrentHashMap<String, List<String>> dictionary, String threadName) {
            this.clientSocket = clientSocket;
            this.dictionary = dictionary;
            this.clientId = Integer.parseInt(threadName.split("-")[1]);
        }

        public String Server_Report (int clientId, String status, String message) {
            return "Client " + clientId + ":" + status + " " + message;
        }

        public String Client_Report (String status, String message) {
            return status + "  " + message;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    String[] command = inputLine.split(" ", 2);

                    // Check if the client wants to exit
                    if (command.length > 0) {
                        if (command[0].equalsIgnoreCase("exit")) {
                            break;
                        }
                    }
                    
                    if (command.length < 2) {
                        System.out.println(Server_Report(clientId, fail_status, "Invalid command: Arguments missing"));
                        out.println("Invalid command: Arguments missing");
                        out.println("END");
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
                            System.out.println(Server_Report(clientId, fail_status, "Invalid command"));
                            out.println("Invalid command");
                            out.println("END");
                            break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error communicating with client" + clientId + ": " + e.getMessage());
            } finally {
                try {
                    System.out.println("Client" + clientId + ": disconnected");
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client" + clientId +"socket: " + e.getMessage());
                }
            }
        }

        private void addWord(String word) {
            String[] word_meaning = word.split(" ", 2);
            if (word_meaning.length < 2) {
                out.println("Add command with wrong syntax");
                System.out.println(Server_Report(clientId, fail_status, "Add command with wrong syntax"));
                return;
            }

            String wordToAdd = word_meaning[0];
            String meaningToAdd = word_meaning[1];

            if (dictionary.containsKey(wordToAdd)) {
                List<String> meanings = dictionary.get(wordToAdd);
                meanings.add(meaningToAdd);
                dictionary.put(wordToAdd, meanings);
                out.println(Client_Report(sucess_status, "Successfully added meaning to the word " + wordToAdd));
                System.out.println(Server_Report(clientId, sucess_status, "Add meaning to the word " + wordToAdd));
            } else {
                List<String> meanings = new ArrayList<>();
                meanings.add(meaningToAdd);
                dictionary.put(wordToAdd, meanings);
                out.println(Client_Report(sucess_status, "Successfully added word " + wordToAdd));
                System.out.println(Server_Report(clientId, sucess_status, "Add word " + wordToAdd));
            }
        }


        private void deleteWord(String word) {
            if (dictionary.containsKey(word)) {
                dictionary.remove(word);
                out.println(Client_Report(sucess_status, "Successfully deleted word " + word));
                System.out.println(Server_Report(clientId, sucess_status, "Delete word " + word));
            } else {
                out.println(Client_Report(fail_status, "Delete word " + word + " does not exist"));
                System.out.println(Server_Report(clientId, fail_status, "Delete word " + word + " does not exist"));
            }
        }


        private void searchWord(String word) {
            if (dictionary.containsKey(word)) {
                List<String> meanings = dictionary.get(word);
                out.println(word + " has " + meanings.size() + " meanings");
                int num_meanings = 1;
                for (String meaning : meanings) {
                    out.println(num_meanings + ". " + meaning);
                    num_meanings++;
                }
                out.println("END");
                System.out.println(Server_Report(clientId, sucess_status, "Search word " + word));
            } else {
                out.println(Client_Report(fail_status, "Search word " + word + " does not exist"));
                System.out.println(Server_Report(clientId, fail_status, "Search word " + word + " does not exist"));
                out.println("END");
            }
        }

        
        private void updateWord(String word) {
            String[] word_meaning = word.split(" ", 2);
            if (word_meaning.length < 2) {
                System.out.println(Server_Report(clientId, fail_status, "Update command with wrong syntax"));
                out.println(Client_Report(fail_status, "Update command with wrong syntax"));
                return;
            }

            String wordToUpdate = word_meaning[0];
            String meaningToUpdate = word_meaning[1];

            if (dictionary.containsKey(wordToUpdate)) {
                List<String> meanings = new ArrayList<>();
                meanings.add(meaningToUpdate);
                dictionary.put(wordToUpdate, meanings);
                out.println(Client_Report(sucess_status, "Successfully updated meaning to the word " + wordToUpdate));
                System.out.println(Server_Report(clientId, sucess_status, "Update meaning to the word " + wordToUpdate));
            } else {
                out.println(Client_Report(fail_status, "Update word " + wordToUpdate + " does not exist"));
                System.out.println(Server_Report(clientId, fail_status, "Update word " + wordToUpdate + " does not exist"));
            }
        }
    }
}
