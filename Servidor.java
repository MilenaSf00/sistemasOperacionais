import java.io.*;
import java.net.*;

public class Servidor {
    private static final int SERVER_PORT = 8080;

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Servidor iniciado na porta " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();  // Aguarda um cliente
                System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress());

                new Thread(new ClienteHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }
    
    private static class ClienteHandler implements Runnable {
        private Socket clientSocket;

        public ClienteHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                
                String input;
                while ((input = in.readLine()) != null) {
                    System.out.println("Recebido do cliente: " + input);
                    out.println("Servidor respondeu: " + input);
                }
            } catch (IOException e) {
                System.err.println("Erro na comunicação com o cliente: " + e.getMessage());
            }
        }
    }
}

