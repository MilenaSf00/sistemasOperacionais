import database.DatabaseVector;
import java.io.*;
import java.net.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServidorThread {
    private static final int PORT = 12345;
    private boolean useLock;
    private DatabaseVector database;
    private Lock lock;

    // Construtor que aceita um DatabaseVector como parâmetro
    public ServidorThread(DatabaseVector database) {
        this.database = database;
        this.useLock = false;  // Por padrão, inicia sem o controle de concorrência
        this.lock = new ReentrantLock();
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor de threads iniciado na porta " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request;
            while ((request = in.readLine()) != null) {
                String[] parts = request.split(" ");
                String command = parts[0];

                if ("READ".equalsIgnoreCase(command)) {
                    int index = Integer.parseInt(parts[1]);
                    int value = database.read(index);
                    out.println("Valor lido na posição " + index + ": " + value);
                } else if ("WRITE".equalsIgnoreCase(command)) {
                    int index = Integer.parseInt(parts[1]);
                    int increment = Integer.parseInt(parts[2]);
                    database.write(index, increment, useLock);
                    out.println("Valor incrementado em " + increment + " na posição " + index);
                } else {
                    out.println("Comando inválido. Use READ ou WRITE.");
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
