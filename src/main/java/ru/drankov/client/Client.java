package ru.drankov.client;


import ru.drankov.util.Console;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class Client {

    public SocketChannel client = null;
    public InetSocketAddress isa = null;
    public ReceiveMesasgesThread rt = null;
    private Console console;

    public Client(Console console) {
        this.console = console;
    }

    public void send(String st) throws IOException {
        sendMessage(st, client);
        System.out.println("bytes sended to server");
    }

    private void sendMessage(String msg, SocketChannel socket) {
        byte[] bytes = new byte[0];
        try {
            bytes = msg.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.out.println("encode error");
        }
        try {

            //Header
            ByteBuffer header = ByteBuffer.allocate(4).putInt(bytes.length);
            header.flip();
            ByteBuffer body = ByteBuffer.allocate(4 + bytes.length);
            body.put(header);
            /*body.flip();*/
            body.put(bytes);
            body.flip();
            int w2 = socket.write(body);
            System.out.println("wrote " + w2);
            System.out.println("header is 4");
            System.out.println("body is "+bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
            console.cout("problem with message sending to sockets");
        }
    }

    public void makeConnection(String string) {
        String[] split = string.split(":");
        if (split.length != 2) {
            console.cout("invalid format of adders");
            return;
        }

        int port = Integer.parseInt(split[1]);
        String host = split[0];
        try {
            if (rt != null) {
                interruptThread();
            }
            if (client != null) {
                client.close();
            }
            client = SocketChannel.open();
            isa = new InetSocketAddress(host, port);
            client.connect(isa);
            client.configureBlocking(false);
            receiveMessage();
        } catch (IOException e) {
            e.printStackTrace();
            console.cout("invalid format of adders");
        }

    }

    public void receiveMessage() {
        rt = new ReceiveMesasgesThread("Receive THread", client);
        rt.start();

    }

    public void interruptThread() {
        rt.cancelled = false;
    }

    public class ReceiveMesasgesThread extends Thread {
        public SocketChannel channel = null;
        public boolean cancelled = true;

        public ReceiveMesasgesThread(String str, SocketChannel client) {
            super(str);
            channel = client;
        }

        @Override
        public void run() {

            System.out.println("Successful connection to server");
            console.cout("Successful connection to server");
            try {
                while (cancelled) {
                    int nBytes;
                    ByteBuffer buf = ByteBuffer.allocate(4);
                    //read length
                    nBytes = client.read(buf);
                    if (nBytes == 4) {

                        //  System.out.println("nnBytes " + nBytes);
                        buf.rewind();
                        int i = buf.asIntBuffer().get();
                        //   System.out.println("buf to alloc " + i);
                        buf.rewind();

                        //read file
                        ByteBuffer b2 = ByteBuffer.allocate(i);
                        nBytes = client.read(b2);
                        System.out.println("file nBytes =" + nBytes);
                        b2.rewind();

                        //print the file
                        Charset charset = Charset.forName("UTF-8");
                        CharsetDecoder decoder = charset.newDecoder();
                        CharBuffer charBuffer = decoder.decode(b2);
                        String result = charBuffer.toString();
                        System.out.println(result);
                        console.cout(result);
                        buf.clear();
                        b2.clear();
                    }

                    Thread.sleep(100);
                }


            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
