import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Servidor servidor = new Servidor();
        new Thread(() -> servidor.iniciar()).start();

        System.out.print("Informe o número de clientes (threads) que se conectarão ao servidor: ");
        int numClients = scanner.nextInt();

        for (int i = 0; i < numClients; i++) {
            Cliente client = new Cliente();
            new Thread(client).start();
        }

        scanner.close();
    }
}
