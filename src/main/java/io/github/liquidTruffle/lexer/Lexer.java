package io.github.liquidTruffle.lexer;

import java.io.Reader;
import java.io.StringReader;
import java.util.*;

public class Lexer implements TokenStream {
    private final Reader reader;
    private final char[] buffer = new char[4096]; // Buffer for reading from Reader
    private int bufferEnd = 0;
    private int bufferPos = 0;
    private int position = 0; // Global position counter
    private boolean eof = false;
    private LexerMode currentMode = LexerMode.IN_TEXT;
    
    // Streaming state
    private Token nextToken = null;
    private boolean hasNextToken = false;
    private boolean streamInitialized = false;
    
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
        "first", "last", "size"
    ));

    public Lexer(Reader reader) {
        this.reader = reader;
    }
    
    public Lexer(String src) {
        this(new StringReader(src));
    }
    
    /**
     * This loads all tokens in memory as a list. Please do not use other than for tests or very simple lexing.
     */
    public List<Token> lex() {
        List<Token> tokens = new ArrayList<>();
        fillBuffer(); // Initial buffer fill

        while (!eof || bufferPos < bufferEnd) {
            Token token = processCurrentState();
            tokens.add(token);
            fillBuffer();
        }

        tokens.add(new Token(TokenType.EOF, "", position, position));
        return tokens;
    }

    // TokenStream interface implementation
    @Override
    public Token peek() {
        if (!streamInitialized) {
            initializeStream();
        }
        return nextToken;
    }
    
    @Override
    public Token advance() {
        if (!streamInitialized) {
            initializeStream();
        }
        Token current = nextToken;
        if (hasNextToken && current.type() != TokenType.EOF) {
            nextToken = getNextToken();
            hasNextToken = nextToken != null;
        } else {
            nextToken = null;
            hasNextToken = false;
        }
        return current;
    }
    
    @Override
    public boolean hasNext() {
        if (!streamInitialized) {
            initializeStream();
        }
        return hasNextToken;
    }
    
    @Override
    public Token[] lookAhead(int n) {
        // For simplicity, we'll implement a basic lookahead
        // In a more sophisticated implementation, we might want to cache tokens
        List<Token> tokens = new ArrayList<>();
        int originalPosition = position;
        int originalBufferPos = bufferPos;
        boolean originalEof = eof;
        LexerMode originalMode = currentMode;
        
        try {
            for (int i = 0; i < n; i++) {
                if (eof && bufferPos >= bufferEnd) {
                    break;
                }
                Token token = processCurrentState();
                if (token.type() == TokenType.EOF) {
                    break;
                }
                tokens.add(token);
                fillBuffer();
            }
            return tokens.toArray(new Token[0]);
        } finally {
            // Restore state
            position = originalPosition;
            bufferPos = originalBufferPos;
            eof = originalEof;
            currentMode = originalMode;
        }
    }
    
    private void initializeStream() {
        fillBuffer(); // Initial buffer fill
        nextToken = getNextToken();
        hasNextToken = (nextToken != null && nextToken.type() != TokenType.EOF);
        streamInitialized = true;
    }
    
    private Token getNextToken() {
        if (eof && bufferPos >= bufferEnd) {
            return new Token(TokenType.EOF, "", position, position);
        }
        Token token = processCurrentState();
        fillBuffer();
        return token;
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
        } else if (peek2("{#")) {
            // Process comment: {# comment #}
            processCommentOpen();
            processComment();
            processCommentClose();
            return processText(); // Return empty text token for comments
        } else {
            return processText();
        }
    }

    private Token processTagMode() {
        if (Character.isWhitespace(peekChar())) {
            skipWhitespace();
        }
        if (peek2("%}")) {
            return processTagClose();
        } else if (peekChar() == '"' || peekChar() == '\'') {
            return processString();
        } else if (Character.isDigit(peekChar()) || (peekChar() == '-' && Character.isDigit(peekChar(1)))) {
            return processNumber();
        } else if (peek2("..")) {
            return processRange();
        } else if (peekChar() == '.') {
            return processDot();
        } else if (peekChar() == '|') {
            return processPipe();
        } else if (peekChar() == ',') {
            return processComma();
        } else if (peekChar() == ':') {
            return processColon();
        } else if (peekChar() == '?') {
            return processQuestion();
        } else if (peek2(">=")) {
            return processGTE();
        } else if (peek2("<=")) {
            return processLTE();
        } else if (peek2("==")) {
            return processEQ();
        } else if (peek2("!=")) {
            return processNE();
        } else if (peek2("**")) {
            return processExponent();
        } else if (peek2("..")) {
            return processRange();
        } else if (peekChar() == '>') {
            return processGT();
        } else if (peekChar() == '<') {
            return processLT();
        } else if (peekChar() == '+') {
            return processPlus();
        } else if (peekChar() == '-') {
            return processMinus();
        } else if (peekChar() == '*') {
            return processMultiply();
        } else if (peekChar() == '/') {
            return processDivide();
        } else if (peekChar() == '%') {
            return processModulo();
        } else if (peekChar() == '(') {
            return processLParen();
        } else if (peekChar() == ')') {
            return processRParen();
        } else if (peekChar() == '[') {
            return processLBracket();
        } else if (peekChar() == ']') {
            return processRBracket();
        } else if (peekChar() == '{') {
            return processLBrace();
        } else if (peekChar() == '}') {
            return processRBrace();
        } else if (isIdentStart(peekChar())) {
            return processIdentifier();
        } else {
            throw new LexerException("Invalid character in tag mode: " + peekChar());
        }
    }

    private Token processObjectMode() {
        if (Character.isWhitespace(peekChar())) {
            skipWhitespace();
        }
        if (peek2("}}")) {
            return processObjClose();
        } else if (peekChar() == '"' || peekChar() == '\'') {
            return processString();
        } else if (Character.isDigit(peekChar()) || (peekChar() == '-' && Character.isDigit(peekChar(1)))) {
            return processNumber();
        } else if (peek2("..")) {
            return processRange();
        } else if (peekChar() == '.') {
            return processDot();
        } else if (peekChar() == '|') {
            return processPipe();
        } else if (peekChar() == ',') {
            return processComma();
        } else if (peekChar() == ':') {
            return processColon();
        } else if (peekChar() == '?') {
            return processQuestion();
        } else if (peek2(">=")) {
            return processGTE();
        } else if (peek2("<=")) {
            return processLTE();
        } else if (peek2("==")) {
            return processEQ();
        } else if (peek2("!=")) {
            return processNE();
        } else if (peek2("**")) {
            return processExponent();
        } else if (peek2("..")) {
            return processRange();
        } else if (peekChar() == '>') {
            return processGT();
        } else if (peekChar() == '<') {
            return processLT();
        } else if (peekChar() == '+') {
            return processPlus();
        } else if (peekChar() == '-') {
            return processMinus();
        } else if (peekChar() == '*') {
            return processMultiply();
        } else if (peekChar() == '/') {
            return processDivide();
        } else if (peekChar() == '%') {
            return processModulo();
        } else if (peekChar() == '(') {
            return processLParen();
        } else if (peekChar() == ')') {
            return processRParen();
        } else if (peekChar() == '[') {
            return processLBracket();
        } else if (peekChar() == ']') {
            return processRBracket();
        } else if (peekChar() == '{') {
            return processLBrace();
        } else if (peekChar() == '}') {
            return processRBrace();
        } else if (isIdentStart(peekChar())) {
            return processIdentifier();
        } else {
            throw new LexerException("Invalid character in object mode: " + peekChar());
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
        if (isIdentStart(peekChar())) {
            result.append(peekChar());
            advance(1);
        }
        result.append(collectWhile(this::isIdentPart));
        return result.toString();
    }
    
    private Token parseString() {
        int startPos = position;
        char quote = peekChar();
        advance(1); // consume opening quote
        
        StringBuilder result = new StringBuilder();
        while (!eof || bufferPos < bufferEnd) {
            char c = peekChar();
            if (c == quote) {
                advance(1); // consume closing quote
                break;
            }
            if (c == '\\') {
                advance(1); // consume backslash
                if (!eof || bufferPos < bufferEnd) {
                    char next = peekChar();
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
            char c = peekChar();
            if (c == '\u0000') break; // EOF
            
            // Stop at special sequences
            if (peek2("{{") || peek2("{%") || peek2("{#")) {
                break;
            }
            
            result.append(c);
            advance(1);
        }
        return result.toString();
    }
    
    private String collectComment() {
        StringBuilder result = new StringBuilder();
        while (!eof || bufferPos < bufferEnd) {
            char c = peekChar();
            if (c == '\u0000') break; // EOF
            
            // Stop at comment close
            if (peek2("#}")) {
                break;
            }
            
            result.append(c);
            advance(1);
        }
        return result.toString();
    }

    // Processing functions
    private Token processObjOpen() {
        int startPos = position;
        advance(2);
        
        // Check for whitespace control
        if (peekChar() == '-') {
            advance(1);
            currentMode = LexerMode.IN_OBJ;
            return new Token(TokenType.OBJECT_OPEN_WS, "{{-", startPos, position);
        }
        
        currentMode = LexerMode.IN_OBJ;
        return new Token(TokenType.OBJECT_OPEN, "{{", startPos, position);
    }

    private Token processObjClose() {
        int startPos = position;
        
        // Check for whitespace control before advancing
        if (peekChar(-1) == '-') {
            advance(2);
            currentMode = LexerMode.IN_TEXT;
            return new Token(TokenType.OBJECT_CLOSE_WS, "-}}", startPos - 1, position);
        }
        
        advance(2);
        currentMode = LexerMode.IN_TEXT;
        return new Token(TokenType.OBJECT_CLOSE, "}}", startPos, position);
    }

    private Token processTagOpen() {
        int startPos = position;
        advance(2);
        
        // Check for whitespace control
        if (peekChar() == '-') {
            advance(1);
            currentMode = LexerMode.IN_TAG;
            return new Token(TokenType.TAG_OPEN_WS, "{%-", startPos, position);
        }
        
        currentMode = LexerMode.IN_TAG;
        return new Token(TokenType.TAG_OPEN, "{%", startPos, position);
    }
    
    private Token processTagClose() {
        int startPos = position;
        
        // Check for whitespace control before advancing
        if (peekChar(-1) == '-') {
            advance(2);
            currentMode = LexerMode.IN_TEXT;
            return new Token(TokenType.TAG_CLOSE_WS, "-%}", startPos - 1, position);
        }
        
        advance(2);
        currentMode = LexerMode.IN_TEXT;
        return new Token(TokenType.TAG_CLOSE, "%}", startPos, position);
    }
    
    private void skipWhitespace() {
        collectWhile(Character::isWhitespace);
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
        StringBuilder lexeme = new StringBuilder();
        
        // Handle negative numbers
        if (peekChar() == '-') {
            lexeme.append('-');
            advance(1);
        }
        
        // Collect digits
        lexeme.append(collectWhile(Character::isDigit));
        
        // Handle floating point numbers
        if (peekChar() == '.' && Character.isDigit(peekChar(1))) {
            lexeme.append('.');
            advance(1);
            lexeme.append(collectWhile(Character::isDigit));
            return new Token(TokenType.FLOAT, lexeme.toString(), startPos, position);
        }
        
        return new Token(TokenType.NUMBER, lexeme.toString(), startPos, position);
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
    
    private Token processComment() {
        int startPos = position;
        String lexeme = collectComment();
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

    // New token processing methods for missing Liquid constructs
    
    private Token processCommentOpen() {
        int startPos = position;
        advance(2);
        return new Token(TokenType.COMMENT_OPEN, "{#", startPos, position);
    }
    
    private Token processCommentClose() {
        int startPos = position;
        advance(2);
        return new Token(TokenType.COMMENT_CLOSE, "#}", startPos, position);
    }
    
    private Token processPlus() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.PLUS, "+", startPos, position);
    }
    
    private Token processMinus() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.MINUS, "-", startPos, position);
    }
    
    private Token processMultiply() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.MULTIPLY, "*", startPos, position);
    }
    
    private Token processDivide() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.DIVIDE, "/", startPos, position);
    }
    
    private Token processModulo() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.MODULO, "%", startPos, position);
    }
    
    private Token processExponent() {
        int startPos = position;
        advance(2);
        return new Token(TokenType.EXPONENT, "**", startPos, position);
    }
    
    private Token processLParen() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.LPAREN, "(", startPos, position);
    }
    
    private Token processRParen() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.RPAREN, ")", startPos, position);
    }
    
    private Token processLBracket() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.LBRACKET, "[", startPos, position);
    }
    
    private Token processRBracket() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.RBRACKET, "]", startPos, position);
    }
    
    private Token processLBrace() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.LBRACE, "{", startPos, position);
    }
    
    private Token processRBrace() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.RBRACE, "}", startPos, position);
    }
    
    private Token processQuestion() {
        int startPos = position;
        advance(1);
        return new Token(TokenType.QUESTION, "?", startPos, position);
    }
    
    private Token processRange() {
        int startPos = position;
        advance(2);
        return new Token(TokenType.RANGE, "..", startPos, position);
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

    private char peekChar() {
        fillBuffer();
        return bufferPos < bufferEnd ? buffer[bufferPos] : '\u0000';
    }
    
    private char peekChar(int offset) {
        fillBuffer();
        int pos = bufferPos + offset;
        return pos < bufferEnd ? buffer[pos] : '\u0000';
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
                    if (charsRead <= 0) {
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
            char c = peekChar();
            if (!predicate.test(c)) break;
            result.append(c);
            advance(1);
        }
        return result.toString();
    }
}