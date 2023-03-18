import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.json.JSONObject;

public class DictionaryServer {
    private HashMap<String, List<String>> dictionary = new HashMap<>();
    private ServerSocket serverSocket;
    private int clientNumber = 1;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java DictionaryServer <port> <dictionary-file>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        String fileName = args[1];

        DictionaryServer server = new DictionaryServer();
        server.loadDictionaryFromFile(fileName);
        server.start(port);
    }

    private void loadDictionaryFromFile(String fileName) {
        try {
            String fileContent = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(fileContent);

            for (String key : jsonObject.keySet()) {
                List<String> meaningsList = new ArrayList<>();
                meaningsList.add(jsonObject.getString(key));
                dictionary.put(key, meaningsList);
            }

        } catch (IOException e) {
            System.err.println("Error reading the dictionary file: " + e.getMessage());
        }
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                
                int clientID = clientNumber;
                String threadName = "ClientHandler-" + clientNumber++;
                ClientHandler clientHandler = new ClientHandler(clientSocket, dictionary, threadName, clientID);
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                }
            }
        }
    }
}

