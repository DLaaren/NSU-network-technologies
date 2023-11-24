package ru.nsu.fit.vinter.lab5.SOCKSproxy.Exceptions;

public class UnsupportedAuthenticationCodeException extends Exception {
    public UnsupportedAuthenticationCodeException(String message){
        super(message);
    }
}
