import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Scanner;

public class Cliente implements Runnable {
    private static boolean verbose = false; // Variável verbose no cliente
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static int numReadOperations;
    private static int numWriteOperations;
    private static int numClients;
    private static String operationSequence; // RW, WR ou intercalada

    public Cliente() {
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Executa operações conforme a sequência definida
            for (int i = 0; i < Math.max(numReadOperations, numWriteOperations); i++) {
                if (operationSequence.equalsIgnoreCase("RW") || operationSequence.equalsIgnoreCase("intercalada")) {
                    if (i < numReadOperations) {
                        out.println("READ " + i);  // Simula leitura de uma posição
                        String response = in.readLine();
                        log(Thread.currentThread().getName() + " - Resposta READ: " + response);
                    }
                    if (i < numWriteOperations) {
                        out.println("WRITE " + i + " 1");  // Simula escrita de incremento em 1 na posição i
                        String response = in.readLine();
                        log(Thread.currentThread().getName() + " - Resposta WRITE: " + response);
                    }
                } else if (operationSequence.equalsIgnoreCase("WR")) {
                    if (i < numWriteOperations) {
                        out.println("WRITE " + i + " 1");
                        String response = in.readLine();
                        log(Thread.currentThread().getName() + " - Resposta WRITE: " + response);
                    }
                    if (i < numReadOperations) {
                        out.println("READ " + i);
                        String response = in.readLine();
                        log(Thread.currentThread().getName() + " - Resposta READ: " + response);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void log(String message) {
        if (verbose) {
            System.out.println(message);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Coleta de dados do usuário
        System.out.print("Informe o número de clientes (threads): ");
        numClients = scanner.nextInt();

        System.out.print("Informe o número de operações READ: ");
        numReadOperations = scanner.nextInt();

        System.out.print("Informe o número de operações WRITE: ");
        numWriteOperations = scanner.nextInt();

        scanner.nextLine();  // Consome a nova linha após o nextInt()

        System.out.print("Informe a sequência de operações (RW, WR, intercalada): ");
        operationSequence = scanner.nextLine();

        // Verificação do argumento verbose
        System.out.print("Deseja ativar o modo verbose? (sim/não): ");
        String verboseInput = scanner.nextLine();
        if (verboseInput.equalsIgnoreCase("sim")) {
            verbose = true;
        }

        // Criação do pool de threads para emular clientes
        ExecutorService executor = Executors.newFixedThreadPool(numClients);

        for (int i = 0; i < numClients; i++) {
            executor.execute(new Cliente());
        }

        // Finaliza o executor após a execução de todas as threads
        executor.shutdown();
        scanner.close();
    }
}
