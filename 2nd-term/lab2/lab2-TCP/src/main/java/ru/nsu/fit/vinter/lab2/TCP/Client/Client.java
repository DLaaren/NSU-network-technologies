package main.java.ru.nsu.fit.vinter.lab2.TCP.Client;


// arg1 = absolute/relative path to file
//  Длина имени файла не превышает 4096 байт в кодировке UTF-8. Размер файла не более 1 терабайта.
// arg2 = ip-address (or dns-name)
// arg3 = port
// show if the data transmission was successful

import main.java.ru.nsu.fit.vinter.lab2.TCP.Server.ResponseCode;
import main.java.ru.nsu.fit.vinter.lab2.TCP.Exceptions.FileDoesNotExistException;
import main.java.ru.nsu.fit.vinter.lab2.TCP.Exceptions.UnknownResponseCodeException;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements Runnable {
    private static final Logger logger = Logger.getLogger(Client.class.getName());

    private final int BUF_SIZE = 1024;
    private final Path filePath;
    private final InetAddress address;
    private final int port;

    public Client(String path, InetAddress address, int port) throws FileDoesNotExistException {
        this.filePath = getFilePath(path);
        this.address = address;
        this.port = port;
    }

    private Path getFilePath(String pathStr) throws FileDoesNotExistException {
        Path tmpPath = Paths.get(pathStr);
        if (!Files.exists(tmpPath)){
            throw new FileDoesNotExistException(pathStr + " does not exist");
        }
        return tmpPath;
    }

    @Override
    public void run() {
        try(Socket socket = new Socket(this.address, this.port)) {
            InputStream fileReader = Files.newInputStream(this.filePath);
            DataOutputStream socketWriter = new DataOutputStream(socket.getOutputStream());
            DataInputStream socketReader = new DataInputStream(socket.getInputStream());

            // defining FTP header
            String protocolName = "FTP";
            String fileName = this.filePath.getFileName().toString();
            long fileSize = Files.size(this.filePath);

            socketWriter.writeUTF(protocolName);
            socketWriter.writeUTF(fileName);
            socketWriter.writeLong(fileSize);
            socketWriter.flush();

            // checking response
            ResponseCode responseCode = ResponseCode.getResponseByCode(socketReader.readInt());
            if (responseCode == ResponseCode.FAILURE_HEADER_TRANSFER){
                logger.log(Level.SEVERE, "Failure header transfer");
                return;
            }
            logger.info("Success header transfer");

            // sending file content
            byte[] buf = new byte[BUF_SIZE];
            int bytesRead;
            MessageDigest hashSum = MessageDigest.getInstance("MD5");
            while((bytesRead = fileReader.read(buf, 0, BUF_SIZE))> 0) {
                socketWriter.write(buf, 0, bytesRead);
                socketWriter.flush();
                hashSum.update(buf, 0, bytesRead);
            }
            socketWriter.writeUTF(hashSum.toString());
            socketWriter.flush();

            // checking response
            responseCode = ResponseCode.getResponseByCode(socketReader.readInt());
            if (responseCode == ResponseCode.FAILURE_HEADER_TRANSFER){
                logger.log(Level.SEVERE, "Failure file transfer");
            }
            logger.info("Success file transfer");

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnknownResponseCodeException unknownResponseCodeException) {
            logger.log(Level.SEVERE, "Get unknown response code from the server");
        }
    }
}
