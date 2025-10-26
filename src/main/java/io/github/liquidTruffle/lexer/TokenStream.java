package io.github.liquidTruffle.lexer;

/**
 * Interface for streaming tokens from a lexer.
 * This allows for memory-efficient tokenization without loading all tokens into memory.
 */
public interface TokenStream {
    /**
     * Returns the next token without consuming it.
     * @return the next token, or null if no more tokens
     */
    Token peek();
    
    /**
     * Consumes and returns the next token.
     * @return the next token, or null if no more tokens
     */
    Token advance();
    
    /**
     * Returns true if there are more tokens available.
     * @return true if more tokens are available
     */
    boolean hasNext();
    
    /**
     * Looks ahead n tokens without consuming them.
     * @param n number of tokens to look ahead
     * @return array of tokens, or null if not enough tokens available
     */
    Token[] lookAhead(int n);
}