package main.java.ru.nsu.fit.vinter.lab5.SOCKSproxy.Exceptions;

public class UnsupportedCommandInProtocolException extends Exception {
    public UnsupportedCommandInProtocolException(String message){
        super(message);
    }
}
