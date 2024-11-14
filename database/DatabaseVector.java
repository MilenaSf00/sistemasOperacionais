package database;

public class DatabaseVector {
    private final int[] vector;

    // Construtor que define o tamanho do vetor e inicializa com zeros
    public DatabaseVector(int size) {
        vector = new int[size];
    }

    // Método para obter o tamanho do vetor
    public int getSize() {
        return vector.length;
    }

    // Método sincronizado para escrever um valor no índice especificado
    public synchronized void write(int index, int value) {
        if (index >= 0 && index < vector.length) {
            vector[index] = value;
            System.out.println("Valor " + value + " escrito no índice " + index);
        } else {
            System.out.println("Índice fora dos limites.");
        }
    }

    // Método sincronizado para ler um valor de um índice
    public synchronized int read(int index) {
        if (index >= 0 && index < vector.length) {
            int value = vector[index];
            System.out.println("Valor lido do índice " + index + ": " + value);
            return value;
        } else {
            System.out.println("Índice fora dos limites.");
            return -1; // Ou outro valor padrão
        }
    }
}
