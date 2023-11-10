package main.java.ru.nsu.fit.vinter.lab2.TCP.Server;

import main.java.ru.nsu.fit.vinter.lab2.TCP.Exceptions.UnknownResponseCodeException;

public enum ResponseCode {
    SUCCESS_HEADER_TRANSFER(201),
    SUCCESS_FILE_TRANSFER(202),
    FAILURE_HEADER_TRANSFER(101),
    FAILURE_FILE_TRANSFER(102);

    private final int code;

    ResponseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ResponseCode getResponseByCode(int code) throws UnknownResponseCodeException {
        for (ResponseCode responseCode: ResponseCode.values()){
            if (responseCode.code == code){
                return responseCode;
            }
        }
        throw new UnknownResponseCodeException("No response code = " + code);
    }
}
