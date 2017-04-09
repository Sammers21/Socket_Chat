package ru.drankov.server;

import ru.drankov.util.Console;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {

    public Selector sel = null;
    public ServerSocketChannel server = null;
    public List<SocketChannel> sockets = new ArrayList<>();
    private Map<String, String> chats = new HashMap<>(1);

    Console serverConsole;

    private Semaphore semaphore = new Semaphore(1);
    private AtomicBoolean cancellsed = new AtomicBoolean(false);

    public int port;

    public final String host = "localhost";

    Thread t;

    private void startServer() {

        Runnable r = () -> {
            //server acceptor
            try {
                semaphore.acquire();
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
                                System.out.println("Key is Acceptable");
                                serverConsole.cout("Key is Acceptable");
                                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                                SocketChannel socket = ssc.accept();
                                socket.configureBlocking(false);
                                socket.register(sel, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                                sockets.add(socket);
                            }
                            if (key.isReadable()) {
                                System.out.println("Key is readable");
                                serverConsole.cout("Key is readable");
                            }


                        }
                    }
                    try {
                        System.out.println("sleep");
                        serverConsole.cout("sleep");
                        Thread.sleep(100);
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
                    semaphore.release();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }


        };
        t = new Thread(r);
        t.start();
    }


    public Server(Console serverConsole) {
        this.serverConsole = serverConsole;
    }

    //server async init
    public synchronized void initServer(int port) throws IOException, InterruptedException {
        stopServer();
        portInit(port);
        startServer();
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
            chats.put("default", "");
        }

    }

    private void stopServer() throws InterruptedException {
        cancellsed.set(true);
        semaphore.acquire();
        semaphore.release();
    }

}
