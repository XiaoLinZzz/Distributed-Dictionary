import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class DictionaryClient {

    public static void main(String[] args) {
        String serverAddress = args[0];
        int serverPort = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(serverAddress, serverPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            String userInput;
            System.out.println("Enter command (add, delete, search, update) or type 'exit' to quit:");

            while ((userInput = stdIn.readLine()) != null) {
                if (userInput.equalsIgnoreCase("exit")) {
                    System.out.println("Successfully disconnected from server.");
                    break;
                }

                out.println(userInput);
                String response = in.readLine();
                System.out.println("Server response: " + response);
                System.out.println("Enter another command or type 'exit' to quit:");
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + serverAddress);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + serverAddress);
            System.exit(1);
        }
    }
}
