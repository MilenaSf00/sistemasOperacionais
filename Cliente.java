import java.io.*;
import java.net.*;

public class Cliente implements Runnable {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;
    private final int numReads;
    private final int numWrites;
    private final String operationSequence;

    public Cliente(int numReads, int numWrites, String operationSequence) {
        this.numReads = numReads;
        this.numWrites = numWrites;
        this.operationSequence = operationSequence;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            int readOps = 0;
            int writeOps = 0;
            int index = 0;

            for (char operation : operationSequence.toCharArray()) {
                if (operation == 'R' && readOps < numReads) {
                    out.println("READ " + index);
                    String response = in.readLine();
                    System.out.println("Cliente " + Thread.currentThread().getId() + " recebeu: " + response);
                    readOps++;
                } else if (operation == 'W' && writeOps < numWrites) {
                    out.println("WRITE " + index);
                    String response = in.readLine();
                    System.out.println("Cliente " + Thread.currentThread().getId() + " recebeu: " + response);
                    writeOps++;
                }

                index = (index + 1) % 10;
            }

        } catch (IOException e) {
            System.err.println("Erro na comunicação com o servidor: " + e.getMessage());
        }
    }
}
