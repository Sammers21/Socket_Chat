package ru.drankov.server;


import ru.drankov.util.Consolable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class MessageProcessor {

    Consolable console;

    public List<SocketChannel> sockets = new ArrayList<>();
    private Map<String, StringBuilder> chats = new ConcurrentHashMap<>(1);

    ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(150);

    public MessageProcessor(Consolable console, List<SocketChannel> sockets, Map<String, StringBuilder> chats) {
        this.console = console;
        this.sockets = sockets;
        this.chats = chats;
        startProcessing();
    }

    /**
     * Create main thread of receiver that wait for new messages
     */
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
                    String reduce =
                            Arrays.stream(poll)
                                    .skip(3)
                                    .reduce((s1, s2) -> s1 + " " + s2)
                                    .orElse("");

                    String time = getCurTime();
                    String str = time +
                            " [" +
                            poll[1] +
                            "]" +
                            ":" +
                            reduce;

                    System.out.println("send  " + str + " to all chats");
                    System.out.println("socket amount " + sockets.size());
                    chats.get(poll[2])
                            .append(" ")
                            .append(str)
                            .append("\n");
                    sendToChat(str, poll[2]);
                }
            }

        });
        msg.setDaemon(true);
        msg.start();
    }

    /**
     * Get Formatted string
     * @return
     */
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

    /**
     * Send to all clients message
     * @param msg message to send
     * @param chatName chat name
     */
    private void sendToChat(String msg, String chatName) {
        final String toSend = "chat " + chatName + " " + msg;
        System.out.println("to send " + "chat " + chatName + " " + msg);
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
