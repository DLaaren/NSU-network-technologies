package main.java.ru.nsu.fit.vinter.lab2.TCP.Exceptions;

public class UnknownResponseCodeException extends Exception{
    public UnknownResponseCodeException(String message){
        super(message);
    }
}
