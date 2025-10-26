package io.github.liquidTruffle.lexer;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

public class LexerLineNumberTest {

    @Test
    public void testBasicMultilineTemplate() {
        String template = "Hello World\n{{ name }}\nGoodbye";
        Lexer lexer = new Lexer(template);
        List<Token> tokens = lexer.lex();

        // First text token should be on line 1 (where it starts)
        Token firstText = tokens.get(0);
        assertThat(firstText.type()).isEqualTo(TokenType.TEXT);
        assertThat(firstText.lexeme()).isEqualTo("Hello World\n");
        assertThat(firstText.line()).isEqualTo(1);

        // Object open should be on line 2
        Token objectOpen = tokens.get(1);
        assertThat(objectOpen.type()).isEqualTo(TokenType.OBJECT_OPEN);
        assertThat(objectOpen.line()).isEqualTo(2);

        // Identifier should be on line 2
        Token identifier = tokens.get(2);
        assertThat(identifier.type()).isEqualTo(TokenType.IDENT);
        assertThat(identifier.lexeme()).isEqualTo("name");
        assertThat(identifier.line()).isEqualTo(2);

        // Object close should be on line 2
        Token objectClose = tokens.get(3);
        assertThat(objectClose.type()).isEqualTo(TokenType.OBJECT_CLOSE);
        assertThat(objectClose.line()).isEqualTo(2);

        // Second text token should be on line 2 (where it starts)
        Token secondText = tokens.get(4);
        assertThat(secondText.type()).isEqualTo(TokenType.TEXT);
        assertThat(secondText.lexeme()).isEqualTo("\nGoodbye");
        assertThat(secondText.line()).isEqualTo(2);
    }

    @Test
    public void testMultilineComment() {
        String template = "Hello\n{# This is a \n  multiline \n  comment \n#}\nWorld";
        Lexer lexer = new Lexer(template);
        List<Token> tokens = lexer.lex();
        
        // First text should be on line 1 (where it starts)
        Token firstText = tokens.get(0);
        assertThat(firstText.type()).isEqualTo(TokenType.TEXT);
        assertThat(firstText.lexeme()).isEqualTo("Hello\n");
        assertThat(firstText.line()).isEqualTo(1);
        
        // Second text should be on line 5 (where it starts)
        Token secondText = tokens.get(1);
        assertThat(secondText.type()).isEqualTo(TokenType.TEXT);
        assertThat(secondText.lexeme()).isEqualTo("\nWorld");
        assertThat(secondText.line()).isEqualTo(5);
    }

    @Test
    public void testMultilineTagExpression() {
        String template = "{% \n  if \n  condition \n%}\nContent\n{% \n  endif \n%}";
        Lexer lexer = new Lexer(template);
        List<Token> tokens = lexer.lex();
        
        // First tag open should be on line 1
        Token firstTagOpen = tokens.get(0);
        assertThat(firstTagOpen.type()).isEqualTo(TokenType.TAG_OPEN);
        assertThat(firstTagOpen.line()).isEqualTo(1);
        
        // 'if' keyword should be on line 2
        Token ifKeyword = tokens.get(1);
        assertThat(ifKeyword.type()).isEqualTo(TokenType.KEYWORD);
        assertThat(ifKeyword.lexeme()).isEqualTo("if");
        assertThat(ifKeyword.line()).isEqualTo(2);
        
        // 'condition' identifier should be on line 3
        Token condition = tokens.get(2);
        assertThat(condition.type()).isEqualTo(TokenType.IDENT);
        assertThat(condition.lexeme()).isEqualTo("condition");
        assertThat(condition.line()).isEqualTo(3);
        
        // First tag close should be on line 4
        Token firstTagClose = tokens.get(3);
        assertThat(firstTagClose.type()).isEqualTo(TokenType.TAG_CLOSE);
        assertThat(firstTagClose.line()).isEqualTo(4);
        
        // Content text should be on line 4 (where it starts)
        Token content = tokens.get(4);
        assertThat(content.type()).isEqualTo(TokenType.TEXT);
        assertThat(content.lexeme()).isEqualTo("\nContent\n");
        assertThat(content.line()).isEqualTo(4);
        
        // Second tag open should be on line 6
        Token secondTagOpen = tokens.get(5);
        assertThat(secondTagOpen.type()).isEqualTo(TokenType.TAG_OPEN);
        assertThat(secondTagOpen.line()).isEqualTo(6);
        
        // 'endif' keyword should be on line 7
        Token endifKeyword = tokens.get(6);
        assertThat(endifKeyword.type()).isEqualTo(TokenType.KEYWORD);
        assertThat(endifKeyword.lexeme()).isEqualTo("endif");
        assertThat(endifKeyword.line()).isEqualTo(7);
        
        // Second tag close should be on line 8
        Token secondTagClose = tokens.get(7);
        assertThat(secondTagClose.type()).isEqualTo(TokenType.TAG_CLOSE);
        assertThat(secondTagClose.line()).isEqualTo(8);
    }

