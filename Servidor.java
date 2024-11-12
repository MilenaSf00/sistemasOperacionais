import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.ExecutorService;  // Importação do ExecutorService
import java.util.concurrent.Executors;        // Importação do Executors
import database.DatabaseVector;

public class Servidor {
    
    private static final int PORT = 12345;
    private static boolean verbose = true;
    private static boolean useLock = false;
    private static DatabaseVector databaseVector = new DatabaseVector();
    private static final ReentrantLock lock = new ReentrantLock();
    private static boolean serverRunning = true;  // Variável para controlar o servidor

    public static void main(String[] args) {
        // Processando parâmetros de execução
        for (String arg : args) {
            if (arg.equals("verbose")) {
                verbose = true;
            } else if (arg.equals("lock")) {
                useLock = true;
            }
        }

        // Inicializando o vetor com tamanho fixo
        initializeDatabase(0);  

        ExecutorService executor = Executors.newCachedThreadPool();  // Para gerenciar múltiplos clientes
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log("Servidor iniciado na porta " + PORT);
            log("Servidor aguardando conexões...");

            // Espera por novas conexões de clientes
            while (serverRunning) {
                Socket clientSocket = serverSocket.accept();
                log("Cliente conectado: " + clientSocket.getInetAddress());

                // Usando threads para atender múltiplos clientes simultaneamente
                executor.submit(new ClientHandler(clientSocket)); // Envia o socket do cliente para o manipulador
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();  // Aguarda a conclusão de todas as operações dos clientes
            log("Somatório do banco de dados: " + sumDatabase());
        }
    }

    private static void initializeDatabase(int size) {
        int defaultSize = 40;  // Tamanho fixo do vetor
        databaseVector.setSize(defaultSize); // Define o tamanho do vetor
        Arrays.fill(databaseVector.getVector(), 0); // Preenche com zeros
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
                    } else if (inputLine.equals("SHUTDOWN")) {  // Fechando o servidor quando o comando for recebido
                        out.println("Servidor encerrado. Somatório final: " + sumDatabase());
                        serverRunning = false;  // Interrompe o loop de espera de conexões
                        break;  // Sai do loop de leitura de mensagens
                    } else {
                        out.println("Comando desconhecido.");
                    }
                }
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
                lock.lock(); // Locking for concurrency control
                try {
                    databaseVector.write(index, 1); // Chama a função write sem o useLock
                } finally {
                    lock.unlock();
                }
            } else {
                databaseVector.write(index, 1); // Chama a função write sem lock
            }
        }

        private int readDatabase(int index) {
            return databaseVector.read(index);
        }
    }
}
