package ru.drankov.server;

import ru.drankov.util.Consolable;
import ru.drankov.util.StupidConsole;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {

    public Selector sel = null;
    public ServerSocketChannel server = null;
    public List<SocketChannel> sockets = new ArrayList<>();
    private Map<String, StringBuilder> chats = new ConcurrentHashMap<>(1);

    Consolable serverConsole;

    private Semaphore semaphoreForRecieve = new Semaphore(1);
    private Semaphore rebootsem = new Semaphore(1);
    private Semaphore msgSemaphore = new Semaphore(1);
    private AtomicBoolean cancellsed = new AtomicBoolean(false);

    private MessageProcessor messageProcessor = new MessageProcessor(serverConsole, sockets, chats);

    public int port;

    public final String host = "localhost";

    Thread t;

    public static void main(String[] args) {
        new Thread(() -> {
            Server server = new Server(new StupidConsole());
            try {
                server.initServer(8080);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startServer() {

        Runnable r = () -> {
            //server acceptor
            try {
                semaphoreForRecieve.acquire();
                SelectionKey acceptKey = server.register(sel, SelectionKey.OP_ACCEPT);

                while (true) {
                    if (cancellsed.get()) {
                        System.out.println("stop server");
                        serverConsole.cout("stop server");
                        throw new IOException("server was stopped");
                    }
                    while (acceptKey.selector().selectNow() > 0) {
                        Set<SelectionKey> readyKeys = sel.selectedKeys();
                        Iterator<SelectionKey> it = readyKeys.iterator();

                        while (it.hasNext()) {
                            SelectionKey key = it.next();
                            it.remove();

                            if (key.isAcceptable()) {
                                System.out.println("key is Acceptable");
                                serverConsole.cout("Key is Acceptable");
                                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                                SocketChannel socket = ssc.accept();
                                socket.configureBlocking(false);
                                socket.register(sel, SelectionKey.OP_READ | SelectionKey.OP_WRITE, socket);
                                sockets.add(socket);
                            }
                            if (key.isReadable()) {
                                System.out.println("Key is readable");
                                serverConsole.cout("key is readable");
                                handleRead(key);
                            }
                        }
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                // e.printStackTrace();
                try {
                    //flush connection
                    sel.close();
                    server.close();
                    for (SocketChannel socket : sockets) {
                        socket.finishConnect();
                    }
                    semaphoreForRecieve.release();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }


        };
        t = new Thread(r);
        t.setDaemon(true);
        t.start();
    }

    private void handleRead(SelectionKey key) {

        String result = null;
        try {
            result = getMessageFomChannel(key);
            serverConsole.cout("received " + result);
            messageProcessor.process(result);
        } catch (IOException e) {
            e.printStackTrace();
            serverConsole.cout("problems with message reading");
        }
        System.out.println("decoded " + result);
    }

    private String getMessageFomChannel(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();


        String result = null;
        int nBytes;
        ByteBuffer buf = ByteBuffer.allocate(4);
        //read length
        nBytes = channel.read(buf);
        if (nBytes == 4) {

            System.out.println("nnBytes " + nBytes);
            buf.flip();
            int i = buf.asIntBuffer().get();
            System.out.println("buf to alloc " + i);
            buf.rewind();

            //read file
            ByteBuffer b2 = ByteBuffer.allocate(i);
            nBytes = channel.read(b2);
            System.out.println("file nBytes =" + nBytes);
            b2.rewind();

            //print the file
            Charset charset = Charset.forName("UTF-8");
            CharsetDecoder decoder = charset.newDecoder();
            CharBuffer charBuffer = decoder.decode(b2);
            result = charBuffer.toString();
            System.out.println(result);
            buf.clear();
            b2.clear();

            return result;
        }

        ByteBuffer bu1f = ByteBuffer.allocate(1);
        int i;
        while ((i = channel.read(bu1f)) != 0) {
            bu1f.clear();
            bu1f.flip();
        }
        channel.finishConnect();
        key.cancel();
        sockets.remove(channel);
        throw new IOException("cant read from channel");

    }


    public Server(Consolable serverConsole) {
        this.serverConsole = serverConsole;
    }

    //server async init
    public void initServer(int port) throws IOException, InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                rebootsem.acquire();
                stopServer();
                portInit(port);
                startServer();
                rebootsem.release();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();

    }

    private void portInit(int port) throws IOException {

        this.port = port;
        sel = Selector.open();
        server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.socket().bind(
                new InetSocketAddress(host, port)
        );

        cancellsed.set(false);

        //if main chat haven't initialized yet
        String aDefault = chats.keySet()
                .stream()
                .filter(s -> s.equals("default"))
                .findAny()
                .orElse(null);
        if (aDefault == null) {
            chats.put("default", new StringBuilder(""));
        }

    }

    private void stopServer() throws InterruptedException {
        cancellsed.set(true);
        semaphoreForRecieve.acquire();
        semaphoreForRecieve.release();
    }

}
