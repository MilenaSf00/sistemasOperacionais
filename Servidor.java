import database.DatabaseVector;
import java.io.*;
import java.net.*;

public class Servidor {
    private static final int SERVER_PORT = 8080;
    private static final int VECTOR_SIZE = 10;
    private final DatabaseVector database;

    public Servidor() {
        this.database = new DatabaseVector(VECTOR_SIZE);
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Servidor iniciado na porta " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress());

                new Thread(new ClienteHandler(clientSocket, database)).start();
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }

    // Método para calcular a soma total dos valores no vetor
    public int calcularSomaTotal() {
        int total = 0;
        for (int i = 0; i < database.getSize(); i++) {
            total += database.read(i);
        }
        return total;
    }

    private static class ClienteHandler implements Runnable {
        private final Socket clientSocket;
        private final DatabaseVector database;

        public ClienteHandler(Socket clientSocket, DatabaseVector database) {
            this.clientSocket = clientSocket;
            this.database = database;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String input;
                while ((input = in.readLine()) != null) {
                    String[] parts = input.split(" ");
                    String command = parts[0];
                    int index = Integer.parseInt(parts[1]);

                    if ("READ".equals(command)) {
                        int value = database.read(index);
                        out.println("Valor no índice " + index + ": " + value);
                    } else if ("WRITE".equals(command)) {
                        database.write(index, database.read(index) + 1);
                        out.println("Incrementado índice " + index + " para " + database.read(index));
                    }
                }
            } catch (IOException e) {
                System.err.println("Erro na comunicação com o cliente: " + e.getMessage());
            }
        }
    }
}
