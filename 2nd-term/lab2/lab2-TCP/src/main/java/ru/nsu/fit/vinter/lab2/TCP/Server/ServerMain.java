package main.java.ru.nsu.fit.vinter.lab2.TCP.Server;

import main.java.ru.nsu.fit.vinter.lab2.TCP.exceptions.WrongArgumentsException;

public class ServerMain {
    public static void main(String[] args) throws WrongArgumentsException {
        if (args.length != 1){
            throw new WrongArgumentsException("Expected arguments: port");
        }
        int port;
        try{
            port = Integer.parseInt(args[0], 10);
        }
        catch (NumberFormatException ex){
            throw new WrongArgumentsException("Cant parse port: " + args[0]);
        }
        Server server = new Server(port);
        server.run();
    }
}
