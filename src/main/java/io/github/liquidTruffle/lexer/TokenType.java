package io.github.liquidTruffle.lexer;

public enum TokenType {
    TEXT,
    OBJECT_OPEN,      // {{
    OBJECT_CLOSE,     // }}
    TAG_OPEN,      // {%
    TAG_CLOSE,     // %}
    IDENT,
    KEYWORD,       // Reserved keywords like if, for, unless, etc.
    STRING,
    NUMBER,
    PIPE,          // |
    COLON,         // :
    COMMA,         // ,
    DOT,           // .
    GT,            // >
    LT,            // <
    GTE,           // >=
    LTE,           // <=
    EQ,            // ==
    NE,            // !=
    WHITESPACE,
    EOF
}