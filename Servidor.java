import database.DatabaseVector;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

public class Servidor {
    private static final int PORT = 12345;
    private static boolean verbose = false; // (a) Parâmetro de execução para mensagens do servidor
    private static boolean useLock = false; // (b) Parâmetro de execução para controle de concorrência
    private static DatabaseVector databaseVector = new DatabaseVector();
    private static final ReentrantLock lock = new ReentrantLock(); // Controle de concorrência

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Solicita ao usuário o tamanho do vetor
        System.out.print("Tamanho do 'database': ");
        int size = scanner.nextInt();
        scanner.close();

        // (a) Parâmetro de execução para habilitar mensagens do servidor
        for (String arg : args) {
            if (arg.equals("verbose")) {
                verbose = true;
            } else if (arg.equals("lock")) {
                useLock = true; // (b) Controle de concorrência habilitado com parâmetro de execução
            }
        }

        // (e) Inicializar o vetor/array do banco de dados com 0 (zero) em todas as posições
        initializeDatabase(size);

        System.out.println("Vetor 'database' inicializado com tamanho " + size);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log("Servidor iniciado na porta " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                log("Cliente conectado: " + clientSocket.getInetAddress());
                
                // (c) Implementação de processo leve (threads)
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // (f) Imprimir no final da execução do servidor o somatório de todas as posições do vetor
        log("Somatório do banco de dados: " + sumDatabase());
    }

    private static void initializeDatabase(int size) {
        databaseVector.setSize(size);
        int[] vector = databaseVector.getVector();
        for (int i = 0; i < vector.length; i++) {
            vector[i] = 0; // (e) Inicialização do vetor com 0
        }
    }

    private static int sumDatabase() {
        int sum = 0;
        for (int value : databaseVector.getVector()) {
            sum += value;
        }
        return sum; // (f) Somatório do banco de dados
    }

    private static void log(String message) {
        if (verbose) { // (a) Controle para exibir mensagens do servidor
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

            if (useLock) { // (b) Controle de concorrência com ReentrantLock
                lock.lock();
                try {
                    vector[index] += value;
                } finally {
                    lock.unlock();
                }
            } else {
                synchronized (vector) { // (b) Controle de concorrência com synchronized
                    vector[index] += value;
                }
            }
        }
    }
}
