package main.java.ru.nsu.fit.vinter.lab2.TCP.Server;

import main.java.ru.nsu.fit.vinter.lab2.TCP.Exceptions.FileNameExcessException;
import main.java.ru.nsu.fit.vinter.lab2.TCP.Exceptions.FileSizeExcessException;
import main.java.ru.nsu.fit.vinter.lab2.TCP.Exceptions.UnknownProtocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientRequestHandler implements Runnable{
    private static final Logger logger = Logger.getLogger(ClientRequestHandler.class.getName());
    private final int BUF_SIZE = 1024;
    private final int SPEED_TEST_INTERVAL = 3000;
    private final int MAX_FILENAME_LENGTH = 4096;
    private final long MAX_FILE_SIZE = 1_099_511_627_776L;

    private final Socket socket;

    public ClientRequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream clientReader = new DataInputStream(socket.getInputStream());
             DataOutputStream clientWriter = new DataOutputStream(socket.getOutputStream());
             socket) {

            // reading the header
            String protocolName = clientReader.readUTF();
            if (protocolName.equals("FTP")) {
                throw new UnknownProtocol("Got unknown protocol name");
            }
            String fileName = clientReader.readUTF();
            long fileSize = clientReader.readLong();
            if (fileName.contains("/..")) {
                throw new SecurityException("File name contains \"/..\"");
            }
            if (fileName.length() > MAX_FILENAME_LENGTH) {
                throw new FileNameExcessException("Got file name greater than MAX_FILENAME_SIZE");
            }
            if (fileSize > MAX_FILE_SIZE) {
                throw new FileSizeExcessException("Got file size larger than MAX_FILE_SIZE");
            }

            // response
            sendFeedback(clientWriter, ResponseCode.SUCCESS_HEADER_TRANSFER);

            // reading file content
            Path filePath = createFile(fileName);
            try(OutputStream fileWriter = Files.newOutputStream(filePath)) {
                long initTime = System.currentTimeMillis();
                long lastTime = initTime;
                long prevReadBytes = 0;
                boolean clientWasActiveLessSpeedInterval = true;
                byte[] buf = new byte[BUF_SIZE];
                int sumBytesRead = 0;
                MessageDigest hashSum = MessageDigest.getInstance("MD5");
                while (sumBytesRead < fileSize) {
                    int readBytes;
                    if ((readBytes = clientReader.read(buf, 0, BUF_SIZE)) >= 0) {
                        fileWriter.write(buf, 0, readBytes);
                        hashSum.update(buf, 0, readBytes);
                    }
                    sumBytesRead += readBytes;

                    // checking avgSpeed
                    long currTime = System.currentTimeMillis();
                    if (currTime - lastTime >= SPEED_TEST_INTERVAL) {
                        long currSpeed = (sumBytesRead - prevReadBytes) * 1000 / (currTime - lastTime);
                        long avgSpeed = sumBytesRead * 1000L / (currTime - initTime);
                        logger.info("current speed = " + currSpeed + " bytes/sec");
                        logger.info("average speed = " + avgSpeed + " bytes/sec");
                        lastTime = currTime;
                        prevReadBytes = sumBytesRead;
                        clientWasActiveLessSpeedInterval = false;
                    }

                }
                if (clientWasActiveLessSpeedInterval) {
                    long speed = sumBytesRead * 1000L / (System.currentTimeMillis() - lastTime);
                    logger.info("speed = " + speed + " bytes/sec");
                }

                ResponseCode responseCode = ResponseCode.SUCCESS_FILE_TRANSFER;

                // check hashSum
                String resultHashSum = hashSum.toString();
                String hashSumCheck = clientReader.readUTF();
                if (!resultHashSum.equals(hashSumCheck)) {
                    responseCode = ResponseCode.FAILURE_FILE_TRANSFER;
                }

                // response
                sendFeedback(clientWriter, responseCode);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnknownProtocol unknownProtocol) {
            logger.log(Level.SEVERE, "Got unknown protocol name from the server");
        } catch (SecurityException securityException) {
            logger.log(Level.SEVERE, "Got filename with \"/..\"");
        } catch (FileNameExcessException fileNameExcessException) {
            logger.log(Level.SEVERE, "Got file name greater than MAX_FILENAME_SIZE");
        } catch (FileSizeExcessException fileSizeExcessException) {
            logger.log(Level.SEVERE, "Got file size larger than MAX_FILE_SIZE");
        } finally {
            logger.info("Client disconnected - " + socket.getInetAddress() + ":" +  socket.getPort());
        }
    }

    Path createFile(String fileName) throws IOException {
        Path dirPath = Paths.get("uploads");
        if (!Files.exists(dirPath)) {
            Files.createDirectory(dirPath);
        }
        Path filePath = Paths.get(dirPath + "/" + fileName);
        Random random = new Random();
        while (Files.exists(filePath)) {
            filePath = Paths.get(dirPath + "/" + fileName + Math.abs(random.nextInt()));
        }
        Files.createFile(filePath);
        return filePath;
    }

    void sendFeedback(DataOutputStream outputStream, ResponseCode code) throws IOException {
        outputStream.writeInt(code.getCode());
        outputStream.flush();
    }

}
