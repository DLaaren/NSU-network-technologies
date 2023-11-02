package main.java.ru.nsu.fit.vinter.lab2.TCP.Client;

import main.java.ru.nsu.fit.vinter.lab2.TCP.Exceptions.FileDoesNotExistException;
import main.java.ru.nsu.fit.vinter.lab2.TCP.Exceptions.WrongArgumentsException;

import java.io.IOException;
import java.net.*;

public class ClientMain {
    public static void main(String[] args) throws WrongArgumentsException, IOException, FileDoesNotExistException {
        if (args.length < 3){
            throw new WrongArgumentsException("Expected arguments: path to file, ip-address(or dns-name), port");
        }
        String fileName = args[0];
        String address = args[1];
        int port;
        try{
            port = Integer.parseInt(args[2], 10);
        }
        catch (NumberFormatException ex){
            throw new WrongArgumentsException("Cant parse port: " + args[2]);
        }
        Client client = new Client(fileName, InetAddress.getByName(address), port);
        client.run();
    }
}