    @Test
    public void testMultilineStringLiteral() {
        String template = "{{ \n  \"This is a \n  multiline \n  string\" \n}}";
        Lexer lexer = new Lexer(template);
        List<Token> tokens = lexer.lex();
        
        // Object open should be on line 1
        Token objectOpen = tokens.get(0);
        assertThat(objectOpen.type()).isEqualTo(TokenType.OBJECT_OPEN);
        assertThat(objectOpen.line()).isEqualTo(1);
        
        // String should be on line 2 (where it starts)
        Token stringToken = tokens.get(1);
        assertThat(stringToken.type()).isEqualTo(TokenType.STRING);
        assertThat(stringToken.lexeme()).isEqualTo("This is a \n  multiline \n  string");
        assertThat(stringToken.line()).isEqualTo(2);
        
        // Object close should be on line 5
        Token objectClose = tokens.get(2);
        assertThat(objectClose.type()).isEqualTo(TokenType.OBJECT_CLOSE);
        assertThat(objectClose.line()).isEqualTo(5);
    }

    @Test
    public void testComplexMultilineTemplate() {
        String template = "Header\n" +
                         "{% if user %}\n" +
                         "  Welcome {{ user.name | \n" +
                         "    upcase }}\n" +
                         "  {% for item in \n" +
                         "    items %}\n" +
                         "    {{ item.title }}\n" +
                         "  {% endfor %}\n" +
                         "{% else %}\n" +
                         "  Please login\n" +
                         "{% endif %}\n" +
                         "Footer";
        
        Lexer lexer = new Lexer(template);
        List<Token> tokens = lexer.lex();
        
        // Header text should be on line 1 (where it starts)
        Token header = tokens.get(0);
        assertThat(header.type()).isEqualTo(TokenType.TEXT);
        assertThat(header.lexeme()).isEqualTo("Header\n");
        assertThat(header.line()).isEqualTo(1);
        
        // First if tag should be on line 2
        Token ifTag = tokens.get(1);
        assertThat(ifTag.type()).isEqualTo(TokenType.TAG_OPEN);
        assertThat(ifTag.line()).isEqualTo(2);
        
        // 'if' keyword should be on line 2
        Token ifKeyword = tokens.get(2);
        assertThat(ifKeyword.type()).isEqualTo(TokenType.KEYWORD);
        assertThat(ifKeyword.line()).isEqualTo(2);
        
        // 'user' identifier should be on line 2
        Token userIdent = tokens.get(3);
        assertThat(userIdent.type()).isEqualTo(TokenType.IDENT);
        assertThat(userIdent.lexeme()).isEqualTo("user");
        assertThat(userIdent.line()).isEqualTo(2);
        
        // First tag close should be on line 2
        Token firstTagClose = tokens.get(4);
        assertThat(firstTagClose.type()).isEqualTo(TokenType.TAG_CLOSE);
        assertThat(firstTagClose.line()).isEqualTo(2);
        
        // Welcome text should be on line 2 (where it starts)
        Token welcomeText = tokens.get(5);
        assertThat(welcomeText.type()).isEqualTo(TokenType.TEXT);
        assertThat(welcomeText.lexeme()).isEqualTo("\n  Welcome ");
        assertThat(welcomeText.line()).isEqualTo(2);
        
        // Object open should be on line 3
        Token objectOpen = tokens.get(6);
        assertThat(objectOpen.type()).isEqualTo(TokenType.OBJECT_OPEN);
        assertThat(objectOpen.line()).isEqualTo(3);
        
        // user.name should be on line 3
        Token userDotName = tokens.get(7);
        assertThat(userDotName.type()).isEqualTo(TokenType.IDENT);
        assertThat(userDotName.lexeme()).isEqualTo("user");
        assertThat(userDotName.line()).isEqualTo(3);
        
        // Dot should be on line 3
        Token dot = tokens.get(8);
        assertThat(dot.type()).isEqualTo(TokenType.DOT);
        assertThat(dot.line()).isEqualTo(3);
        
        // name should be on line 3
        Token name = tokens.get(9);
        assertThat(name.type()).isEqualTo(TokenType.IDENT);
        assertThat(name.lexeme()).isEqualTo("name");
        assertThat(name.line()).isEqualTo(3);
        
        // Pipe should be on line 3
        Token pipe = tokens.get(10);
        assertThat(pipe.type()).isEqualTo(TokenType.PIPE);
        assertThat(pipe.line()).isEqualTo(3);
        
        // upcase should be on line 4
        Token upcase = tokens.get(11);
        assertThat(upcase.type()).isEqualTo(TokenType.IDENT);
        assertThat(upcase.lexeme()).isEqualTo("upcase");
        assertThat(upcase.line()).isEqualTo(4);
        
        // Object close should be on line 4
        Token objectClose = tokens.get(12);
        assertThat(objectClose.type()).isEqualTo(TokenType.OBJECT_CLOSE);
        assertThat(objectClose.line()).isEqualTo(4);
    }

