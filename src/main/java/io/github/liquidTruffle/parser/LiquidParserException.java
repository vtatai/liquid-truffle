package io.github.liquidTruffle.parser;

import io.github.liquidTruffle.lexer.Token;

public class LiquidParserException extends RuntimeException {
    private final Token token;

    public LiquidParserException(String message) {
        this(message, null);
    }

    public LiquidParserException(String message, Token token) {
        super(message);
        this.token = token;
    }

    @Override
    public String toString() {
        if (token == null) {
            return "Error parsing: " + getMessage();
        }
        return "Error parsing at " + token + ": " + getMessage();
    }
}