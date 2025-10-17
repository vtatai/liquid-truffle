package io.github.liquidTruffle.parser;

import io.github.liquidTruffle.LiquidRuntimeException;
import io.github.liquidTruffle.lexer.Lexer;
import io.github.liquidTruffle.lexer.Token;
import io.github.liquidTruffle.lexer.TokenType;
import io.github.liquidTruffle.parser.ast.AstNode;
import io.github.liquidTruffle.parser.ast.nodes.*;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LiquidParserFacade {
    private List<Token> tokens;
    private int p = 0;
    private Map<String, FilterFunction> filterFunctions = Map.of(
            "append", new FilterFunction("append", params -> params[0].toString() + params[1].toString()),
            "limit", new FilterFunction("limit", params -> {
                throw new LiquidRuntimeException("Not implemented");
            }),
            "replace", new FilterFunction("replace", params -> {
                return params[0].toString().replace(params[1].toString(), params[2].toString());
            })
    );

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
            } else if (match(TokenType.OBJECT_OPEN)) {
                nodes.add(parseObject());
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
            child = parseVariableRef();
        } else {
            throw new LiquidParserException("First part of object should be a literal or variable ref");
        }

        skipSpace();
        if (check(TokenType.OBJECT_CLOSE)) {
            advance();
            return new LiquidObjectNode(child, new FilterNode[0]);
        }
        FilterNode[] filters = parseFilters();
        expect(TokenType.OBJECT_CLOSE, "Expected '}}'");
        return new LiquidObjectNode(child, filters);
    }

    private FilterNode[] parseFilters() {
        List<FilterNode> nodes = new ArrayList<>();
        while (!check(TokenType.EOF) && !check(TokenType.OBJECT_CLOSE)) {
            expect(TokenType.PIPE, "Expected '|'");
            nodes.add(parseFilter());
            skipSpace();
        }
        return nodes.toArray(new FilterNode[0]);
    }

    private FilterNode parseFilter() {
        String functionName = ident();
        if (!filterFunctions.containsKey(functionName)) {
            throw new LiquidParserException(functionName);
        }
        FilterFunction filterFunction = filterFunctions.get(functionName);
        
        // Parse filter parameters if present
        List<AstNode> params = new ArrayList<>();
        if (match(TokenType.COLON)) {
            skipSpace();
            // Parse comma-separated parameters
            do {
                skipSpace();
                if (check(TokenType.STRING) || check(TokenType.NUMBER) || check(TokenType.KEYWORD) || check(TokenType.IDENT)) {
                    params.add(literal());
                } else {
                    throw new LiquidParserException("Expected parameter after colon in filter " + functionName);
                }
                skipSpace();
            } while (match(TokenType.COMMA));
        }
        
        return new FilterNode(filterFunction, params.toArray(new AstNode[0]));
    }

    private AstNode parseLiteralOrVariableRef() {
        if (check(TokenType.IDENT)) {
            return parseVariableRef();
        }
        if (check(TokenType.STRING) || check(TokenType.NUMBER)) {
            return literal();
        }
        throw new LiquidParserException("Expected literal or variable ref but got " + peek());
    }

    private AstNode parseVariableRef() {
        skipSpace();
        String name = ident();
        return new VariableRefNode(name);
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
                } else if (match(TokenType.OBJECT_OPEN)) {
                    body.add(parseObject());
                    expect(TokenType.OBJECT_CLOSE, "Expected '}}'");
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
        }
        throw new LiquidParserException("Expecting a literal node but got " + peek());
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