    @Test
    public void testLineNumbersWithWhitespaceControl() {
        String template = "{{- \n  name \n-}}\nText";
        Lexer lexer = new Lexer(template);
        List<Token> tokens = lexer.lex();
        
        // Object open with whitespace control should be on line 1
        Token objectOpen = tokens.get(0);
        assertThat(objectOpen.type()).isEqualTo(TokenType.OBJECT_OPEN_WS);
        assertThat(objectOpen.line()).isEqualTo(1);
        
        // Identifier should be on line 2
        Token identifier = tokens.get(1);
        assertThat(identifier.type()).isEqualTo(TokenType.IDENT);
        assertThat(identifier.lexeme()).isEqualTo("name");
        assertThat(identifier.line()).isEqualTo(2);
        
        // Minus should be on line 3
        Token minus = tokens.get(2);
        assertThat(minus.type()).isEqualTo(TokenType.MINUS);
        assertThat(minus.line()).isEqualTo(3);
        
        // Object close with whitespace control should be on line 3
        Token objectClose = tokens.get(3);
        assertThat(objectClose.type()).isEqualTo(TokenType.OBJECT_CLOSE_WS);
        assertThat(objectClose.line()).isEqualTo(3);
        
        // Text should be on line 3 (where it starts)
        Token text = tokens.get(4);
        assertThat(text.type()).isEqualTo(TokenType.TEXT);
        assertThat(text.lexeme()).isEqualTo("\nText");
        assertThat(text.line()).isEqualTo(3);
    }

    @Test
    public void testLineNumbersWithEmptyLines() {
        String template = "Line 1\n\n\nLine 4\n{{ variable }}\n\nLine 7";
        Lexer lexer = new Lexer(template);
        List<Token> tokens = lexer.lex();
        
        // First text should be on line 1 (where it starts)
        Token firstText = tokens.get(0);
        assertThat(firstText.type()).isEqualTo(TokenType.TEXT);
        assertThat(firstText.lexeme()).isEqualTo("Line 1\n\n\nLine 4\n");
        assertThat(firstText.line()).isEqualTo(1);
        
        // Object open should be on line 5
        Token objectOpen = tokens.get(1);
        assertThat(objectOpen.type()).isEqualTo(TokenType.OBJECT_OPEN);
        assertThat(objectOpen.line()).isEqualTo(5);
        
        // Variable should be on line 5
        Token variable = tokens.get(2);
        assertThat(variable.type()).isEqualTo(TokenType.IDENT);
        assertThat(variable.lexeme()).isEqualTo("variable");
        assertThat(variable.line()).isEqualTo(5);
        
        // Object close should be on line 5
        Token objectClose = tokens.get(3);
        assertThat(objectClose.type()).isEqualTo(TokenType.OBJECT_CLOSE);
        assertThat(objectClose.line()).isEqualTo(5);
        
        // Last text should be on line 5 (where it starts)
        Token lastText = tokens.get(4);
        assertThat(lastText.type()).isEqualTo(TokenType.TEXT);
        assertThat(lastText.lexeme()).isEqualTo("\n\nLine 7");
        assertThat(lastText.line()).isEqualTo(5);
    }

