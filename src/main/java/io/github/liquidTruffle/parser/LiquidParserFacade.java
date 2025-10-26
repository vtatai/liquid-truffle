package io.github.liquidTruffle.parser;

import io.github.liquidTruffle.LiquidRuntimeException;
import io.github.liquidTruffle.lexer.Lexer;
import io.github.liquidTruffle.lexer.Token;
import io.github.liquidTruffle.lexer.TokenStream;
import io.github.liquidTruffle.lexer.TokenType;
import io.github.liquidTruffle.parser.ast.AstNode;
import io.github.liquidTruffle.parser.ast.nodes.*;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LiquidParserFacade {
    private TokenStream tokenStream;
    private Token lastConsumedToken = null;
    private final Map<String, FilterFunction> filterFunctions = Map.of(
            "append", new FilterFunction("append", params -> params[0].toString() + params[1].toString()),
            "capitalize", new FilterFunction("capitalize", params -> params[0].toString().toUpperCase()),
            "limit", new FilterFunction("limit", _ -> {
                throw new LiquidRuntimeException("Not implemented");
            }),
            "replace", new FilterFunction("replace",
                    params -> params[0].toString().replace(params[1].toString(), params[2].toString()))
    );

    public LiquidRootNode parse(LiquidLanguage language, Reader reader) {
        return new LiquidRootNode(language, parseNodes(reader).toArray(new AstNode[0]));
    }
    
    public List<AstNode> parseNodes(Reader reader) {
        tokenStream = new Lexer(reader);
        List<AstNode> nodes = new ArrayList<>();
        while (tokenStream.hasNext()) {
            if (check(TokenType.TEXT)) {
                nodes.add(new TextNode(advance().lexeme()));
            } else if (match(TokenType.OBJECT_OPEN)) {
                nodes.add(parseObject());
            } else if (match(TokenType.TAG_OPEN)) {
                nodes.add(parseTag());
                expect(TokenType.TAG_CLOSE, "Expected '%}'");
            } else if (match(TokenType.EOF)) {
                break;
            } else {
                throw new LiquidParserException("Found extraneous token when parsing " + tokenStream.advance());
            }
        }
        return nodes;
    }

    public LiquidRootNode parse(LiquidLanguage language, String src) {
        return parse(language, new StringReader(src));
    }

    private AstNode parseObject() {
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

        if (check(TokenType.OBJECT_CLOSE)) {
            advance();
            return new LiquidObjectNode(child);
        }
        
        // Parse filters and build binary tree
        AstNode filterChain = parseFilterChain(child);
        expect(TokenType.OBJECT_CLOSE, "Expected '}}'");
        return new LiquidObjectNode(filterChain);
    }

    private AstNode parseFilterChain(AstNode initialValue) {
        AstNode current = initialValue;
        
        while (!check(TokenType.EOF) && !check(TokenType.OBJECT_CLOSE)) {
            expect(TokenType.PIPE, "Expected '|'");
            FilterNode filter = parseFilter();
            current = new FilterNode(filter.getFilterFunction(), current, filter.getParameters());
        }
        
        return current;
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
            // Parse comma-separated parameters
            do {
                if (check(TokenType.STRING) || check(TokenType.NUMBER) || check(TokenType.KEYWORD) || check(TokenType.IDENT)) {
                    params.add(literal());
                } else {
                    throw new LiquidParserException("Expected parameter after colon in filter " + functionName);
                }
            } while (match(TokenType.COMMA));
        }
        
        // Create a temporary FilterNode with null left child - this will be replaced in parseFilterChain
        return new FilterNode(filterFunction, null, params.toArray(new AstNode[0]));
    }

    private AstNode parseVariableRef() {
        String name = ident();
        return new VariableRefNode(name);
    }

    private AstNode parseTag() {
        String kw = ident();
        if (kw.isBlank()) {
            return null;
        }
        if ("if".equals(kw)) {
            String varName = ident();
            expect(TokenType.TAG_CLOSE, "Expected '%}' after if condition");
            List<AstNode> body = new ArrayList<>();
            while (tokenStream.hasNext()) {
                if (check(TokenType.TAG_OPEN)) {
                    // Use lookahead to check if this is an endif
                    Token[] lookahead = tokenStream.lookAhead(3);
                    if (lookahead.length >= 3 && 
                        lookahead[0].type() == TokenType.TAG_OPEN &&
                        lookahead[1].type() == TokenType.IDENT && 
                        lookahead[1].lexeme().equals("endif") &&
                        lookahead[2].type() == TokenType.TAG_CLOSE) {
                        // This is an endif, consume it and break
                        advance(); // consume TAG_OPEN
                        advance(); // consume "endif"
                        advance(); // consume TAG_CLOSE
                        break;
                    } else {
                        // This is not an endif, parse it as content
                        if (check(TokenType.TEXT)) {
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
                } else if (check(TokenType.TEXT)) {
                    body.add(new TextNode(advance().lexeme()));
                } else if (match(TokenType.OBJECT_OPEN)) {
                    body.add(parseObject());
                    expect(TokenType.OBJECT_CLOSE, "Expected '}}'");
                } else {
                    body.add(new TextNode(advance().lexeme()));
                }
            }
            return new IfNode(varName, body.toArray(new AstNode[0]));
        }
        throw new LiquidParserException("Unsupported / unexpected tag command " + kw);
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
        Token token = peek();
        return token != null && token.type() == t;
    }

    private boolean match(TokenType t) {
        if (check(t)) {
            advance();
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
        lastConsumedToken = tokenStream.advance();
        return lastConsumedToken;
    }
    
    private Token prev() {
        return lastConsumedToken;
    }
    
    private Token peek() {
        return tokenStream.peek();
    }
}