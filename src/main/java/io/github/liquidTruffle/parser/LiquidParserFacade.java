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

    protected List<AstNode> parseNodes(Reader reader) {
        tokenStream = new Lexer(reader);
        return parseNodes();
    }

    private List<AstNode> parseNodes() {
        List<AstNode> nodes = new ArrayList<>();
        while (tokenStream.hasNext()) {
            if (check(TokenType.EOF)) {
                break;
            }
            nodes.add(parseNode());
        }
        return nodes;
    }

    /**
     * Always call this AFTER having advanced token
     */
    private AstNode parseNode() {
        if (match(TokenType.TEXT)) {
            return new TextNode(lastConsumedToken.lexeme());
        } else if (match(TokenType.OBJECT_OPEN)) {
            return parseObject();
        } else if (match(TokenType.TAG_OPEN)) {
            return parseTag();
        } else {
            throw new LiquidParserException("Found extraneous token when parsing " + tokenStream.peek(),
                    tokenStream.peek());
        }
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
            throw new LiquidParserException("Expecting a tag command, but found none");
        }
        if ("if".equals(kw)) {
            return parseIfNode();
        }
        throw new LiquidParserException("Unsupported / unexpected tag command " + kw);
    }

    private IfNode parseIfNode() {
        AstNode condition = parseCondition();
        expect(TokenType.TAG_CLOSE, "Expected '%}' after if condition");

        List<AstNode> body = new ArrayList<>();
        while (!checkEndIf()) { // Advances token
            body.add(parseNode());
        }
        expect(TokenType.TAG_OPEN, "Expected '{%' for endif");
        expect(TokenType.KEYWORD, "Expected 'endif' for endif");
        expect(TokenType.TAG_CLOSE, "Expected '%}' for endif");
        return new IfNode(condition, body.toArray(new AstNode[0]));
    }

    private boolean checkEndIf() {
        if (!(peek().type() == TokenType.TAG_OPEN)) {
            return false;
        }
        Token nextToken = peek2();
        return nextToken != null
                && nextToken.type() == TokenType.KEYWORD
                && "endif".equals(nextToken.lexeme());
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

    private AstNode parseCondition() {
        // TODO full support for logical expression
        if (check(TokenType.STRING) || check(TokenType.NUMBER) || check(TokenType.KEYWORD)) {
            return literal();
        } else if (check(TokenType.IDENT)) {
            return parseVariableRef();
        } else {
            throw new LiquidParserException("Expected condition (literal or variable) but got " + peek());
        }
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
        if (!check(t)) throw new LiquidParserException(msg, peek());
        advance();
    }

    private Token advance() {
        System.out.println("Consumed: " + lastConsumedToken);
        lastConsumedToken = tokenStream.advance();
        return lastConsumedToken;
    }
    
    private Token prev() {
        return lastConsumedToken;
    }

    private Token peek() {
        return tokenStream.peek();
    }

    private Token peek2() {
        return tokenStream.lookAhead(1)[0];
    }
}