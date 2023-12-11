package com.coderzoe.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author yinhuasheng
 * @email yinhuasheng@unicloud.com
 * @date 2023/12/11 15:19
 */
public class NioDemo {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(8080);
        serverSocketChannel.socket().bind(inetSocketAddress);
        //设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true){
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();

            while (selectionKeyIterator.hasNext()){
                SelectionKey selectionKey = selectionKeyIterator.next();
                if(selectionKey.isAcceptable()){
                    //因为已经确定了有客户端连接，所以不会阻塞
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    //注册到Selector上，同时给该Channel关联一个Buffer上去
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector,SelectionKey.OP_READ);
                }else if(selectionKey.isReadable()){    //可读事件 针对服务器端而言的读事件
                    try {
                        SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        socketChannel.read(buffer);
                        System.out.println("客户端发送一条数据:"+new String(buffer.array()));
                    }catch (Exception e){
                        System.out.println("客户端断开连接");
                        selectionKey.cancel();
                        selectionKey.channel().close();
                    }
                }
                selectionKeyIterator.remove();
            }
        }
    }
}
