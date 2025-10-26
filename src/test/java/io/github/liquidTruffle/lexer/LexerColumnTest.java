package io.github.liquidTruffle.lexer;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

public class LexerColumnTest {

    @Test
    public void testBasicColumnTracking() {
        String template = "Hello {{ name }} World";
        Lexer lexer = new Lexer(template);
        List<Token> tokens = lexer.lex();

        // First text token should start at column 1
        Token firstText = tokens.get(0);
        assertThat(firstText.type()).isEqualTo(TokenType.TEXT);
        assertThat(firstText.lexeme()).isEqualTo("Hello ");
        assertThat(firstText.line()).isEqualTo(1);
        assertThat(firstText.column()).isEqualTo(1);

        // Object open should start at column 7 (after "Hello ")
        Token objectOpen = tokens.get(1);
        assertThat(objectOpen.type()).isEqualTo(TokenType.OBJECT_OPEN);
        assertThat(objectOpen.line()).isEqualTo(1);
        assertThat(objectOpen.column()).isEqualTo(7);

        // Identifier should start at column 10 (after "{{ ")
        Token identifier = tokens.get(2);
        assertThat(identifier.type()).isEqualTo(TokenType.IDENT);
        assertThat(identifier.lexeme()).isEqualTo("name");
        assertThat(identifier.line()).isEqualTo(1);
        assertThat(identifier.column()).isEqualTo(10);

        // Object close should start at column 15 (after "name ")
        Token objectClose = tokens.get(3);
        assertThat(objectClose.type()).isEqualTo(TokenType.OBJECT_CLOSE);
        assertThat(objectClose.line()).isEqualTo(1);
        assertThat(objectClose.column()).isEqualTo(15);

        // Second text token should start at column 15 (after "}} ")
        Token secondText = tokens.get(4);
        assertThat(secondText.type()).isEqualTo(TokenType.TEXT);
        assertThat(secondText.lexeme()).isEqualTo(" World");
        assertThat(secondText.line()).isEqualTo(1);
        assertThat(secondText.column()).isEqualTo(17);
    }

    @Test
    public void testMultilineColumnTracking() {
        String template = "Line 1\n  {{ name }}\nLine 3";
        Lexer lexer = new Lexer(template);
        List<Token> tokens = lexer.lex();

        // First text token should start at column 1, line 1
        Token firstText = tokens.get(0);
        assertThat(firstText.type()).isEqualTo(TokenType.TEXT);
        assertThat(firstText.lexeme()).isEqualTo("Line 1\n  ");
        assertThat(firstText.line()).isEqualTo(1);
        assertThat(firstText.column()).isEqualTo(1);

        // Object open should start at column 3, line 2 (after "  ")
        Token objectOpen = tokens.get(1);
        assertThat(objectOpen.type()).isEqualTo(TokenType.OBJECT_OPEN);
        assertThat(objectOpen.line()).isEqualTo(2);
        assertThat(objectOpen.column()).isEqualTo(3);

        // Identifier should start at column 6, line 2 (after "{{ ")
        Token identifier = tokens.get(2);
        assertThat(identifier.type()).isEqualTo(TokenType.IDENT);
        assertThat(identifier.lexeme()).isEqualTo("name");
        assertThat(identifier.line()).isEqualTo(2);
        assertThat(identifier.column()).isEqualTo(6);

        // Object close should start at column 9, line 2 (after "name ")
        Token objectClose = tokens.get(3);
        assertThat(objectClose.type()).isEqualTo(TokenType.OBJECT_CLOSE);
        assertThat(objectClose.line()).isEqualTo(2);
        assertThat(objectClose.column()).isEqualTo(11);

        // Second text token should start at column 1, line 2 (with newline)
        Token secondText = tokens.get(4);
        assertThat(secondText.type()).isEqualTo(TokenType.TEXT);
        assertThat(secondText.lexeme()).isEqualTo("\nLine 3");
        assertThat(secondText.line()).isEqualTo(2);
        assertThat(secondText.column()).isEqualTo(13);
    }

