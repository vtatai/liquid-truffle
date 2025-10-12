package io.github.liquidTruffle.lexer;

public class Token {
    private final TokenType type;
    private final String lexeme;
    private final int start;
    private final int end;

    public Token(TokenType type, String lexeme, int start, int end) {
        this.type = type;
        this.lexeme = lexeme;
        this.start = start;
        this.end = end;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return type + "(" + lexeme + ")@" + start + ".." + end;
    }
}