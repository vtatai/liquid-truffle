package io.github.liquidTruffle.lexer;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class LexerTest {
    @Test
    public void lexerProducesTokens() {
        String src = "{{ name | upcase }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Basic verification that lexer produces tokens
        assertThat(tokens).isNotEmpty();
        
        // Assert for the correct order: VAR_OPEN, IDENT, PIPE, IDENT, VAR_CLOSE (no whitespace tokens)
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).containsExactly(
            TokenType.OBJECT_OPEN,
            TokenType.IDENT,      // name
            TokenType.PIPE,       // |
            TokenType.IDENT,      // upcase
            TokenType.OBJECT_CLOSE,
            TokenType.EOF
        );
        
        // Verify the actual lexemes
        assertThat(tokens.get(1).lexeme()).isEqualTo("name");
        assertThat(tokens.get(3).lexeme()).isEqualTo("upcase");
    }

    @Test
    public void lexerHandlesSimpleText() {
        String src = "Hello World";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        assertThat(tokens).hasSize(2);
        assertThat(tokens.getFirst().type()).isEqualTo(TokenType.TEXT);
        assertThat(tokens.getFirst().lexeme()).isEqualTo("Hello World");
        assertThat(tokens.getLast().type()).isEqualTo(TokenType.EOF);
    }

    @Test
    public void lexerHandlesVariableSyntax() {
        String src = "{{ variable }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        assertThat(tokens).hasSizeGreaterThanOrEqualTo(4);
        assertThat(tokens.get(0).type()).isEqualTo(TokenType.OBJECT_OPEN);
        assertThat(tokens.get(1).type()).isEqualTo(TokenType.IDENT);
        assertThat(tokens.get(1).lexeme()).isEqualTo("variable");
        assertThat(tokens.get(2).type()).isEqualTo(TokenType.OBJECT_CLOSE);
        assertThat(tokens.getLast().type()).isEqualTo(TokenType.EOF);
    }

    @Test
    public void lexerHandlesFilters() {
        String src = "{{ name | upcase | downcase }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Should have: VAR_OPEN, IDENT(name), PIPE, IDENT(upcase), PIPE, IDENT(downcase), VAR_CLOSE, EOF
        assertThat(tokens).hasSizeGreaterThanOrEqualTo(8);
        
        // Count pipes
        long pipeCount = tokens.stream().map(Token::type).filter(t -> t == TokenType.PIPE).count();
        assertThat(pipeCount).isEqualTo(2);
        
        // Count identifiers
        long identCount = tokens.stream().map(Token::type).filter(t -> t == TokenType.IDENT).count();
        assertThat(identCount).isEqualTo(3);
    }

    @Test
    public void lexerIgnoresWhitespaceTokensByDefault() {
        String src = "{{ variable }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Should have the other expected tokens
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).containsExactly(
            TokenType.OBJECT_OPEN,
            TokenType.IDENT,
            TokenType.OBJECT_CLOSE,
            TokenType.EOF
        );
    }

    @Test
    public void lexerHandlesTextWithoutWhitespaceTokens() {
        String src = "Hello World";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Should have IDENT tokens for "Hello" and "World"
        List<Token> textTokens = tokens.stream().filter(t -> t.type() == TokenType.TEXT).collect(Collectors.toList());
        assertThat(textTokens).hasSize(1);
        assertThat(textTokens.getFirst().lexeme()).isEqualTo("Hello World");
    }

    @Test
    public void lexerWorksWithReader() {
        String src = "{{ variable | filter }}";
        StringReader reader = new StringReader(src);
        Lexer lexer = new Lexer(reader);
        List<Token> tokens = lexer.lex();
        
        // Should produce the same tokens as String constructor
        Lexer stringLexer = new Lexer(src);
        List<Token> stringTokens = stringLexer.lex();
        
        assertThat(tokens).hasSameSizeAs(stringTokens);
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        List<TokenType> stringTokenTypes = stringTokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).isEqualTo(stringTokenTypes);
        
        List<String> lexemes = tokens.stream().map(Token::lexeme).collect(Collectors.toList());
        List<String> stringLexemes = stringTokens.stream().map(Token::lexeme).collect(Collectors.toList());
        assertThat(lexemes).isEqualTo(stringLexemes);
    }

    @Test
    public void lexerHandlesMultiLineInput() {
        String src = "{{ variable }}\nSome text\n{% if condition %}\nMore content\n{% endif %}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Should handle multi-line input correctly
        assertThat(tokens).isNotEmpty();
        assertThat(tokens.getLast().type()).isEqualTo(TokenType.EOF);
        
        // Should have all expected token types
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).contains(TokenType.OBJECT_OPEN);
        assertThat(tokenTypes).contains(TokenType.OBJECT_CLOSE);
        assertThat(tokenTypes).contains(TokenType.TAG_OPEN);
        assertThat(tokenTypes).contains(TokenType.TAG_CLOSE);
        assertThat(tokenTypes).contains(TokenType.TEXT);
    }

    @Test
    public void lexerRecognizesKeywords() {
        String src = "{% if condition %}Hello{% endif %}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Should recognize 'if' and 'endif' as keywords
        List<Token> keywordTokens = tokens.stream().filter(t -> t.type() == TokenType.KEYWORD).collect(Collectors.toList());
        assertThat(keywordTokens).hasSize(2);
        assertThat(keywordTokens.get(0).lexeme()).isEqualTo("if");
        assertThat(keywordTokens.get(1).lexeme()).isEqualTo("endif");
        
        // Should have other expected tokens
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).contains(TokenType.TAG_OPEN);
        assertThat(tokenTypes).contains(TokenType.TAG_CLOSE);
        assertThat(tokenTypes).contains(TokenType.IDENT); // "condition"
        assertThat(tokenTypes).contains(TokenType.TEXT); // "Hello"
    }

    @Test
    public void lexerRecognizesBooleanKeywords() {
        String src = "{% if true or false %}{{ nil }}{% endif %}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Should recognize 'if', 'true', 'or', 'false', 'nil', 'endif' as keywords
        List<Token> keywordTokens = tokens.stream().filter(t -> t.type() == TokenType.KEYWORD).collect(Collectors.toList());
        assertThat(keywordTokens).hasSize(6);
        List<String> keywordLexemes = keywordTokens.stream().map(Token::lexeme).collect(Collectors.toList());
        assertThat(keywordLexemes).containsExactly("if", "true", "or", "false", "nil", "endif");
    }

    @Test
    public void lexerThrowsErrorInObject() {
        String src = "{{ @ }}";
        Lexer lexer = new Lexer(src);
        Assertions.assertThatThrownBy(lexer::lex).isInstanceOf(LexerException.class);
    }

    @Test
    public void lexerHandlesFilterWithParameters() {
        String src = "{{ products | where: \"color\", \"red\" }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Should have: OBJECT_OPEN, IDENT(products), PIPE, IDENT(where), COLON, STRING("color"), COMMA, STRING("red"), OBJECT_CLOSE, EOF
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).containsExactly(
            TokenType.OBJECT_OPEN,
            TokenType.IDENT,      // products
            TokenType.PIPE,        // |
            TokenType.IDENT,       // where
            TokenType.COLON,       // :
            TokenType.STRING,      // "color"
            TokenType.COMMA,       // ,
            TokenType.STRING,      // "red"
            TokenType.OBJECT_CLOSE,
            TokenType.EOF
        );
        
        // Verify the actual lexemes
        assertThat(tokens.get(1).lexeme()).isEqualTo("products");
        assertThat(tokens.get(3).lexeme()).isEqualTo("where");
        assertThat(tokens.get(5).lexeme()).isEqualTo("color");
        assertThat(tokens.get(7).lexeme()).isEqualTo("red");
    }

    @Test
    public void lexerHandlesFilterWithNumericParameters() {
        String src = "{{ items | limit: 5 }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Should have: OBJECT_OPEN, IDENT(items), PIPE, IDENT(limit), COLON, NUMBER(5), OBJECT_CLOSE, EOF
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).containsExactly(
            TokenType.OBJECT_OPEN,
            TokenType.IDENT,      // items
            TokenType.PIPE,        // |
            TokenType.IDENT,       // limit
            TokenType.COLON,       // :
            TokenType.NUMBER,      // 5
            TokenType.OBJECT_CLOSE,
            TokenType.EOF
        );
        
        // Verify the actual lexemes
        assertThat(tokens.get(1).lexeme()).isEqualTo("items");
        assertThat(tokens.get(3).lexeme()).isEqualTo("limit");
        assertThat(tokens.get(5).lexeme()).isEqualTo("5");
    }

    @Test
    public void lexerHandlesMultipleFilterParameters() {
        String src = "{{ products | where: \"category\", \"electronics\" | sort: \"price\" }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Should have multiple filters with parameters
        long pipeCount = tokens.stream().map(Token::type).filter(t -> t == TokenType.PIPE).count();
        assertThat(pipeCount).isEqualTo(2);
        
        long colonCount = tokens.stream().map(Token::type).filter(t -> t == TokenType.COLON).count();
        assertThat(colonCount).isEqualTo(2);
        
        long commaCount = tokens.stream().map(Token::type).filter(t -> t == TokenType.COMMA).count();
        assertThat(commaCount).isEqualTo(1);
        
        // Verify specific tokens exist
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).contains(TokenType.IDENT); // products, where, sort
        assertThat(tokenTypes).contains(TokenType.STRING); // "category", "electronics", "price"
        assertThat(tokenTypes).contains(TokenType.COLON);
        assertThat(tokenTypes).contains(TokenType.COMMA);
    }

    @Test
    public void lexerThrowsErrorInTag() {
        String src = "{% @ %}";
        Lexer lexer = new Lexer(src);
        Assertions.assertThatThrownBy(lexer::lex).isInstanceOf(LexerException.class);
    }

    // Tests for new Liquid constructs

    @Test
    public void lexerHandlesArithmeticOperators() {
        String src = "{{ 5 + 3 * 2 - 1 / 4 % 2 }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).contains(TokenType.PLUS);
        assertThat(tokenTypes).contains(TokenType.MULTIPLY);
        assertThat(tokenTypes).contains(TokenType.MINUS);
        assertThat(tokenTypes).contains(TokenType.DIVIDE);
        assertThat(tokenTypes).contains(TokenType.MODULO);
    }

    @Test
    public void lexerHandlesExponentiation() {
        String src = "{{ 2 ** 3 }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).contains(TokenType.EXPONENT);
    }

    @Test
    public void lexerHandlesParentheses() {
        String src = "{{ (5 + 3) * 2 }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).contains(TokenType.LPAREN);
        assertThat(tokenTypes).contains(TokenType.RPAREN);
    }

    @Test
    public void lexerHandlesArraySyntax() {
        String src = "{{ [1, 2, 3] }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).contains(TokenType.LBRACKET);
        assertThat(tokenTypes).contains(TokenType.RBRACKET);
        assertThat(tokenTypes).contains(TokenType.COMMA);
    }

    @Test
    public void lexerHandlesHashSyntax() {
        String src = "{{ {name: 'John', age: 30} }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).contains(TokenType.LBRACE);
        assertThat(tokenTypes).contains(TokenType.RBRACE);
        assertThat(tokenTypes).contains(TokenType.COLON);
    }

    @Test
    public void lexerHandlesRangeSyntax() {
        String src = "{{ (1..5) }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).contains(TokenType.RANGE);
    }

    @Test
    public void lexerHandlesTernaryOperator() {
        String src = "{{ condition ? 'yes' : 'no' }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).contains(TokenType.QUESTION);
        assertThat(tokenTypes).contains(TokenType.COLON);
    }

    @Test
    public void lexerHandlesNegativeNumbers() {
        String src = "{{ -42 }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        List<Token> numberTokens = tokens.stream()
            .filter(t -> t.type() == TokenType.NUMBER)
            .collect(Collectors.toList());
        assertThat(numberTokens).hasSize(1);
        assertThat(numberTokens.getFirst().lexeme()).isEqualTo("-42");
    }

    @Test
    public void lexerHandlesFloatingPointNumbers() {
        String src = "{{ 3.14 }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        List<Token> floatTokens = tokens.stream()
            .filter(t -> t.type() == TokenType.FLOAT)
            .collect(Collectors.toList());
        assertThat(floatTokens).hasSize(1);
        assertThat(floatTokens.getFirst().lexeme()).isEqualTo("3.14");
    }

    @Test
    public void lexerHandlesNegativeFloatingPointNumbers() {
        String src = "{{ -3.14 }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        List<Token> floatTokens = tokens.stream()
            .filter(t -> t.type() == TokenType.FLOAT)
            .collect(Collectors.toList());
        assertThat(floatTokens).hasSize(1);
        assertThat(floatTokens.getFirst().lexeme()).isEqualTo("-3.14");
    }

    @Test
    public void lexerHandlesWhitespaceControl() {
        String src = "{{- variable -}}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).contains(TokenType.OBJECT_OPEN_WS);
        assertThat(tokenTypes).contains(TokenType.OBJECT_CLOSE_WS);
    }

    @Test
    public void lexerHandlesTagWhitespaceControl() {
        String src = "{%- if true -%}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).contains(TokenType.TAG_OPEN_WS);
        assertThat(tokenTypes).contains(TokenType.TAG_CLOSE_WS);
    }

    @Test
    public void lexerHandlesComments() {
        String src = "Hello {# This is a comment #} World";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Comments should be ignored (not produce tokens)
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).doesNotContain(TokenType.COMMENT_OPEN);
        assertThat(tokenTypes).doesNotContain(TokenType.COMMENT_CLOSE);
        
        // Should have text tokens for "Hello" and "World"
        List<Token> textTokens = tokens.stream()
            .filter(t -> t.type() == TokenType.TEXT)
            .collect(Collectors.toList());
        assertThat(textTokens).hasSize(2);
        assertThat(textTokens.get(0).lexeme()).isEqualTo("Hello ");
        assertThat(textTokens.get(1).lexeme()).isEqualTo(" World");
    }

    @Test
    public void lexerHandlesComplexExpression() {
        String src = "{{ (items | where: 'category', 'electronics' | limit: 5) | sort: 'price' }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).contains(TokenType.LPAREN);
        assertThat(tokenTypes).contains(TokenType.RPAREN);
        assertThat(tokenTypes).contains(TokenType.PIPE);
        assertThat(tokenTypes).contains(TokenType.COLON);
        assertThat(tokenTypes).contains(TokenType.COMMA);
        assertThat(tokenTypes).contains(TokenType.STRING);
    }

    @Test
    public void lexerHandlesArrayAccess() {
        String src = "{{ items[0] }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).contains(TokenType.LBRACKET);
        assertThat(tokenTypes).contains(TokenType.RBRACKET);
        assertThat(tokenTypes).contains(TokenType.NUMBER);
    }

    @Test
    public void lexerHandlesNestedExpressions() {
        String src = "{{ items[user.preferences[0]] | where: 'active', true }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        List<TokenType> tokenTypes = tokens.stream().map(Token::type).collect(Collectors.toList());
        assertThat(tokenTypes).contains(TokenType.LBRACKET);
        assertThat(tokenTypes).contains(TokenType.RBRACKET);
        assertThat(tokenTypes).contains(TokenType.DOT);
        assertThat(tokenTypes).contains(TokenType.PIPE);
        assertThat(tokenTypes).contains(TokenType.COLON);
    }
}