import java.io.*;
import java.net.*;

public class Cliente {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            // Recebe a solicitação de tamanho do 'database' do servidor
            System.out.println(in.readLine());
            String sizeInput = stdIn.readLine();
            out.println(sizeInput); // Envia o tamanho de volta para o servidor

            System.out.println("Conectado ao servidor. Digite comandos (e.g., update 1 10, sum):");

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                String response = in.readLine();
                System.out.println("Resposta do servidor: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
