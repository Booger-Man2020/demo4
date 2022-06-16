package com.example.demo4;



import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class NioServer {
    private ServerSocketChannel ssc;
    private Selector selector;
    private List<String> list = new ArrayList<>();
    private  String homeDir = System.getProperty("user.home");
    private String ls = "ls";
    private String catFile = "cat";
    private String cdPath = "cd path";


    public NioServer() throws IOException {
        ssc = ServerSocketChannel.open();
        selector = Selector.open();
        ssc.socket().bind(new InetSocketAddress(8180));
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);


    }

    public void run() throws Exception {
        while (true) {
            if (selector.select(1000) == 0) {
                System.out.println("Сервер ждет ");
                continue;
            }

            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();

                if (key.isAcceptable()) {
                    handleAccept();
                }
                if (key.isReadable()) {
                    handleRead(key);
//                    SocketChannel sc = (SocketChannel) key.channel();
//                    ByteBuffer buffer = (ByteBuffer) key.attachment();
//                    sc.read(buffer);
//                    System.out.println("Сервер получает сообщение клиента:");

                }

                it.remove();


            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder s = new StringBuilder();
        while (channel.isOpen()) {
            int read = channel.read(byteBuffer);
            if (read < 0) {
                channel.close();
                return;
            }
            if (read == 0) {
                break;
            }
            byteBuffer.flip();
            while (byteBuffer.hasRemaining()) {
                s.append((char) byteBuffer.get());
            }
            byteBuffer.clear();

        }
        byte[] mess = s.toString().getBytes(StandardCharsets.UTF_8);
        channel.write(ByteBuffer.wrap(mess));
        String message =new String(mess);
        String [] message1= message.split("\r");
        String [] message2= message.split(" ");


        if (message1[0].equals(ls)) {
           list.addAll(getFiles(homeDir));
           byte[] mess1 = list.toString().getBytes(StandardCharsets.UTF_8);
            channel.write(ByteBuffer.wrap(mess1));

        }else if (message2[0].equals(catFile)){
            String [] mess2 = message2[1].split("\r");
            try(FileReader reader = new FileReader(mess2[0])) {
            // читаем посимвольно
                char[] buf = new char[256];
                int c;
                while((c = reader.read(buf))>0){

                    if(c < 256){
                        buf = Arrays.copyOf(buf, c);
                    }

                byte[] file = buf.toString().getBytes(StandardCharsets.UTF_8);
         channel.write(ByteBuffer.wrap(file));

        }

    }}}



    private Collection<String> getFiles(String homeDir) {
            String[] list = new File(homeDir).list();
            assert list != null;
            return Arrays.asList(list);

    }


    public void handleAccept() throws Exception {
        SocketChannel chanel = ssc.accept();
        chanel.configureBlocking(false);
        chanel.register(selector, SelectionKey.OP_READ);
        chanel.write(ByteBuffer.wrap("welcom  to hell \n".getBytes(StandardCharsets.UTF_8)));
    }
}
