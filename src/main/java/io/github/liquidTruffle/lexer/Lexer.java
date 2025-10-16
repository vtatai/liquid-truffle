package io.github.liquidTruffle.lexer;

import java.io.Reader;
import java.io.StringReader;
import java.util.*;

public class Lexer {
    private final Reader reader;
    private final boolean reportWhitespaceTokens;
    private final char[] buffer = new char[4096]; // Buffer for reading from Reader
    private int bufferEnd = 0;
    private int bufferPos = 0;
    private int position = 0; // Global position counter
    private boolean eof = false;
    private LexerMode currentMode = LexerMode.IN_TEXT;
    
    // Liquid reserved keywords
    private final Set<String> keywords = new HashSet<>(Arrays.asList(
        // Control flow tags
        "assign", "capture", "case", "comment", "cycle", "for", "in", "break", "continue",
        "if", "include", "raw", "unless", "endfor", "endif", "endunless", "endcase",
        "else", "elsif", "when", "tablerow", "endtablerow", "increment", "decrement",
        "liquid", "echo", "render", "section", "endsection", "schema", "form",
        "paginate", "endpaginate", "layout", "block", "endblock", "extends",
        // Logical operators
        "and", "or", "not", "contains", "equals", "greater_than", "less_than",
        "greater_than_or_equal_to", "less_than_or_equal_to", "not_equals",
        // Other keywords
        "true", "false", "nil", "null", "empty", "blank", "default", "with",
        "limit", "offset", "reversed", "sort", "where", "group_by", "order",
        "first", "last", "size", "join", "split", "strip", "strip_html",
        "strip_newlines", "newline_to_br", "escape", "url_encode", "url_decode",
        "base64_encode", "base64_decode", "hmac_sha1", "hmac_sha256", "md5",
        "sha1", "sha256", "date", "time", "now", "today", "yesterday", "tomorrow"
    ));

    public Lexer(Reader reader, boolean reportWhitespaceTokens) {
        this.reader = reader;
        this.reportWhitespaceTokens = reportWhitespaceTokens;
    }
    
    public Lexer(String src, boolean reportWhitespaceTokens) {
        this(new StringReader(src), reportWhitespaceTokens);
    }
    
    public Lexer(String src) {
        this(src, false);
    }
    
    public List<Token> lex() {
        List<Token> tokens = new ArrayList<>();
        fillBuffer(); // Initial buffer fill

        while (!eof || bufferPos < bufferEnd) {
            Token token = processCurrentState();
            if (reportWhitespaceTokens || token.type() != TokenType.WHITESPACE) {
                tokens.add(token);
            }
            fillBuffer();
        }

        tokens.add(new Token(TokenType.EOF, "", position, position));
        return tokens;
    }
    
    private Token processCurrentState() {
        return switch (currentMode) {
            case IN_TEXT -> processTextMode();
            case IN_TAG -> processTagMode();
            case IN_OBJ -> processObjectMode();
        };
    }

    private Token processTextMode() {
        if (peek2("{{")) {
            return processObjOpen();
        } else if (peek2("{%")) {
            return processTagOpen();
        } else {
            return processText();
        }
    }

    private Token processTagMode() {
        if (peek2("%}")) {
            return processTagClose();
        } else if (Character.isWhitespace(peek())) {
            return processWhitespace();
        } else if (peek() == '"' || peek() == '\'') {
            return processString();
        } else if (Character.isDigit(peek())) {
            return processNumber();
        } else if (peek() == '.') {
            return processDot();
        } else if (peek() == '|') {
            return processPipe();
        } else if (peek() == ',') {
            return processComma();
        } else if (peek() == ':') {
            return processColon();
        } else if (peek2(">=")) {
            return processGTE();
        } else if (peek2("<=")) {
            return processLTE();
        } else if (peek2("==")) {
            return processEQ();
        } else if (peek2("!=")) {
            return processNE();
        } else if (peek() == '>') {
            return processGT();
        } else if (peek() == '<') {
            return processLT();
        } else if (isIdentStart(peek())) {
            return processIdentifier();
        } else {
            throw new LexerException("Invalid character in tag mode: " + peek());
        }
    }

