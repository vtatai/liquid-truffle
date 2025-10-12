package io.github.liquidTruffle.lexer;

public class LexerException extends RuntimeException {
    public LexerException(String message) {
        super(message);
    }
    
    public LexerException(String message, Throwable cause) {
        super(message, cause);
    }
}