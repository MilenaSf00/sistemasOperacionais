package database;

public class DatabaseVector {
    private int[] vector;

    public DatabaseVector() {
        this.vector = new int[0]; // Inicializa um vetor vazio
    }

    public void setSize(int size) {
        this.vector = new int[size]; // Redimensiona o vetor
    }

    public int getSize() {
        return vector.length;
    }

    public int[] getVector() {
        return vector;
    }
}