    private Token processObjectMode() {
        if (peek2("}}")) {
            return processObjClose();
        } else if (Character.isWhitespace(peek())) {
            return processWhitespace();
        } else if (peek() == '"' || peek() == '\'') {
            return processString();
        } else if (Character.isDigit(peek())) {
            return processNumber();
        } else if (peek() == '.') {
            return processDot();
        } else if (peek() == '|') {
            return processPipe();
        } else if (peek() == ',') {
            return processComma();
        } else if (peek() == ':') {
            return processColon();
        } else if (peek2(">=")) {
            return processGTE();
        } else if (peek2("<=")) {
            return processLTE();
        } else if (peek2("==")) {
            return processEQ();
        } else if (peek2("!=")) {
            return processNE();
        } else if (peek() == '>') {
            return processGT();
        } else if (peek() == '<') {
            return processLT();
        } else if (isIdentStart(peek())) {
            return processIdentifier();
        } else {
            throw new LexerException("Invalid character in object mode: " + peek());
        }
    }

    private boolean isIdentStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '-';
    }
    
    private boolean isIdentPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '-';
    }
    
    private String collectIdent() {
        StringBuilder result = new StringBuilder();
        if (isIdentStart(peek())) {
            result.append(peek());
            advance(1);
        }
        result.append(collectWhile(this::isIdentPart));
        return result.toString();
    }
    
    private Token parseString() {
        int startPos = position;
        char quote = peek();
        advance(1); // consume opening quote
        
        StringBuilder result = new StringBuilder();
        while (!eof || bufferPos < bufferEnd) {
            char c = peek();
            if (c == quote) {
                advance(1); // consume closing quote
                break;
            }
            if (c == '\\') {
                advance(1); // consume backslash
                if (!eof || bufferPos < bufferEnd) {
                    char next = peek();
                    result.append(next);
                    advance(1);
                }
            } else {
                result.append(c);
                advance(1);
            }
        }
        
        return new Token(TokenType.STRING, result.toString(), startPos, position);
    }
    
    private String collectText() {
        StringBuilder result = new StringBuilder();
        while (!eof || bufferPos < bufferEnd) {
            char c = peek();
            if (c == '\u0000') break; // EOF
            
            // Stop at special sequences
            if (peek2("{{") || peek2("}}") || peek2("{%") || peek2("%}")) {
                break;
            }
            
            result.append(c);
            advance(1);
        }
        
        // Fallback for single character
        if (result.isEmpty() && (!eof || bufferPos < bufferEnd)) {
            result.append(peek());
            advance(1);
        }
        
        return result.toString();
    }

    // Processing functions
    private Token processObjOpen() {
        int startPos = position;
        advance(2);
        currentMode = LexerMode.IN_OBJ;
        return new Token(TokenType.OBJECT_OPEN, "{{", startPos, position);
    }

    private Token processObjClose() {
        int startPos = position;
        advance(2);
        currentMode = LexerMode.IN_TEXT;
        return new Token(TokenType.OBJECT_CLOSE, "}}", startPos, position);
    }

    private Token processTagOpen() {
        int startPos = position;
        advance(2);
        currentMode = LexerMode.IN_TAG;
        return new Token(TokenType.TAG_OPEN, "{%", startPos, position);
    }
    
    private Token processTagClose() {
        int startPos = position;
        advance(2);
        currentMode = LexerMode.IN_TEXT;
        return new Token(TokenType.TAG_CLOSE, "%}", startPos, position);
    }
    
    private Token processWhitespace() {
        int startPos = position;
        String lexeme = collectWhile(Character::isWhitespace);
        return new Token(TokenType.WHITESPACE, lexeme, startPos, position);
    }
    
    private Token processPipe() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.PIPE, "|", startPos, position);
    }
    
    private Token processColon() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.COLON, ":", startPos, position);
    }
    
    private Token processComma() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.COMMA, ",", startPos, position);
    }
    
    private Token processDot() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.DOT, ".", startPos, position);
    }

    private Token processString() {
        return parseString();
    }
    
    private Token processNumber() {
        int startPos = position;
        String lexeme = collectWhile(Character::isDigit);
        return new Token(TokenType.NUMBER, lexeme, startPos, position);
    }
    
    private Token processIdentifier() {
        int startPos = position;
        String lexeme = collectIdent();
        TokenType tokenType = keywords.contains(lexeme) ? TokenType.KEYWORD : TokenType.IDENT;
        return new Token(tokenType, lexeme, startPos, position);
    }
    
    private Token processText() {
        int startPos = position;
        String lexeme = collectText();
        return new Token(TokenType.TEXT, lexeme, startPos, position);
    }

    private Token processGT() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.GT, ">", startPos, position);
    }

    private Token processLT() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.LT, "<", startPos, position);
    }

    private Token processGTE() {
        int startPos = position;
        advance(2);
        return new Token(TokenType.GTE, ">=", startPos, position);
    }

    private Token processLTE() {
        int startPos = position;
        advance(2);
        return new Token(TokenType.LTE, "<=", startPos, position);
    }

    private Token processEQ() {
        int startPos = position;
        advance(2);
        return new Token(TokenType.EQ, "==", startPos, position);
    }

    private Token processNE() {
        int startPos = position;
        advance(2);
        return new Token(TokenType.NE, "!=", startPos, position);
    }

    // Buffer management methods
    private void fillBuffer() {
        if (bufferPos >= bufferEnd && !eof) {
            bufferPos = 0;
            try {
                int charsRead = reader.read(buffer);
                if (charsRead == -1) {
                    eof = true;
                    bufferEnd = 0;
                } else {
                    bufferEnd = charsRead;
                }
            } catch (Exception e) {
                throw new LexerException("Error reading from input", e);
            }
        }
    }

    private char peek() {
        fillBuffer();
        return bufferPos < bufferEnd ? buffer[bufferPos] : '\u0000';
    }

    private boolean peek2(String s) {
        fillBuffer();
        if (bufferPos + s.length() > bufferEnd) {
            // Need to check across buffer boundary
            return peekString(s.length()).equals(s);
        }
        char[] target = s.toCharArray();
        for (int i = 0; i < s.length(); i++) {
            if (buffer[bufferPos + i] != target[i]) {
                return false;
            }
        }
        return true;
    }

    private String peekString(int length) {
        fillBuffer();
        StringBuilder result = new StringBuilder();
        int remaining = length;
        int pos = bufferPos;

        while (remaining > 0) {
            if (pos >= bufferEnd) {
                // Need more data
                try {
                    int charsRead = reader.read(buffer, bufferEnd, buffer.length - bufferEnd);
                    if (charsRead == -1) {
                        break; // EOF
                    }
                    bufferEnd += charsRead;
                } catch (Exception e) {
                    throw new LexerException("Error reading from input", e);
                }
            }

            int available = bufferEnd - pos;
            int toRead = Math.min(remaining, available);
            result.append(buffer, pos, toRead);
            pos += toRead;
            remaining -= toRead;
        }

        return result.toString();
    }

    private void advance(int count) {
        for (int i = 0; i < count; i++) {
            fillBuffer();
            if (bufferPos < bufferEnd) {
                bufferPos++;
                position++;
            }
        }
    }

    private String collectWhile(java.util.function.Predicate<Character> predicate) {
        StringBuilder result = new StringBuilder();
        while (!eof || bufferPos < bufferEnd) {
            char c = peek();
            if (!predicate.test(c)) break;
            result.append(c);
            advance(1);
        }
        return result.toString();
    }
}