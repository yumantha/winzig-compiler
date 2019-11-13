package com.yumantha.errors;

public class CompilerError extends RuntimeException {
    public CompilerError(String message) {
        super(message);
    }

    public CompilerError(String message, Throwable cause) {
        super(message, cause);
    }


}
