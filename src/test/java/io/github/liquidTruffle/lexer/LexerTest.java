package io.github.liquidTruffle.lexer;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

public class LexerTest {
    @Test
    public void lexerProducesTokens() {
        String src = "{{ name | upcase }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Basic verification that lexer produces tokens
        Assertions.assertThat(tokens).isNotEmpty();
        
        // Assert for the correct order: VAR_OPEN, IDENT, PIPE, IDENT, VAR_CLOSE (no whitespace tokens)
        List<TokenType> tokenTypes = tokens.stream().map(Token::getType).collect(Collectors.toList());
        Assertions.assertThat(tokenTypes).containsExactly(
            TokenType.VAR_OPEN,
            TokenType.IDENT,      // name
            TokenType.PIPE,       // |
            TokenType.IDENT,      // upcase
            TokenType.VAR_CLOSE,
            TokenType.EOF
        );
        
        // Verify the actual lexemes
        Assertions.assertThat(tokens.get(1).getLexeme()).isEqualTo("name");
        Assertions.assertThat(tokens.get(3).getLexeme()).isEqualTo("upcase");
    }

    @Test
    public void lexerHandlesSimpleText() {
        String src = "Hello World";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        Assertions.assertThat(tokens).hasSize(2);
        Assertions.assertThat(tokens.get(0).getType()).isEqualTo(TokenType.TEXT);
        Assertions.assertThat(tokens.get(0).getLexeme()).isEqualTo("Hello World");
        Assertions.assertThat(tokens.get(tokens.size() - 1).getType()).isEqualTo(TokenType.EOF);
    }

    @Test
    public void lexerHandlesVariableSyntax() {
        String src = "{{ variable }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        Assertions.assertThat(tokens).hasSizeGreaterThanOrEqualTo(4);
        Assertions.assertThat(tokens.get(0).getType()).isEqualTo(TokenType.VAR_OPEN);
        Assertions.assertThat(tokens.get(1).getType()).isEqualTo(TokenType.IDENT);
        Assertions.assertThat(tokens.get(1).getLexeme()).isEqualTo("variable");
        Assertions.assertThat(tokens.get(2).getType()).isEqualTo(TokenType.VAR_CLOSE);
        Assertions.assertThat(tokens.get(tokens.size() - 1).getType()).isEqualTo(TokenType.EOF);
    }

    @Test
    public void lexerHandlesFilters() {
        String src = "{{ name | upcase | downcase }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Should have: VAR_OPEN, IDENT(name), PIPE, IDENT(upcase), PIPE, IDENT(downcase), VAR_CLOSE, EOF
        Assertions.assertThat(tokens).hasSizeGreaterThanOrEqualTo(8);
        
        // Count pipes
        long pipeCount = tokens.stream().map(Token::getType).filter(t -> t == TokenType.PIPE).count();
        Assertions.assertThat(pipeCount).isEqualTo(2);
        
        // Count identifiers
        long identCount = tokens.stream().map(Token::getType).filter(t -> t == TokenType.IDENT).count();
        Assertions.assertThat(identCount).isEqualTo(3);
    }

    @Test
    public void lexerIgnoresWhitespaceTokensByDefault() {
        String src = "{{ variable }}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Should not report whitespace tokens by default
        boolean hasWhitespace = tokens.stream().anyMatch(t -> t.getType() == TokenType.WHITESPACE);
        Assertions.assertThat(hasWhitespace).isFalse();
        
        // Should have the other expected tokens
        List<TokenType> tokenTypes = tokens.stream().map(Token::getType).collect(Collectors.toList());
        Assertions.assertThat(tokenTypes).containsExactly(
            TokenType.VAR_OPEN,
            TokenType.IDENT,
            TokenType.VAR_CLOSE,
            TokenType.EOF
        );
    }

    @Test
    public void lexerReportsWhitespaceTokensWhenEnabled() {
        String src = "{{ variable }}";
        Lexer lexer = new Lexer(src, true);
        List<Token> tokens = lexer.lex();
        
        // Should report whitespace tokens when explicitly enabled
        boolean hasWhitespace = tokens.stream().anyMatch(t -> t.getType() == TokenType.WHITESPACE);
        Assertions.assertThat(hasWhitespace).isTrue();
        
        // Should have the expected tokens including whitespace
        List<TokenType> tokenTypes = tokens.stream().map(Token::getType).collect(Collectors.toList());
        Assertions.assertThat(tokenTypes).containsExactly(
            TokenType.VAR_OPEN,
            TokenType.WHITESPACE,
            TokenType.IDENT,
            TokenType.WHITESPACE,
            TokenType.VAR_CLOSE,
            TokenType.EOF
        );
        
        // Should have more tokens than default behavior
        Lexer defaultLexer = new Lexer(src);
        List<Token> defaultTokens = defaultLexer.lex();
        Assertions.assertThat(tokens).hasSizeGreaterThan(defaultTokens.size());
    }

