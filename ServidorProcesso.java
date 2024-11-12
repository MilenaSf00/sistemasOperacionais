import database.DatabaseVector;
import java.io.IOException;
import java.net.Socket;

import java.net.ServerSocket;


public class ServidorProcesso {
    private DatabaseVector database;

    public ServidorProcesso(DatabaseVector database) {
        this.database = database;
    }

    public void iniciar() {
        System.out.println("Servidor em modo de processos iniciado...");
        // Aqui criamos novos processos para cada cliente.
        while (true) {
            // Aguardando conexões de clientes
            try (ServerSocket serverSocket = new ServerSocket(12345)) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());
                // Inicia um processo para atender o cliente
                ProcessBuilder builder = new ProcessBuilder("java", "Cliente", String.valueOf(clientSocket.getPort()));
                builder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
