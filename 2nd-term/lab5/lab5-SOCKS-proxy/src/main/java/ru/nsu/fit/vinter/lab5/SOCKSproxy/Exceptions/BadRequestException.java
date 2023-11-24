package ru.nsu.fit.vinter.lab5.SOCKSproxy.Exceptions;

public class BadRequestException extends Exception  {
    public BadRequestException(String message){
        super(message);
    }
}
