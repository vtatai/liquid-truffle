package io.github.liquidTruffle.lexer

enum class TokenType {
	TEXT,
	VAR_OPEN,      // {{
	VAR_CLOSE,     // }}
	TAG_OPEN,      // {%
	TAG_CLOSE,     // %}
	IDENT,
	STRING,
	NUMBER,
	PIPE,          // |
	COLON,         // :
	COMMA,         // ,
    DOT,           // .
	WHITESPACE,
	EOF,
}