    @Test
    public void testTagColumnTracking() {
        String template = "{% if condition %}Text{% endif %}";
        Lexer lexer = new Lexer(template);
        List<Token> tokens = lexer.lex();

        // First tag open should start at column 1
        Token tagOpen = tokens.get(0);
        assertThat(tagOpen.type()).isEqualTo(TokenType.TAG_OPEN);
        assertThat(tagOpen.line()).isEqualTo(1);
        assertThat(tagOpen.column()).isEqualTo(1);

        // 'if' keyword should start at column 4 (after "{% ")
        Token ifKeyword = tokens.get(1);
        assertThat(ifKeyword.type()).isEqualTo(TokenType.KEYWORD);
        assertThat(ifKeyword.lexeme()).isEqualTo("if");
        assertThat(ifKeyword.line()).isEqualTo(1);
        assertThat(ifKeyword.column()).isEqualTo(4);

        // 'condition' identifier should start at column 7 (after "if ")
        Token condition = tokens.get(2);
        assertThat(condition.type()).isEqualTo(TokenType.IDENT);
        assertThat(condition.lexeme()).isEqualTo("condition");
        assertThat(condition.line()).isEqualTo(1);
        assertThat(condition.column()).isEqualTo(7);

        // First tag close should start at column 17 (after "condition ")
        Token firstTagClose = tokens.get(3);
        assertThat(firstTagClose.type()).isEqualTo(TokenType.TAG_CLOSE);
        assertThat(firstTagClose.line()).isEqualTo(1);
        assertThat(firstTagClose.column()).isEqualTo(17);

        // Text should start at column 19 (after "{% ")
        Token text = tokens.get(4);
        assertThat(text.type()).isEqualTo(TokenType.TEXT);
        assertThat(text.lexeme()).isEqualTo("Text");
        assertThat(text.line()).isEqualTo(1);
        assertThat(text.column()).isEqualTo(19);

        // Second tag open should start at column 23 (after "Text")
        Token secondTagOpen = tokens.get(5);
        assertThat(secondTagOpen.type()).isEqualTo(TokenType.TAG_OPEN);
        assertThat(secondTagOpen.line()).isEqualTo(1);
        assertThat(secondTagOpen.column()).isEqualTo(23);

        // 'endif' keyword should start at column 26 (after "{% ")
        Token endifKeyword = tokens.get(6);
        assertThat(endifKeyword.type()).isEqualTo(TokenType.KEYWORD);
        assertThat(endifKeyword.lexeme()).isEqualTo("endif");
        assertThat(endifKeyword.line()).isEqualTo(1);
        assertThat(endifKeyword.column()).isEqualTo(26);

        // Second tag close should start at column 32 (after "endif ")
        Token secondTagClose = tokens.get(7);
        assertThat(secondTagClose.type()).isEqualTo(TokenType.TAG_CLOSE);
        assertThat(secondTagClose.line()).isEqualTo(1);
        assertThat(secondTagClose.column()).isEqualTo(32);
    }

    @Test
    public void testStringColumnTracking() {
        String template = "{{ \"Hello World\" }}";
        Lexer lexer = new Lexer(template);
        List<Token> tokens = lexer.lex();

        // Object open should start at column 1
        Token objectOpen = tokens.get(0);
        assertThat(objectOpen.type()).isEqualTo(TokenType.OBJECT_OPEN);
        assertThat(objectOpen.line()).isEqualTo(1);
        assertThat(objectOpen.column()).isEqualTo(1);

        // String should start at column 4 (after "{{ ")
        Token stringToken = tokens.get(1);
        assertThat(stringToken.type()).isEqualTo(TokenType.STRING);
        assertThat(stringToken.lexeme()).isEqualTo("Hello World");
        assertThat(stringToken.line()).isEqualTo(1);
        assertThat(stringToken.column()).isEqualTo(4);

        // Object close should start at column 16 (after "Hello World" ")
        Token objectClose = tokens.get(2);
        assertThat(objectClose.type()).isEqualTo(TokenType.OBJECT_CLOSE);
        assertThat(objectClose.line()).isEqualTo(1);
        assertThat(objectClose.column()).isEqualTo(18);
    }

