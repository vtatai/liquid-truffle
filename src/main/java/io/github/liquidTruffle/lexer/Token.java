package io.github.liquidTruffle.lexer;

public record Token(TokenType type, String lexeme, int line, int start, int end) {

    @Override
    public String toString() {
        return type + "(" + lexeme + ")@" + line + ":" + start + ".." + end;
    }
}