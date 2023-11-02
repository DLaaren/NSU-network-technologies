package main.java.ru.nsu.fit.vinter.lab2.TCP.Exceptions;

public class UnknownResponseCode extends Exception{
    public UnknownResponseCode(String message){
        super(message);
    }
}
