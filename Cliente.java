import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Cliente implements Runnable {
    private static boolean verbose = false;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static int numReadOperations;
    private static int numWriteOperations;
    private static int numClients;
    private static String operationSequence;

    public Cliente() {
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            for (int i = 0; i < Math.max(numReadOperations, numWriteOperations); i++) {
                if (operationSequence.equalsIgnoreCase("RW") || operationSequence.equalsIgnoreCase("intercalada")) {
                    if (i < numReadOperations) {
                        out.println("READ " + i);
                        String response = in.readLine();
                        log(Thread.currentThread().getName() + " - Resposta READ: " + response);
                    }
                    if (i < numWriteOperations) {
                        out.println("WRITE " + i + " 1");
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

        System.out.print("Informe o número de clientes (threads): ");
        numClients = scanner.nextInt();

        System.out.print("Informe o número de operações READ: ");
        numReadOperations = scanner.nextInt();

        System.out.print("Informe o número de operações WRITE: ");
        numWriteOperations = scanner.nextInt();

        scanner.nextLine();

        System.out.print("Informe a sequência de operações (RW, WR, intercalada): ");
        operationSequence = scanner.nextLine();

        System.out.print("Deseja ativar o modo verbose? (sim/não): ");
        String verboseInput = scanner.nextLine();
        if (verboseInput.equalsIgnoreCase("sim")) {
            verbose = true;
        }

        ExecutorService executor = Executors.newFixedThreadPool(numClients);

        for (int i = 0; i < numClients; i++) {
            executor.execute(new Cliente());
        }

        executor.shutdown();
        scanner.close();
    }
}
