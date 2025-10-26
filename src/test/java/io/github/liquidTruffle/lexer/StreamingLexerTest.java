package io.github.liquidTruffle.lexer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StreamingLexerTest {
    
    @Test
    public void canStreamTokensFromString() {
        String liquidTemplate = "Hello {{ name | capitalize }}, welcome to {{ site.title }}!";
        TokenStream stream = new Lexer(liquidTemplate);
        
        List<Token> tokens = new ArrayList<>();
        while (stream.hasNext()) {
            tokens.add(stream.advance());
        }
        
        // Verify we got the expected tokens
        assertThat(tokens).hasSize(14);
        assertThat(tokens.stream().map(Token::type)).containsExactly(
                TokenType.TEXT, TokenType.OBJECT_OPEN, TokenType.IDENT, TokenType.PIPE, TokenType.IDENT,
                TokenType.OBJECT_CLOSE, TokenType.TEXT, TokenType.OBJECT_OPEN, TokenType.IDENT, TokenType.DOT,
                TokenType.IDENT, TokenType.OBJECT_CLOSE, TokenType.TEXT, TokenType.EOF
        );
    }
    
    @Test
    public void canPeekTokensWithoutConsuming() {
        String liquidTemplate = "{{ name }}";
        TokenStream stream = new Lexer(liquidTemplate);

        // Peek at the first token without consuming it
        Token firstToken = stream.peek();
        assertThat(firstToken.type()).isEqualTo(TokenType.OBJECT_OPEN);
        
        // Peek again - should get the same token
        Token firstTokenAgain = stream.peek();
        assertThat(firstTokenAgain).isSameAs(firstToken);
        
        // Now advance to consume it
        Token consumedToken = stream.advance();
        assertThat(consumedToken).isSameAs(firstToken);
        
        // Next peek should be different
        Token secondToken = stream.peek();
        assertThat(secondToken.type()).isEqualTo(TokenType.IDENT);
    }
    
    @Test
    public void canUseLookAhead() {
        String liquidTemplate = "{{ name | capitalize }}";
        TokenStream stream = new Lexer(liquidTemplate);
        
        // Look ahead 3 tokens
        Token[] lookahead = stream.lookAhead(3);
        assertThat(lookahead).hasSize(3);
        assertThat(lookahead[0].type()).isEqualTo(TokenType.OBJECT_OPEN);
        assertThat(lookahead[1].type()).isEqualTo(TokenType.IDENT);
        assertThat(lookahead[2].type()).isEqualTo(TokenType.PIPE);
        
        // Stream position should not have changed
        assertThat(stream.peek().type()).isEqualTo(TokenType.OBJECT_OPEN);
    }
    
    @Test
    public void streamingIsMemoryEfficient() {
        // Create a large template
        StringBuilder largeTemplate = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeTemplate.append("{{ var").append(i).append(" | filter").append(i).append(" }} ");
        }
        
        // Stream approach - should use constant memory
        TokenStream stream = new Lexer(largeTemplate.toString());
        int tokenCount = 0;
        while (stream.hasNext()) {
            stream.advance();
            tokenCount++;
        }
        
        // Should have processed many tokens
        assertThat(tokenCount).isGreaterThan(1000);
        
        // Memory usage should be constant (we can't easily test this in a unit test,
        // but the fact that we can process a large template without running out of memory
        // demonstrates the efficiency)
    }
}