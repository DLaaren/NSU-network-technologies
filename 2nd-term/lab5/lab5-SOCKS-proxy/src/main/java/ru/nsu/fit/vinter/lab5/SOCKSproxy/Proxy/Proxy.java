package ru.nsu.fit.vinter.lab5.SOCKSproxy.Proxy;

import ru.nsu.fit.vinter.lab5.SOCKSproxy.DNS.DNSResolver;
import ru.nsu.fit.vinter.lab5.SOCKSproxy.Exceptions.*;

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

    private static final int BUFFER_SIZE = 1024;
    private static final int SOCKS_VERSION = 5;
    private static final int AUTH_TYPE = 0;
    private static final int ESTABLISH_CONNECTION_COMMAND = 1; // establish a TCP/IP stream connection
    private static final byte[] GREETING_REPLY = new byte[] {5, 0};
    ServerSocketChannel serverSocketChannel;
    Selector selector;
    DNSResolver dnsResolver;

    private int proxyPort;

    public Proxy(int port) {
        try {
            this.proxyPort = port;
            SocketAddress socket = new InetSocketAddress(InetAddress.getByName("localhost"), this.proxyPort);
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(socket);
            selector = Selector.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, serverSocketChannel.validOps());
            logger.info("Proxy is listening on port " + this.proxyPort);

        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Error while getting socket port number");
            closeProxy();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while creating resources");
            closeProxy();
        }
    }

    enum State {
        GREETING,
        // AUTHENTICATION_REQUEST,
        // AUTHENTICATION,
        CONNECTION,
        CONNECTED;
    }

    static class ConnectionContext {
        ByteBuffer in;
        ByteBuffer out;
        SelectionKey peer;
        State state;

        int addressType;
        int domainNameLength;
        InetAddress address;
        int port;
    }

    enum AddressType {
        IPv4(1),
        DomainName(3),
        IPv6(4);
        private int type;

        AddressType(int type) {
            this.type = (type);
        }

        public int getAddressType() {
            return type;
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
                    // get connection request
                    if (key.isAcceptable()) {
                        SocketChannel newChannel = ((ServerSocketChannel)key.channel()).accept();
                        newChannel.configureBlocking(false);
                        newChannel.register(key.selector(), SelectionKey.OP_READ);
                        logger.info("Get connection request");
                    }
                    // establish connection
                    else if (key.isConnectable()) {
                        logger.info("Trying to connect");
                        SocketChannel channel = ((SocketChannel) key.channel());
                        ConnectionContext connectionContext = ((ConnectionContext) key.attachment());

                        if (!channel.finishConnect()) {
                            throw new FailedConnectionException("Error at 'finishConnect'");
                        }

                        ConnectionContext peerConnectionContext = (ConnectionContext) connectionContext.peer.attachment();

                        byte[] address;
                        if (connectionContext.addressType == AddressType.DomainName.getAddressType()) {
                            ByteBuffer tmp = ByteBuffer.allocate(connectionContext.domainNameLength + 1);
                            tmp.put((byte) connectionContext.domainNameLength);
                            tmp.put(connectionContext.address.getAddress());
                            address = tmp.array();
                        }
                        else {
                            address = connectionContext.address.getAddress();
                        }
                        byte[] port = {((byte) (connectionContext.port >> 8)), ((byte) connectionContext.port)};
                        ByteBuffer connectionMessageReply = ByteBuffer.allocate(25);
                        connectionMessageReply.put(new byte[] {5, 0, 0});
                        connectionMessageReply.put(address);
                        connectionMessageReply.put(port);

                        peerConnectionContext.in.clear();
                        peerConnectionContext.in.put(connectionMessageReply.array()).flip();
                        logger.info("The connection message was replied :: " + Arrays.toString(connectionMessageReply.array()));

                        connectionContext.out = peerConnectionContext.in;
                        peerConnectionContext.out = connectionContext.in;

                        key.interestOps(SelectionKey.OP_WRITE);
                        connectionContext.peer.interestOps(SelectionKey.OP_WRITE);

                        connectionContext.state = State.CONNECTED;
                        peerConnectionContext.state = State.CONNECTED;

                        logger.info("Connected successfully");
                    }

                    else if (key.isReadable()) {
                        SocketChannel channel = ((SocketChannel) key.channel());
                        ConnectionContext connectionContext = ((ConnectionContext) key.attachment());

                        if (connectionContext == null) {
                            key.attach(connectionContext = new ConnectionContext());
                            connectionContext.in = ByteBuffer.allocate(BUFFER_SIZE);
                            connectionContext.state = State.GREETING;
                        }

                        logger.info("read");
                        if (channel.read(connectionContext.in) < 1) {
                            closeKey(key);
                            logger.log(Level.WARNING, "Error while reading - closing the connection");
                        }

                        else if (connectionContext.state == State.GREETING) {
                            logger.info("Start reading greeting message");
                            readGreetingMessage(key, connectionContext);

                        } else if (connectionContext.state == State.CONNECTION) {
                            logger.info("Start reading connection message");
                            readConnectionMessage(key, connectionContext);

                        } else if (connectionContext.state == State.CONNECTED && connectionContext.peer != null){
                            connectionContext.peer.interestOps(connectionContext.peer.interestOps() | SelectionKey.OP_WRITE);
                            key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
                            connectionContext.in.flip();
                        }
                    }

                    else if (key.isWritable()) {
                        SocketChannel channel = ((SocketChannel) key.channel());
                        ConnectionContext connectionContext = ((ConnectionContext) key.attachment());
                        logger.info("writing");

                        ByteBuffer msgToWrite;
                        if (connectionContext.state == State.CONNECTED) {
                            msgToWrite = connectionContext.out;
                        }
                        else {
                            msgToWrite = connectionContext.in;
                        }
                        //logger.info(Arrays.toString(msgToWrite.array()));

                        if (channel.write(msgToWrite) == -1) {
                            closeKey(key);
                            logger.log(Level.WARNING, "Error while writing - closing the connection");
                        }
                        else if (msgToWrite.remaining() == 0) {
                            if (connectionContext.state == State.GREETING) {
                                logger.info("The greeting was replied :: " + Arrays.toString(connectionContext.in.array()));
                                connectionContext.state = State.CONNECTION;
                            }
                            msgToWrite.clear();
                            if (connectionContext.peer != null)
                                connectionContext.peer.interestOps(connectionContext.peer.interestOps() | SelectionKey.OP_READ);
                            key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
                        }

                        logger.info("finished writing");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
//            logger.log(Level.SEVERE, "IOException at select :: " + e.getMessage());
        } catch (BadRequestException | UnsupportedAuthenticationCodeException | UnsupportedCommandCodeException  | WrongProtocolVersion | FailedConnectionException e) {
            logger.info(e.getMessage());
        }
    }

    void readGreetingMessage(SelectionKey key, ConnectionContext connectionContext) throws IOException, BadRequestException, WrongProtocolVersion, UnsupportedAuthenticationCodeException {
        byte[] greetingMessageHeader = connectionContext.in.array();
        logger.info("Got greeting message :: " + Arrays.toString(greetingMessageHeader));

        if (greetingMessageHeader[0] != SOCKS_VERSION) {
            logger.log(Level.SEVERE, "Got wrong protocol version");
            closeKey(key);
            throw new WrongProtocolVersion("Wrong protocol version");
        }
        // we do not need to realize an authentication in this lab
        else if (greetingMessageHeader[2] != AUTH_TYPE) {
            logger.log(Level.SEVERE, "Got unsupported authentication code");
            closeKey(key);
            throw new UnsupportedAuthenticationCodeException("Unsupported authentication code");
        }
        else if (connectionContext.in.position() < 3) {
            logger.log(Level.SEVERE, "Bad request");
            closeKey(key);
            throw new BadRequestException("Bad request");
        }
        else {
            key.interestOps(SelectionKey.OP_WRITE);
            connectionContext.in.clear();
            connectionContext.in.put(GREETING_REPLY).flip();

            logger.info("The greeting was successful");
        }
    }

    void readConnectionMessage(SelectionKey key, ConnectionContext connectionContext) throws IOException, BadRequestException, WrongProtocolVersion, UnsupportedCommandCodeException, FailedConnectionException {
        byte[] connectionMessageHeader = connectionContext.in.array();
        logger.info("Got connection message :: " + Arrays.toString(connectionMessageHeader));

        int addressType = connectionMessageHeader[3];
        int msgSize = 6;
        if (addressType == AddressType.IPv4.getAddressType()) {
            msgSize += 4;
        }
        else if (addressType == AddressType.DomainName.getAddressType()) {
            msgSize += 1 + connectionMessageHeader[4];
        }
        else if (addressType == AddressType.IPv6.getAddressType()) {
            msgSize += 16;
        }
        else {
            logger.log(Level.SEVERE, "Bad request");
            closeKey(key);
            throw new BadRequestException("Bad request");
        }

        if (connectionMessageHeader[0] != SOCKS_VERSION) {
            logger.log(Level.SEVERE, "Got wrong protocol version");
            closeKey(key);
            throw new WrongProtocolVersion("Wrong protocol version");
        }
        if (connectionMessageHeader[1] != ESTABLISH_CONNECTION_COMMAND) {
            logger.log(Level.SEVERE, "Got unsupported command code");
            closeKey(key);
            throw new UnsupportedCommandCodeException("Unsupported command code");
        }
        if (connectionContext.in.position() < msgSize) {
            logger.log(Level.SEVERE, "Bad request");
            closeKey(key);
            throw new BadRequestException("Bad request");
        }
        else {
            InetAddress address;
            int port;

            logger.info("address type :: " + addressType);

            if (addressType == AddressType.IPv4.getAddressType()) {
                address = InetAddress.getByAddress(Arrays.copyOfRange(connectionMessageHeader, 4, 8));
                port = (((0xFF & connectionMessageHeader[8]) << 8) + (0xFF & connectionMessageHeader[9]));
                logger.info("address :: " + address.getCanonicalHostName());
            }

            else if (addressType == AddressType.DomainName.getAddressType()) {
                int domainNameLength = connectionMessageHeader[4];
                byte[] domainName = Arrays.copyOfRange(connectionMessageHeader, 5, 5 + domainNameLength);
                address = InetAddress.getByName(new String(domainName));
                port  = (((0xFF & (5 + domainNameLength)) << 8) + (0xFF & (5 + domainNameLength + 1)));
                logger.info("address :: " + new String(domainName));
            }

            else if (addressType == AddressType.IPv6.getAddressType()) {
                address = InetAddress.getByAddress(Arrays.copyOfRange(connectionMessageHeader, 4, 20));
                port = (((0xFF & connectionMessageHeader[20]) << 8) + (0xFF & connectionMessageHeader[21]));
                logger.info("address :: " + address.getCanonicalHostName());
            }
            else {
                throw new BadRequestException("Bad request");
            }

            logger.info("port :: " + port);

            key.interestOps(0);

            SocketChannel peer = SocketChannel.open();
            peer.configureBlocking(false);
            SelectionKey peerKey = peer.register(key.selector(), SelectionKey.OP_CONNECT);
            if (peer.connect(new InetSocketAddress(address, port))) {
                logger.info("immediately connected");
            }
            ConnectionContext peerConnectionContext = new ConnectionContext();
            peerConnectionContext.in = ByteBuffer.allocate(BUFFER_SIZE);
            peerConnectionContext.peer = key;
            peerKey.attach(peerConnectionContext);
            connectionContext.peer = peerKey;

            connectionContext.addressType = addressType;
            peerConnectionContext.addressType = addressType;
            connectionContext.address = address;
            peerConnectionContext.address = address;
            connectionContext.port = port;
            peerConnectionContext.port = port;
            if (addressType == AddressType.DomainName.getAddressType()) {
                connectionContext.domainNameLength = peerConnectionContext.domainNameLength = connectionMessageHeader[4];
            }
            logger.info("Connection request was process successfully");
        }
    }

    void closeKey(SelectionKey key) throws IOException {
        key.cancel();
        key.channel().close();
        SelectionKey peer = ((ConnectionContext) key.attachment()).peer;
        if (peer != null) {
            ((ConnectionContext) peer.attachment()).peer = null;
            if ((peer.interestOps() & SelectionKey.OP_WRITE) == 0) {
                ((ConnectionContext) peer.attachment()).in.flip();
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
