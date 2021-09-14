package com.tw.exception;

public class ContainerStartupException extends RuntimeException {
    public ContainerStartupException(String message) {
        super(message);
    }

    public ContainerStartupException(BusinessExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
    }
}
