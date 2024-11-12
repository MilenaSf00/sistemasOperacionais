import database.DatabaseVector;
import java.io.*;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;

public class Servidor {
    private static final int PORT = 12345;
    private static boolean verbose = false;
    private static boolean useLock = false;
    private static DatabaseVector databaseVector = new DatabaseVector();
    private static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.equals("verbose")) {
                verbose = true;
            } else if (arg.equals("lock")) {
                useLock = true;
            }
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log("Servidor iniciado na porta " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                log("Cliente conectado: " + clientSocket.getInetAddress());

                // Solicita o tamanho do vetor ao cliente antes de iniciar o processamento
                int databaseSize = getDatabaseSizeFromClient(clientSocket);
                initializeDatabase(databaseSize);
                System.out.println("Vetor 'database' inicializado com tamanho " + databaseSize);

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        log("Somatório do banco de dados: " + sumDatabase());
    }

    private static int getDatabaseSizeFromClient(Socket clientSocket) throws IOException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        out.println("Digite o tamanho do 'database':"); // Envia solicitação ao cliente
        String sizeInput = in.readLine(); // Lê resposta do cliente

        try {
            return Integer.parseInt(sizeInput); // Converte a resposta em inteiro
        } catch (NumberFormatException e) {
            log("Entrada inválida para o tamanho do 'database'. Usando tamanho padrão de 10.");
            return 10;
        }
    }

    private static void initializeDatabase(int size) {
        databaseVector.setSize(size);
        int[] vector = databaseVector.getVector();
        for (int i = 0; i < vector.length; i++) {
            vector[i] = 0;
        }
    }

    private static int sumDatabase() {
        int sum = 0;
        for (int value : databaseVector.getVector()) {
            sum += value;
        }
        return sum;
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
                    if (inputLine.startsWith("update")) {
                        String[] parts = inputLine.split(" ");
                        if (parts.length == 3) {
                            int index = Integer.parseInt(parts[1]);
                            int value = Integer.parseInt(parts[2]);
                            updateDatabase(index, value);
                            out.println("Atualização realizada no índice " + index);
                        } else {
                            out.println("Comando inválido. Use: update <índice> <valor>");
                        }
                    } else if (inputLine.equals("sum")) {
                        out.println("Somatório atual: " + sumDatabase());
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

        private void updateDatabase(int index, int value) {
            int[] vector = databaseVector.getVector();
            if (index < 0 || index >= vector.length) {
                log("Índice fora do limite: " + index);
                return;
            }

            if (useLock) {
                lock.lock();
                try {
                    vector[index] += value;
                } finally {
                    lock.unlock();
                }
            } else {
                synchronized (vector) {
                    vector[index] += value;
                }
            }
        }
    }
}
