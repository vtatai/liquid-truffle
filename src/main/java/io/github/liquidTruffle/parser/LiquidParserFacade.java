package io.github.liquidTruffle.parser;

import io.github.liquidTruffle.lexer.Lexer;
import io.github.liquidTruffle.lexer.Token;
import io.github.liquidTruffle.lexer.TokenType;
import io.github.liquidTruffle.parser.ast.AstNode;
import io.github.liquidTruffle.parser.ast.nodes.*;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class LiquidParserFacade {
    private List<Token> tokens;
    private int p = 0;

    public LiquidRootNode parse(LiquidLanguage language, Reader reader) {
        return new LiquidRootNode(language, parseNodes(reader).toArray(new AstNode[0]));
    }
    
    public List<AstNode> parseNodes(Reader reader) {
        tokens = new Lexer(reader, false).lex();
        p = 0;
        List<AstNode> nodes = new ArrayList<>();
        while (!match(TokenType.EOF)) {
            if (check(TokenType.TEXT)) {
                nodes.add(new TextNode(advance().lexeme()));
            } else if (match(TokenType.VAR_OPEN)) {
                nodes.add(parseObject());
                expect(TokenType.VAR_CLOSE, "Expected '}}'");
            } else if (match(TokenType.TAG_OPEN)) {
                nodes.add(parseTag());
                expect(TokenType.TAG_CLOSE, "Expected '%}'");
            } else if (check(TokenType.WHITESPACE)) {
                // keep whitespace outside tags/vars as text
                nodes.add(new TextNode(advance().lexeme()));
            } else {
                // fallback consume
                nodes.add(new TextNode(advance().lexeme()));
            }
        }
        return nodes;
    }

    public LiquidRootNode parse(LiquidLanguage language, String src) {
        return parse(language, new StringReader(src));
    }

    private AstNode parseObject() {
        skipSpace();
        
        // Check if this is a literal or a variable
        AstNode child;
        if (check(TokenType.STRING) || check(TokenType.NUMBER) || check(TokenType.KEYWORD)) {
            // Parse as literal
            child = literal();
        } else if (check(TokenType.IDENT)) {
            // Parse as variable
            child = parseVariable();
        } else {
            // Fallback to literal parsing
            child = literal();
        }
        
        return new LiquidObjectNode(child);
    }

    private AstNode parseVariable() {
        skipSpace();
        String name = ident();
        List<VariableNode.FilterSpec> filters = new ArrayList<>();
        while (true) {
            skipSpace();
            if (!match(TokenType.PIPE)) break;
            skipSpace();
            String filterName = ident();
            List<AstNode> args = new ArrayList<>();
            skipSpace();
            if (match(TokenType.COLON)) {
                do {
                    skipSpace();
                    args.add(literal());
                    skipSpace();
                } while (match(TokenType.COMMA));
            }
            filters.add(new VariableNode.FilterSpec(filterName, args));
        }
        return new VariableNode(name, filters);
    }

    private AstNode parseTag() {
        skipSpace();
        String kw = ident();
        skipSpace();
        if (kw.isBlank()) {
            return null;
        }
        if ("if".equals(kw)) {
            String varName = ident();
            expect(TokenType.TAG_CLOSE, "Expected '%}' after if condition");
            List<AstNode> body = new ArrayList<>();
            while (!(matchSeq(TokenType.TAG_OPEN, TokenType.IDENT, TokenType.TAG_CLOSE) && 
                     prev(1).lexeme().equals("endif"))) {
                if (check(TokenType.EOF)) break;
                if (check(TokenType.TEXT) || check(TokenType.WHITESPACE)) {
                    body.add(new TextNode(advance().lexeme()));
                } else if (match(TokenType.VAR_OPEN)) {
                    body.add(parseObject());
                    expect(TokenType.VAR_CLOSE, "Expected '}}'");
                } else if (match(TokenType.TAG_OPEN)) {
                    AstNode nested = parseTag();
                    expect(TokenType.TAG_CLOSE, "Expected '%}'");
                    if (nested != null) body.add(nested);
                } else {
                    body.add(new TextNode(advance().lexeme()));
                }
            }
            return new IfNode(varName, body.toArray(new AstNode[0]));
        }
        throw new LiquidParserException("Unsupported / unexpected tag command " + kw);
    }

    private void skipSpace() {
        while (check(TokenType.WHITESPACE)) advance();
    }

    private String ident() {
        Token t;
        if (match(TokenType.IDENT)) {
            t = prev();
        } else if (match(TokenType.KEYWORD)) {
            t = prev();
        } else {
            throw new RuntimeException("Expected identifier or keyword");
        }
        return t.lexeme();
    }

    private AstNode literal() {
        if (match(TokenType.STRING)) {
            return new StringLiteralNode(prev().lexeme());
        } else if (match(TokenType.NUMBER)) {
            return new NumberLiteralNode(Integer.parseInt(prev().lexeme()));
        } else if (match(TokenType.KEYWORD)) {
            String keyword = prev().lexeme();
            return switch (keyword) {
                case "true" -> new BooleanLiteralNode(true);
                case "false" -> new BooleanLiteralNode(false);
                case "nil", "null" -> new NilLiteralNode();
                default -> new StringLiteralNode(keyword); // Treat other keywords as strings
            };
        } else if (match(TokenType.IDENT)) {
            return new StringLiteralNode(prev().lexeme());
        } else {
            return new StringLiteralNode("");
        }
    }

    private boolean check(TokenType t) {
        return peek().type() == t;
    }

    private boolean match(TokenType t) {
        if (check(t)) {
            advance();
            return true;
        } else {
            return false;
        }
    }

    private boolean matchSeq(TokenType a, TokenType b, TokenType c) {
        if (check(a)) {
            advance();
            expect(b, "");
            expect(c, "");
            return true;
        } else {
            return false;
        }
    }

    private void expect(TokenType t, String msg) {
        if (!check(t)) throw new RuntimeException(msg + " at token " + peek());
        advance();
    }

    private Token advance() {
        return tokens.get(p++);
    }
    
    private Token prev() {
        return tokens.get(p - 1);
    }
    
    private Token prev(int back) {
        return tokens.get(p - back);
    }
    
    private Token peek() {
        return tokens.get(p);
    }
}