    @Test
    public void testLineNumbersWithNestedStructures() {
        String template = "{% for item in items %}\n" +
                         "  {% if item.active %}\n" +
                         "    {{ item.name | \n" +
                         "      upcase }}\n" +
                         "  {% endif %}\n" +
                         "{% endfor %}";
        
        Lexer lexer = new Lexer(template);
        List<Token> tokens = lexer.lex();
        
        // First for tag should be on line 1
        Token forTag = tokens.get(0);
        assertThat(forTag.type()).isEqualTo(TokenType.TAG_OPEN);
        assertThat(forTag.line()).isEqualTo(1);
        
        // 'for' keyword should be on line 1
        Token forKeyword = tokens.get(1);
        assertThat(forKeyword.type()).isEqualTo(TokenType.KEYWORD);
        assertThat(forKeyword.line()).isEqualTo(1);
        
        // 'item' identifier should be on line 1
        Token itemIdent = tokens.get(2);
        assertThat(itemIdent.type()).isEqualTo(TokenType.IDENT);
        assertThat(itemIdent.line()).isEqualTo(1);
        
        // 'in' keyword should be on line 1
        Token inKeyword = tokens.get(3);
        assertThat(inKeyword.type()).isEqualTo(TokenType.KEYWORD);
        assertThat(inKeyword.line()).isEqualTo(1);
        
        // 'items' identifier should be on line 1
        Token itemsIdent = tokens.get(4);
        assertThat(itemsIdent.type()).isEqualTo(TokenType.IDENT);
        assertThat(itemsIdent.line()).isEqualTo(1);
        
        // First tag close should be on line 1
        Token firstTagClose = tokens.get(5);
        assertThat(firstTagClose.type()).isEqualTo(TokenType.TAG_CLOSE);
        assertThat(firstTagClose.line()).isEqualTo(1);
        
        // Text token should be on line 1 (where it starts)
        Token text1 = tokens.get(6);
        assertThat(text1.type()).isEqualTo(TokenType.TEXT);
        assertThat(text1.lexeme()).isEqualTo("\n  ");
        assertThat(text1.line()).isEqualTo(1);
        
        // Second if tag should be on line 2
        Token ifTag = tokens.get(7);
        assertThat(ifTag.type()).isEqualTo(TokenType.TAG_OPEN);
        assertThat(ifTag.line()).isEqualTo(2);
        
        // 'if' keyword should be on line 2
        Token ifKeyword = tokens.get(8);
        assertThat(ifKeyword.type()).isEqualTo(TokenType.KEYWORD);
        assertThat(ifKeyword.line()).isEqualTo(2);
        
        // item.active should be on line 2
        Token itemDotActive = tokens.get(9);
        assertThat(itemDotActive.type()).isEqualTo(TokenType.IDENT);
        assertThat(itemDotActive.lexeme()).isEqualTo("item");
        assertThat(itemDotActive.line()).isEqualTo(2);
        
        // Dot should be on line 2
        Token dot = tokens.get(10);
        assertThat(dot.type()).isEqualTo(TokenType.DOT);
        assertThat(dot.line()).isEqualTo(2);
        
        // 'active' identifier should be on line 2
        Token activeIdent = tokens.get(11);
        assertThat(activeIdent.type()).isEqualTo(TokenType.IDENT);
        assertThat(activeIdent.lexeme()).isEqualTo("active");
        assertThat(activeIdent.line()).isEqualTo(2);
        
        // Second tag close should be on line 2
        Token secondTagClose = tokens.get(12);
        assertThat(secondTagClose.type()).isEqualTo(TokenType.TAG_CLOSE);
        assertThat(secondTagClose.line()).isEqualTo(2);
        
        // Text token should be on line 2 (where it starts)
        Token text2 = tokens.get(13);
        assertThat(text2.type()).isEqualTo(TokenType.TEXT);
        assertThat(text2.lexeme()).isEqualTo("\n    ");
        assertThat(text2.line()).isEqualTo(2);
        
        // Object open should be on line 3
        Token objectOpen = tokens.get(14);
        assertThat(objectOpen.type()).isEqualTo(TokenType.OBJECT_OPEN);
        assertThat(objectOpen.line()).isEqualTo(3);
        
        // item.name should be on line 3
        Token itemDotName = tokens.get(15);
        assertThat(itemDotName.type()).isEqualTo(TokenType.IDENT);
        assertThat(itemDotName.lexeme()).isEqualTo("item");
        assertThat(itemDotName.line()).isEqualTo(3);
        
        // Dot should be on line 3
        Token dot2 = tokens.get(16);
        assertThat(dot2.type()).isEqualTo(TokenType.DOT);
        assertThat(dot2.line()).isEqualTo(3);
        
        // name should be on line 3
        Token name = tokens.get(17);
        assertThat(name.type()).isEqualTo(TokenType.IDENT);
        assertThat(name.lexeme()).isEqualTo("name");
        assertThat(name.line()).isEqualTo(3);
        
        // Pipe should be on line 3
        Token pipe = tokens.get(18);
        assertThat(pipe.type()).isEqualTo(TokenType.PIPE);
        assertThat(pipe.line()).isEqualTo(3);
        
        // upcase should be on line 4
        Token upcase = tokens.get(19);
        assertThat(upcase.type()).isEqualTo(TokenType.IDENT);
        assertThat(upcase.lexeme()).isEqualTo("upcase");
        assertThat(upcase.line()).isEqualTo(4);
        
        // Object close should be on line 4
        Token objectClose = tokens.get(20);
        assertThat(objectClose.type()).isEqualTo(TokenType.OBJECT_CLOSE);
        assertThat(objectClose.line()).isEqualTo(4);
        
        // Text token should be on line 4 (where it starts)
        Token text3 = tokens.get(21);
        assertThat(text3.type()).isEqualTo(TokenType.TEXT);
        assertThat(text3.lexeme()).isEqualTo("\n  ");
        assertThat(text3.line()).isEqualTo(4);
        
        // Third tag open should be on line 5
        Token endifTag = tokens.get(22);
        assertThat(endifTag.type()).isEqualTo(TokenType.TAG_OPEN);
        assertThat(endifTag.line()).isEqualTo(5);
        
        // 'endif' keyword should be on line 5
        Token endifKeyword = tokens.get(23);
        assertThat(endifKeyword.type()).isEqualTo(TokenType.KEYWORD);
        assertThat(endifKeyword.line()).isEqualTo(5);
        
        // Third tag close should be on line 5
        Token thirdTagClose = tokens.get(24);
        assertThat(thirdTagClose.type()).isEqualTo(TokenType.TAG_CLOSE);
        assertThat(thirdTagClose.line()).isEqualTo(5);
        
        // Text token should be on line 5 (where it starts)
        Token text4 = tokens.get(25);
        assertThat(text4.type()).isEqualTo(TokenType.TEXT);
        assertThat(text4.lexeme()).isEqualTo("\n");
        assertThat(text4.line()).isEqualTo(5);
        
        // Fourth tag open should be on line 6
        Token endforTag = tokens.get(26);
        assertThat(endforTag.type()).isEqualTo(TokenType.TAG_OPEN);
        assertThat(endforTag.line()).isEqualTo(6);
        
        // 'endfor' keyword should be on line 6
        Token endforKeyword = tokens.get(27);
        assertThat(endforKeyword.type()).isEqualTo(TokenType.KEYWORD);
        assertThat(endforKeyword.line()).isEqualTo(6);
        
        // Fourth tag close should be on line 6
        Token fourthTagClose = tokens.get(28);
        assertThat(fourthTagClose.type()).isEqualTo(TokenType.TAG_CLOSE);
        assertThat(fourthTagClose.line()).isEqualTo(6);
    }
}
