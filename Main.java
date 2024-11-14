import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Inicia o servidor em uma nova thread
        Servidor servidor = new Servidor();
        new Thread(servidor::iniciar).start();

        System.out.print("Informe o número de clientes (threads) que se conectarão ao servidor: ");
        int numClients = scanner.nextInt();

        System.out.print("Número de operações de leitura (R): ");
        int numReads = scanner.nextInt();

        System.out.print("Número de operações de escrita (W): ");
        int numWrites = scanner.nextInt();

        System.out.print("Sequência de operações (RW e WR): ");
        String operationSequence = scanner.next();

        for (int i = 0; i < numClients; i++) {
            Cliente client = new Cliente(numReads, numWrites, operationSequence);
            new Thread(client).start();
        }

        // Espera um tempo para que os clientes terminem de fazer suas operações
        try {
            Thread.sleep(5000);  // Ajuste o tempo conforme necessário
        } catch (InterruptedException e) {
            System.err.println("Erro ao esperar: " + e.getMessage());
        }

        // Calcula a soma total dos valores no vetor após todas as operações
        int totalSoma = servidor.calcularSomaTotal();
        System.out.println("Soma total dos valores no vetor: " + totalSoma);

        scanner.close();
    }
}
