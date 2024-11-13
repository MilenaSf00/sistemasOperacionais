import database.DatabaseVector;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.*;

public class Servidor {
    
    private static final int PORT = 12345;
    private static boolean verbose = true;
    private static boolean useLock = false;
    private static DatabaseVector databaseVector = new DatabaseVector();
    private static final ReentrantLock lock = new ReentrantLock();
    private static boolean serverRunning = true;
    private static int soma = 0;

    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.equals("verbose")) {
                verbose = true;
            } else if (arg.equals("lock")) {
                useLock = true;
            }
        }

        initializeDatabase(0);
        ExecutorService executor = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log("Servidor iniciado na porta " + PORT);
            log("Servidor aguardando conexões...");

            while (serverRunning) {
                Socket clientSocket = serverSocket.accept();
                log("Cliente conectado: " + clientSocket.getInetAddress());

                executor.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
            log("Somatório do banco de dados: " + sumDatabase());
        }
    }

    private static void initializeDatabase(int size) {
        int defaultSize = 40;
        databaseVector.setSize(defaultSize);
        Arrays.fill(databaseVector.getVector(), 0);
    }

    private static int sumDatabase() {
        return Arrays.stream(databaseVector.getVector()).sum();
    }

    private static void log(String message) {
        if (verbose) {
            System.out.println(message);
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    log("Mensagem recebida: " + inputLine);
                    if (inputLine.startsWith("WRITE")) {
                        String[] parts = inputLine.split(" ");
                        if (parts.length == 3) {
                            int index = Integer.parseInt(parts[1]);
                            writeDatabase(index);
                            out.println("Escrita realizada no índice " + index);
                        } else {
                            out.println("Comando inválido. Use: WRITE <índice>");
                        }
                    } else if (inputLine.startsWith("READ")) {
                        String[] parts = inputLine.split(" ");
                        if (parts.length == 2) {
                            int index = Integer.parseInt(parts[1]);
                            int value = readDatabase(index);
                            out.println("Valor no índice " + index + ": " + value);
                        } else {
                            out.println("Comando inválido. Use: READ <índice>");
                        }
                    } else if (inputLine.equals("SHUTDOWN")) {
                        out.println("Servidor encerrado. Somatório final: " + sumDatabase());
                        serverRunning = false;
                        break;
                    } else {
                        out.println("Comando desconhecido.");
                    }
                }
                log("Somatório dos valores: " + soma);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void writeDatabase(int index) {
            if (useLock) {
                lock.lock();
                try {
                    databaseVector.write(index, index + 1);
                    soma +=index;
                } finally {
                    lock.unlock();
                }
            } else {
                soma +=index;
                databaseVector.write(index, index + 1);
            }
        }        

        private int readDatabase(int index) {
            return databaseVector.read(index);
        }
    }
}
