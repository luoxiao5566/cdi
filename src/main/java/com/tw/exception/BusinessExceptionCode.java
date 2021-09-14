package com.tw.exception;

public enum BusinessExceptionCode {
    CIRCULAR_REFERENCE("循环依赖"),
    AMBIGUOUS_IMPLEMENTATION_CLASS("该类依赖存在多个实现，无法注入");

    private final String message;

    BusinessExceptionCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
