package main.java.ru.nsu.fit.vinter.lab1.multicastUDP;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private static final int DEFAULT_MESSAGE_INTERVAL = 100;
    private static final int DEFAULT_TTL = 1000;
    private static final int DEFAULT_PORT = 1333;

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            logger.log(Level.SEVERE, "usage : multicastIP port messageInterval ttl");
            System.exit(-1);
        }
        String multicastIP = args[0];
        InetAddress address = InetAddress.getByName(multicastIP);
        if (!address.isMulticastAddress()) {
            logger.log(Level.SEVERE, "IP = " + multicastIP + " is not an multicast address");
            System.exit(-2);
        }

        int port = DEFAULT_PORT;
        try {
            if (args.length > 1) {
                port = Integer.parseInt(args[1]);
            }
        } catch (NumberFormatException ex) {
            logger.warning("Wrong port format");
            logger.info("Port has default value = " + DEFAULT_PORT);
        }

        int messageInterval = DEFAULT_MESSAGE_INTERVAL;
        try {
            if (args.length > 2) {
                messageInterval = Integer.parseInt(args[2]);
            }
        } catch (NumberFormatException ex) {
            logger.warning("Wrong messageInterval format");
            logger.info("MessageInterval has default value = " + DEFAULT_MESSAGE_INTERVAL);
        }
        if (messageInterval < 0) {
            logger.info("MessageInterval cannot be negative");
            messageInterval = DEFAULT_MESSAGE_INTERVAL;
            logger.info("MessageInterval has default value = " + DEFAULT_MESSAGE_INTERVAL);
        }

        int ttl = DEFAULT_TTL;
        try {
            if (args.length > 3) {
                ttl = Integer.parseInt(args[3]);
            }
        } catch (NumberFormatException ex) {
            logger.warning("Wrong ttl format");
            logger.info("Ttl has default value = " + DEFAULT_TTL);
        }
        if (ttl < messageInterval) {
            logger.info("Ttl cannot be less than messageInterval");
            messageInterval = DEFAULT_TTL;
            logger.info("Ttl has default value = " + DEFAULT_TTL);
        }

        OtherAppsDetector otherAppsDetector = new OtherAppsDetector(address, port, messageInterval, ttl);
        otherAppsDetector.run();
    }
}