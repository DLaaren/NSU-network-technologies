package main.java.ru.nsu.fit.vinter.lab5.SOCKSproxy.Proxy;

import main.java.ru.nsu.fit.vinter.lab5.SOCKSproxy.Exceptions.BadRequestException;
import main.java.ru.nsu.fit.vinter.lab5.SOCKSproxy.Exceptions.UnsupportedCommandInProtocolException;
import main.java.ru.nsu.fit.vinter.lab5.SOCKSproxy.Exceptions.WrongProtocolVersion;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
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

public class Proxy implements Runnable {
    private static final Logger logger = Logger.getLogger(Proxy.class.getName());

    private final static int bufferSize = 8192;
    static final byte[] OK = new byte[] { 0x00, 0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
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
            serverSocketChannel.register(selector, serverSocketChannel.validOps());
            logger.info("Proxy is listening on port " + this.port);

        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Error while getting socket port number");
            closeProxy();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while creating resources");
            closeProxy();
        }
    }

    static class ConnectionContext {
        ByteBuffer in;
        ByteBuffer out;
        SelectionKey peer;
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
                    // get connection request
                    if (key.isAcceptable()) {
                        SocketChannel newChannel = ((ServerSocketChannel)key.channel()).accept();
                        newChannel.configureBlocking(false);
                        newChannel.register(key.selector(), SelectionKey.OP_READ);
                        logger.info("Get connection request");
                    }
                    // establish connection
                    else if (key.isConnectable()) {
                        SocketChannel channel = ((SocketChannel) key.channel());
                        ConnectionContext connectionContext = ((ConnectionContext) key.attachment());
                        channel.finishConnect();
                        connectionContext.in = ByteBuffer.allocate(bufferSize);
                        connectionContext.in.put(OK).flip();
                        connectionContext.out = ((ConnectionContext) connectionContext.peer.attachment()).in;
                        ((ConnectionContext) connectionContext.peer.attachment()).out = connectionContext.in;
                        connectionContext.peer.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                        key.interestOps( 0 );
                        logger.info("Answering to the request and starting connection process");
                    }

                    else if (key.isReadable()) {
                        SocketChannel channel = ((SocketChannel) key.channel());
                        ConnectionContext connectionContext = ((ConnectionContext) key.attachment());
                        if (connectionContext == null) {
                            // initialize buffers
                            key.attach(connectionContext = new ConnectionContext());
                            connectionContext.in = ByteBuffer.allocate(bufferSize);
                        }
                        if (channel.read(connectionContext.in) < 1) {
                            closeKey(key);
                            logger.log(Level.WARNING, "Error while reading - closing the connection");
                        }
                        // if we don't know receiver then read the header or msg
                        else if (connectionContext.peer == null) {
                            logger.info("Start reading header");
                            readHeader(key, connectionContext);
                        }
                        else {
                            // add write-interest to one side
                            connectionContext.peer.interestOps(connectionContext.peer.interestOps() | SelectionKey.OP_WRITE);
                            // remove read-interest from another side 'cause we don't write something for it for now
                            key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
                            // flip buffer to writing
                            connectionContext.in.flip();
                        }
                    }

                    else if (key.isWritable()) {
                        SocketChannel channel = ((SocketChannel) key.channel());
                        ConnectionContext connectionContext = ((ConnectionContext) key.attachment());
                        if (channel.write(connectionContext.out) == -1) {
                            closeKey(key);
                            logger.log(Level.WARNING, "Error while writing - closing the connection");
                        }
                        else if (connectionContext.out.remaining() == 0) {
                            if (connectionContext.peer == null) {
                                closeKey(key);
                                logger.log(Level.WARNING, "Receiver is closed - closing the connection");
                            }
                            else {
                                connectionContext.out.clear();
                                // add read-interest to one side
                                connectionContext.peer.interestOps(connectionContext.peer.interestOps() | SelectionKey.OP_READ);
                                // remove write-interest from another side 'cause we don't read something for it for now
                                key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException at select");
        } catch (BadRequestException | UnsupportedCommandInProtocolException | WrongProtocolVersion e) {
            logger.info("");
        }
    }

    void readHeader(SelectionKey key, ConnectionContext connectionContext) throws IOException, BadRequestException, WrongProtocolVersion, UnsupportedCommandInProtocolException {
        byte[] header = connectionContext.in.array();
        logger.info("Got header :: " + Arrays.toString(header));
//        logger.info("Got header :: " + Arrays.stream(header).forEachOrdered());
        if (header[connectionContext.in.position() - 1] == 0) {
            // check for the version of the protocol and validity of the command (connect)
            if (header[0] != 4) {
                logger.log(Level.SEVERE, "Got wrong protocol version");
                closeKey(key);
                throw new WrongProtocolVersion("Wrong protocol version");
            }
            if (header[1] != 1) {
                logger.log(Level.SEVERE, "Got unsupported command code");
                closeKey(key);
                throw new UnsupportedCommandInProtocolException("Unsupported command code");
            }
            if (connectionContext.in.position() < 8) {
                logger.log(Level.SEVERE, "Bad request");
                closeKey(key);
                throw new BadRequestException("Bad request");
            }
            // connection
            else {
                logger.info("The header was read successful");
                SocketChannel peer = SocketChannel.open();
                peer.configureBlocking(false);
                // getting address and port
                byte[] address = new byte[]{header[4], header[5], header[6], header[7]};
                logger.info("address :: " + InetAddress.getByAddress(address).getCanonicalHostName());
                int port = (((0xFF & header[2]) << 8) + (0xFF & header[3]));
                logger.info("port :: " + port);
                peer.connect(new InetSocketAddress(InetAddress.getByAddress(address), port));
                // register in selector
                SelectionKey peerKey = peer.register(key.selector(), SelectionKey.OP_CONNECT);
                // do not interact with the key for now
                key.interestOps(0);
                //key exchange
                connectionContext.peer = peerKey;
                ConnectionContext peerConnectionContext = new ConnectionContext();
                peerConnectionContext.peer = key;
                peerKey.attach(peerConnectionContext);
                connectionContext.in.clear();
                logger.info("Connection has been established");
            }
        }
    }

    void closeKey(SelectionKey key) throws IOException {
        key.cancel();
        key.channel().close();
        SelectionKey peer = ((ConnectionContext) key.attachment()).peer;
        if (peer != null) {
            ((ConnectionContext) peer.attachment()).peer = null;
            if ((peer.interestOps() & SelectionKey.OP_WRITE) == 0) {
                ((ConnectionContext) peer.attachment()).out.flip();
            }
            peer.interestOps(SelectionKey.OP_WRITE);
        }
    }

    public void closeProxy() {
        logger.info("Closing proxy");
        try {
            serverSocketChannel.close();
            selector.close();
            // close dns
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while closing resources");
        }
    }
}
