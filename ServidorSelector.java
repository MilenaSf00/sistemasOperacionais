
import database.DatabaseVector;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class ServidorSelector {
    private static final int PORT = 12345;
    private boolean useLock;
    private DatabaseVector database;

    public ServidorSelector(DatabaseVector database) {
        this.database = database;
    }

    public ServidorSelector(boolean useLock) {
        this.useLock = useLock;
        this.database = new DatabaseVector();
        this.database.setSize(10);
    }

    public void iniciar() {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverChannel = ServerSocketChannel.open()) {

            serverChannel.bind(new InetSocketAddress(PORT));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Servidor de seletor iniciado na porta " + PORT);

            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isAcceptable()) {
                        acceptClient(selector, serverChannel);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptClient(Selector selector, ServerSocketChannel serverChannel) throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(256);
        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            channel.close();
            return;
        }

        buffer.flip();
        String request = new String(buffer.array(), 0, bytesRead).trim();
        // Parse and handle the request (READ/WRITE)
    }
}