    @Test
    public void testNestedColumnTracking() {
        String template = "{{ user.name | upcase }}";
        Lexer lexer = new Lexer(template);
        List<Token> tokens = lexer.lex();

        // Object open should start at column 1
        Token objectOpen = tokens.get(0);
        assertThat(objectOpen.type()).isEqualTo(TokenType.OBJECT_OPEN);
        assertThat(objectOpen.line()).isEqualTo(1);
        assertThat(objectOpen.column()).isEqualTo(1);

        // 'user' identifier should start at column 4 (after "{{ ")
        Token user = tokens.get(1);
        assertThat(user.type()).isEqualTo(TokenType.IDENT);
        assertThat(user.lexeme()).isEqualTo("user");
        assertThat(user.line()).isEqualTo(1);
        assertThat(user.column()).isEqualTo(4);

        // Dot should start at column 8 (after "user")
        Token dot = tokens.get(2);
        assertThat(dot.type()).isEqualTo(TokenType.DOT);
        assertThat(dot.line()).isEqualTo(1);
        assertThat(dot.column()).isEqualTo(8);

        // 'name' identifier should start at column 9 (after ".")
        Token name = tokens.get(3);
        assertThat(name.type()).isEqualTo(TokenType.IDENT);
        assertThat(name.lexeme()).isEqualTo("name");
        assertThat(name.line()).isEqualTo(1);
        assertThat(name.column()).isEqualTo(9);

        // Pipe should start at column 14 (after "name ")
        Token pipe = tokens.get(4);
        assertThat(pipe.type()).isEqualTo(TokenType.PIPE);
        assertThat(pipe.line()).isEqualTo(1);
        assertThat(pipe.column()).isEqualTo(14);

        // 'upcase' identifier should start at column 16 (after "| ")
        Token upcase = tokens.get(5);
        assertThat(upcase.type()).isEqualTo(TokenType.IDENT);
        assertThat(upcase.lexeme()).isEqualTo("upcase");
        assertThat(upcase.line()).isEqualTo(1);
        assertThat(upcase.column()).isEqualTo(16);

        // Object close should start at column 22 (after "upcase ")
        Token objectClose = tokens.get(6);
        assertThat(objectClose.type()).isEqualTo(TokenType.OBJECT_CLOSE);
        assertThat(objectClose.line()).isEqualTo(1);
        assertThat(objectClose.column()).isEqualTo(23);
    }

    @Test
    public void testWhitespaceControlColumnTracking() {
        String template = "{{- name -}}";
        Lexer lexer = new Lexer(template);
        List<Token> tokens = lexer.lex();

        // Object open with whitespace control should start at column 1
        Token objectOpen = tokens.get(0);
        assertThat(objectOpen.type()).isEqualTo(TokenType.OBJECT_OPEN_WS);
        assertThat(objectOpen.lexeme()).isEqualTo("{{-");
        assertThat(objectOpen.line()).isEqualTo(1);
        assertThat(objectOpen.column()).isEqualTo(1);

        // Identifier should start at column 5 (after "{{- ")
        Token identifier = tokens.get(1);
        assertThat(identifier.type()).isEqualTo(TokenType.IDENT);
        assertThat(identifier.lexeme()).isEqualTo("name");
        assertThat(identifier.line()).isEqualTo(1);
        assertThat(identifier.column()).isEqualTo(5);

        // Object close with whitespace control should start at column 10 (after "name")
        Token objectClose = tokens.get(3);
        assertThat(objectClose.type()).isEqualTo(TokenType.OBJECT_CLOSE_WS);
        assertThat(objectClose.lexeme()).isEqualTo("-}}");
        assertThat(objectClose.line()).isEqualTo(1);
        assertThat(objectClose.column()).isEqualTo(10);
    }
}
