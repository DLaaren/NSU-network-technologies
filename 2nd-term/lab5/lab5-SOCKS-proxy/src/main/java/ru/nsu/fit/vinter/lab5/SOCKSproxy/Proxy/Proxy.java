package main.java.ru.nsu.fit.vinter.lab5.SOCKSproxy.Proxy;

import main.java.ru.nsu.fit.vinter.lab5.SOCKSproxy.Attachment.AcceptHandler;
import main.java.ru.nsu.fit.vinter.lab5.SOCKSproxy.Attachment.Attachment;
import main.java.ru.nsu.fit.vinter.lab5.SOCKSproxy.Attachment.ClientHandler;
import main.java.ru.nsu.fit.vinter.lab5.SOCKSproxy.Attachment.ServerHandler;
import main.java.ru.nsu.fit.vinter.lab5.SOCKSproxy.Exceptions.UnknownAttachmentType;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

// realization command 1 -- establish a TCP/IP stream connection
// non-blocking sockets in one thread (no additional threads)
// no blocking operations except the selector
// transferring data in both sides in one time
// no empty loops
// non-blocking resolving of domain names
// dnsjava

// https://stackoverflow.com/questions/3895461/non-blocking-sockets

public class Proxy implements Closeable {
    private static final Logger logger = Logger.getLogger(Proxy.class.getName());

    ServerSocketChannel serverSocketChannel;
    Selector selector;
    //DNSResolver dnsResolver;

    private int port;

    public Proxy(int port) {
        try {
            this.port = port;
            SocketAddress socket = new InetSocketAddress(InetAddress.getByName("localhost"), this.port);
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(socket);
            selector = Selector.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, ??);

        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Error while getting socket port number");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while creating resources");
        } finally {
            close();
        }
    }

    public void run() {
        try {
            // is blocking call
            while (selector.select() > -1) {
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    handleKeyAttachment(key.attachment());
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException at select");
        } catch (UnknownAttachmentType e) {
            logger.log(Level.SEVERE, "Error while handling selection key");
        }
    }

    private void handleKeyAttachment(Object attachmentObj) throws UnknownAttachmentType {

    }

    private void handleAttachment(AcceptHandler acceptHandler) {
        try {
            SocketChannel socketChannel = acceptHandler.accept();
            SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
            key.attach(new ClientHandler());
        } catch (ClosedChannelException e) {
            logger.log(Level.SEVERE, "Operation with closed channel");
        }
    }

    private void handleAttachment(ClientHandler clientHandler) {
        if (clientHandler.isWaiting()) {
            clientHandler.read();
        } else if (clientHandler.isSending()) {
            clientHandler.write();
        } else if (clientHandler.isReadable()) {
            clientHandler.getDataFromClient();
        } else if (clientHandler.isWritable()) {
            clientHandler.sendDataToClient();
        }
        clientHandler.changeState();
    }

    private void handleAttachment(ServerHandler serverHandler) {
        if (!serverHandler.isConnected) {
            serverHandler.connect();
        } else if (serverHandler.isReadable) {
            serverHandler.getDataFromServer();
        } else if (serverHandler.isWritable) {
            serverHandler.sendDataToServer();
        }
    }

    @Override
    public void close() {
        try {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
            // close dns
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while closing resources");
        }
    }
}
