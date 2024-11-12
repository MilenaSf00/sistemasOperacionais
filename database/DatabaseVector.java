package database;

public class DatabaseVector {
    private int[] vector;

    // Método para definir o tamanho do vetor
    public void setSize(int size) {
        vector = new int[size];
    }

    // Método para obter o vetor
    public int[] getVector() {
        return vector;
    }

    // Método para escrever um valor no índice especificado
    public void write(int index, int value) {
        if (index >= 0 && index < vector.length) {
            vector[index] = value;
        } else {
            System.out.println("Índice fora dos limites.");
        }
    }

    // Método para ler um valor de um índice
    public int read(int index) {
        if (index >= 0 && index < vector.length) {
            return vector[index];
        } else {
            System.out.println("Índice fora dos limites.");
            return -1; // Ou outro valor padrão
        }
    }
}
