package main.java.ru.nsu.fit.vinter.lab5.SOCKSproxy.Exceptions;

public class WrongProtocolVersion extends Exception {
    public WrongProtocolVersion(String message){
        super(message);
    }
}
