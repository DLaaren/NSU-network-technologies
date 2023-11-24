package ru.nsu.fit.vinter.lab5.SOCKSproxy.Exceptions;

public class FailedConnectionException extends Exception {
    public FailedConnectionException(String message){
        super(message);
    }
}
