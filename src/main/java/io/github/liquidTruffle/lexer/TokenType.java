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
    
    // Arithmetic operators
    PLUS,          // +
    MINUS,         // -
    MULTIPLY,      // *
    DIVIDE,        // /
    MODULO,        // %
    EXPONENT,      // **
    
    // Parentheses and brackets
    LPAREN,        // (
    RPAREN,        // )
    LBRACKET,      // [
    RBRACKET,      // ]
    LBRACE,        // {
    RBRACE,        // }
    
    
    // Comments
    COMMENT_OPEN,  // {#
    COMMENT_CLOSE, // #}
    
    // Whitespace control variants
    OBJECT_OPEN_WS,    // {{-
    OBJECT_CLOSE_WS,   // -}}
    TAG_OPEN_WS,       // {%-
    TAG_CLOSE_WS,      // -%}
    
    // Ternary operator
    QUESTION,      // ?
    
    // Range operator
    RANGE,         // ..
    
    // Floating point numbers
    FLOAT,
    
    EOF
}