    @Test
    public void lexerHandlesTextWithoutWhitespaceTokens() {
        String src = "Hello World";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Should not report whitespace tokens
        boolean hasWhitespace = tokens.stream().anyMatch(t -> t.getType() == TokenType.WHITESPACE);
        Assertions.assertThat(hasWhitespace).isFalse();
        
        // Should have IDENT tokens for "Hello" and "World"
        List<Token> textTokens = tokens.stream().filter(t -> t.getType() == TokenType.TEXT).collect(Collectors.toList());
        Assertions.assertThat(textTokens).hasSize(1);
        Assertions.assertThat(textTokens.get(0).getLexeme()).isEqualTo("Hello World");
    }

    @Test
    public void lexerWorksWithReader() {
        String src = "{{ variable | filter }}";
        StringReader reader = new StringReader(src);
        Lexer lexer = new Lexer(reader, false);
        List<Token> tokens = lexer.lex();
        
        // Should produce the same tokens as String constructor
        Lexer stringLexer = new Lexer(src);
        List<Token> stringTokens = stringLexer.lex();
        
        Assertions.assertThat(tokens).hasSameSizeAs(stringTokens);
        List<TokenType> tokenTypes = tokens.stream().map(Token::getType).collect(Collectors.toList());
        List<TokenType> stringTokenTypes = stringTokens.stream().map(Token::getType).collect(Collectors.toList());
        Assertions.assertThat(tokenTypes).isEqualTo(stringTokenTypes);
        
        List<String> lexemes = tokens.stream().map(Token::getLexeme).collect(Collectors.toList());
        List<String> stringLexemes = stringTokens.stream().map(Token::getLexeme).collect(Collectors.toList());
        Assertions.assertThat(lexemes).isEqualTo(stringLexemes);
    }

    @Test
    public void lexerHandlesMultiLineInput() {
        String src = "{{ variable }}\nSome text\n{% if condition %}\nMore content\n{% endif %}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Should handle multi-line input correctly
        Assertions.assertThat(tokens).isNotEmpty();
        Assertions.assertThat(tokens.get(tokens.size() - 1).getType()).isEqualTo(TokenType.EOF);
        
        // Should have all expected token types
        List<TokenType> tokenTypes = tokens.stream().map(Token::getType).collect(Collectors.toList());
        Assertions.assertThat(tokenTypes).contains(TokenType.VAR_OPEN);
        Assertions.assertThat(tokenTypes).contains(TokenType.VAR_CLOSE);
        Assertions.assertThat(tokenTypes).contains(TokenType.TAG_OPEN);
        Assertions.assertThat(tokenTypes).contains(TokenType.TAG_CLOSE);
        Assertions.assertThat(tokenTypes).contains(TokenType.TEXT);
    }

    @Test
    public void lexerRecognizesKeywords() {
        String src = "{% if condition %}Hello{% endif %}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Should recognize 'if' and 'endif' as keywords
        List<Token> keywordTokens = tokens.stream().filter(t -> t.getType() == TokenType.KEYWORD).collect(Collectors.toList());
        Assertions.assertThat(keywordTokens).hasSize(2);
        Assertions.assertThat(keywordTokens.get(0).getLexeme()).isEqualTo("if");
        Assertions.assertThat(keywordTokens.get(1).getLexeme()).isEqualTo("endif");
        
        // Should have other expected tokens
        List<TokenType> tokenTypes = tokens.stream().map(Token::getType).collect(Collectors.toList());
        Assertions.assertThat(tokenTypes).contains(TokenType.TAG_OPEN);
        Assertions.assertThat(tokenTypes).contains(TokenType.TAG_CLOSE);
        Assertions.assertThat(tokenTypes).contains(TokenType.IDENT); // "condition"
        Assertions.assertThat(tokenTypes).contains(TokenType.TEXT); // "Hello"
    }

    @Test
    public void lexerRecognizesBooleanKeywords() {
        String src = "{% if true or false %}{{ nil }}{% endif %}";
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        
        // Should recognize 'if', 'true', 'or', 'false', 'nil', 'endif' as keywords
        List<Token> keywordTokens = tokens.stream().filter(t -> t.getType() == TokenType.KEYWORD).collect(Collectors.toList());
        Assertions.assertThat(keywordTokens).hasSize(6);
        List<String> keywordLexemes = keywordTokens.stream().map(Token::getLexeme).collect(Collectors.toList());
        Assertions.assertThat(keywordLexemes).containsExactly("if", "true", "or", "false", "nil", "endif");
    }

    @Test
    public void lexerThrowsErrorInObject() {
        String src = "{{ % }}";
        Lexer lexer = new Lexer(src);
        Assertions.assertThatThrownBy(new ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                lexer.lex();
            }
        }).isInstanceOf(LexerException.class);
    }

    @Test
    public void lexerThrowsErrorInTag() {
        String src = "{% %%}";
        Lexer lexer = new Lexer(src);
        Assertions.assertThatThrownBy(new ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                lexer.lex();
            }
        }).isInstanceOf(LexerException.class);
    }
}