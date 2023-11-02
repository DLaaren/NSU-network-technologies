package main.java.ru.nsu.fit.vinter.lab2.TCP.Server;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;


// arg1 = port
// check if the size of gotten data is equal of sent data and report to client
// if there is a mistake then disconnect this client
// show traffic speed once and average (за сеанс) in 3 secs per client
// even if client has been connected for less than 3 secs then still show the info

public class Server implements Runnable {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private final int port;

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try(ServerSocket serverSocket = new ServerSocket(this.port)) {
            logger.info("Server has started on port - " + this.port);
            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("Client connected - " + socket.getInetAddress() + ":" + socket.getPort());
                Thread clientHandler = new Thread((Runnable) new ClientRequestHandler(socket));
                clientHandler.start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cant open server socket on port: " + this.port);
        }
    }
}
