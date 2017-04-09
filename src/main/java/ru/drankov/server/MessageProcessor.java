package ru.drankov.server;


import ru.drankov.util.Console;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class MessageProcessor {

    Console console;

    public List<SocketChannel> sockets = new ArrayList<>();
    private Map<String, StringBuilder> chats = new ConcurrentHashMap<>(1);

    ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(150);

    public MessageProcessor(Console console, List<SocketChannel> sockets, Map<String, StringBuilder> chats) {
        this.console = console;
        this.sockets = sockets;
        this.chats = chats;
        startProcessing();
    }

    private void startProcessing() {
        Thread msg = new Thread(() -> {
            while (true) {
                String pollstr = null;
                try {
                    pollstr = queue.take();
                    System.out.println("processor start handle :" + pollstr);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("console problem");
                }

                String[] poll = pollstr.split(" ");
                if (poll.length >= 4 && poll[0].equals("msg")) {
                    String time = getCurTime();
                    String str = time +
                            " [" +
                            poll[1] +
                            "]" +
                            ":" +
                            poll[3];
                    System.out.println("send  " + str + " to all chats");
                    System.out.println("socket amount " + sockets.size());
                    chats.get(poll[2])
                            .append(str)
                            .append("\n");
                    sendToChat(str, poll[2]);
                }
            }

        });
        msg.setDaemon(true);
        msg.start();
    }

    private String getCurTime() {
        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int min = rightNow.get(Calendar.MINUTE);
        int sec = rightNow.get(Calendar.SECOND);

        return "[" + hour + ":" + min + ":" + sec + "]";
    }

    public void process(String message) {
        queue.add(message);
    }

    private void sendToChat(String msg, String chatName) {
        final String toSend = "chat " + chatName + " " + msg;
        sockets.stream()
                .forEach(s ->
                        sendMessage(toSend, s));
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
            System.out.println("body is " + bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
            console.cout("problem with message sending to sockets");
        }
    }
}
