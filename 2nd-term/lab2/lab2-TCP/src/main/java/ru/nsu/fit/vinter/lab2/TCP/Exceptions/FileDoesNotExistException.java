package main.java.ru.nsu.fit.vinter.lab2.TCP.Exceptions;

public class FileDoesNotExistException extends Exception{
    public FileDoesNotExistException(String message){
        super(message);
    }
}
