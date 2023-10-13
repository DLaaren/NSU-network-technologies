package main.java.ru.nsu.fit.vinter.lab1.multicastUDP;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OtherAppsDetector {
    private static final Logger logger = Logger.getLogger(OtherAppsDetector.class.getName());

    private InetAddress address;
    private int port;
    private int messageInterval;
    private int ttl;
    private HashMap<String, Long> lastMessages = new HashMap<>();

    private final String DEFAULT_MESSAGE = "";

    public OtherAppsDetector(InetAddress address, int port, int messageInterval, int ttl) {
        this.address = address;
        this.port = port;
        this.messageInterval = messageInterval;
        this.ttl = ttl;
    }

    String getAddressAndPort(InetAddress address, int port) {
        return address + ":" + port;
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private void sendMessage(DatagramSocket datagramSocket, String message) throws IOException {
        byte[] buf = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        datagramSocket.send(packet);
    }

    private void removeUnavailable(long currentTimeMillis) {
        for (Iterator<Map.Entry<String, Long>> iterator = this.lastMessages.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, Long> entry = iterator.next();
            if (currentTimeMillis - entry.getValue() > this.ttl) {
                logger.info("App with ID = " + entry.getKey() + " has been disconnected");
                iterator.remove();
            }
        }
    }

    public void run() throws IOException {
        final MulticastSocket recvSocket = new MulticastSocket(port);
        final DatagramSocket sendSocket = new DatagramSocket();
        byte[] buf = new byte[1024];
        recvSocket.setSoTimeout(this.messageInterval);
        recvSocket.joinGroup(this.address);
        try (recvSocket; sendSocket) {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    recvSocket.receive(packet);
                } catch (SocketTimeoutException ex) {
                    sendMessage(sendSocket, DEFAULT_MESSAGE);
                    continue;
                }
                String appID = getAddressAndPort(packet.getAddress(), packet.getPort());
                if (!lastMessages.containsKey(appID)) {
                    logger.info("App with ID = " + appID + " has been detected");
                }
                removeUnavailable(getCurrentTime());
                lastMessages.put(appID, getCurrentTime());
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
        }
    }
}