package main.java.ru.nsu.fit.vinter.lab2.TCP.exceptions;

public class UnknownResponseCode extends Exception{
    public UnknownResponseCode(String message){
        super(message);
    }
}
