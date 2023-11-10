package main.java.ru.nsu.fit.vinter.lab5.SOCKSproxy.Proxy;

import main.java.ru.nsu.fit.vinter.lab5.SOCKSproxy.Exceptions.WrongArgumentsException;

import java.io.IOException;
import java.util.logging.Logger;

public class ProxyMain {

    private static final Logger logger = Logger.getLogger(ProxyMain.class.getName());
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new WrongArgumentsException("Expected arguments: port");
        }
        int port;
        try {
            port = Integer.parseInt(args[0], 10);
        } catch (NumberFormatException ex) {
            throw new WrongArgumentsException("Cant parse port: " + args[0]);
        }
        Proxy proxy = new Proxy(port);
        proxy.run();
    }
}
