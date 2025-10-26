package io.github.liquidTruffle.parser;

import io.github.liquidTruffle.parser.ast.AstNode;
import io.github.liquidTruffle.parser.ast.nodes.*;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

public class LiquidParserFacadeTest {
    @Test
    public void canParseTextAndObjectReference() {
        // Test the parser logic without creating LiquidRootNode
        LiquidParserFacade parser = new LiquidParserFacade();
        var nodes = parser.parseNodes(new StringReader("Hello {{ name }}, welcome to our site!"));

        // Assert the correct AST structure
        assertThat(nodes).hasSize(3);

        // First child should be TextNode with "Hello "
        assertTextNode(nodes.get(0), "Hello ");

        // Second child should be LiquidObjectNode with VariableNode child
        LiquidObjectNode objectNode = assertAndCast(nodes.get(1), LiquidObjectNode.class);
        VariableRefNode variableNode = assertAndCast(objectNode.getChild(), VariableRefNode.class);
        assertThat(variableNode.getName()).isEqualTo("name");
        assertThat(nodes.get(1)).isInstanceOf(LiquidObjectNode.class);

        // Third child should be TextNode with ", welcome to our site!"
        assertTextNode(nodes.get(2), ", welcome to our site!");
    }

    @Test
    public void canParseTextAndFilter() {
        LiquidParserFacade parser = new LiquidParserFacade();
        var nodes = parser.parseNodes(new StringReader("Hello {{ name | append }}, welcome to our site!"));
        
        assertThat(nodes).hasSize(3);
        assertTextNode(nodes.get(0), "Hello ");

        LiquidObjectNode objectNode = assertAndCast(nodes.get(1), LiquidObjectNode.class);
        // With binary tree structure, the child should be a FilterNode
        FilterNode filterNode = assertAndCast(objectNode.getChild(), FilterNode.class);
        assertThat(filterNode.getFilterFunction().name()).isEqualTo("append");
        
        // The left child should be the variable
        VariableRefNode variableNode = assertAndCast(filterNode.getInputValue(), VariableRefNode.class);
        assertThat(variableNode.getName()).isEqualTo("name");

        assertTextNode(nodes.get(2), ", welcome to our site!");
    }

    @Test
    public void canParseFilterWithParameters() {
        LiquidParserFacade parser = new LiquidParserFacade();
        var nodes = parser.parseNodes(new StringReader("{{ \"ruby\" | append: \"red\" | capitalize }}"));
        
        assertThat(nodes).hasSize(1);
        
        LiquidObjectNode objectNode = assertAndCast(nodes.getFirst(), LiquidObjectNode.class);

        FilterNode filterNode = assertAndCast(objectNode.getChild(), FilterNode.class);
        assertThat(filterNode.getFilterFunction().name()).isEqualTo("capitalize");

        filterNode = assertAndCast(filterNode.getInputValue(), FilterNode.class);
        assertThat(filterNode.getFilterFunction().name()).isEqualTo("append");
        
        StringLiteralNode stringNode = assertAndCast(filterNode.getInputValue(), StringLiteralNode.class);
        assertThat(stringNode.getStringValue()).isEqualTo("ruby");
        
        assertThat(filterNode.getParameters().length).isEqualTo(1);
        StringLiteralNode paramNode = assertAndCast(filterNode.getParameters()[0], StringLiteralNode.class);
        assertThat(paramNode.getStringValue()).isEqualTo("red");
    }

    @Test
    public void canParseFilterWithMultipleParameters() {
        LiquidParserFacade parser = new LiquidParserFacade();
        var nodes = parser.parseNodes(new StringReader("{{ \"hello\" | replace: \"hello\", \"!\" }}"));
        
        assertThat(nodes).hasSize(1);
        
        LiquidObjectNode objectNode = assertAndCast(nodes.getFirst(), LiquidObjectNode.class);
        // With binary tree structure, the child should be a FilterNode
        FilterNode filterNode = assertAndCast(objectNode.getChild(), FilterNode.class);
        assertThat(filterNode.getFilterFunction().name()).isEqualTo("replace");
        
        // The left child should be the string literal
        StringLiteralNode stringNode = assertAndCast(filterNode.getInputValue(), StringLiteralNode.class);
        assertThat(stringNode.getStringValue()).isEqualTo("hello");
        
        // The right children should contain the parameters
        assertThat(filterNode.getParameters().length).isEqualTo(2);
        StringLiteralNode param1 = assertAndCast(filterNode.getParameters()[0], StringLiteralNode.class);
        assertThat(param1.getStringValue()).isEqualTo("hello");
        
        StringLiteralNode param2 = assertAndCast(filterNode.getParameters()[1], StringLiteralNode.class);
        assertThat(param2.getStringValue()).isEqualTo("!");
    }

    @Test
    public void canParseFilterWithNumericParameters() {
        LiquidParserFacade parser = new LiquidParserFacade();
        var nodes = parser.parseNodes(new StringReader("{{ items | limit: 5 }}"));

        assertThat(nodes).hasSize(1);

        LiquidObjectNode objectNode = assertAndCast(nodes.getFirst(), LiquidObjectNode.class);
        // With binary tree structure, the child should be a FilterNode
        FilterNode filterNode = assertAndCast(objectNode.getChild(), FilterNode.class);
        assertThat(filterNode.getFilterFunction().name()).isEqualTo("limit");

        // The left child should be the variable
        VariableRefNode variableNode = assertAndCast(filterNode.getInputValue(), VariableRefNode.class);
        assertThat(variableNode.getName()).isEqualTo("items");

        // The right children should contain the parameters
        assertThat(filterNode.getParameters().length).isEqualTo(1);
        NumberLiteralNode paramNode = assertAndCast(filterNode.getParameters()[0], NumberLiteralNode.class);
        assertThat(paramNode.getNumberValue()).isEqualTo(5);
    }

    @Test
    public void canParseIfTag() {
        LiquidParserFacade parser = new LiquidParserFacade();
        var nodes = parser.parseNodes(new StringReader("{% if \"str\" %}hello world{% endif %}"));

        assertThat(nodes).hasSize(1);
        
        IfNode ifNode = assertAndCast(nodes.getFirst(), IfNode.class);

        StringLiteralNode condition = assertAndCast(ifNode.getCondition(), StringLiteralNode.class);
        assertThat(condition.getStringValue()).isEqualTo("str");
        
        TextNode textNode = assertAndCast(ifNode.getBody()[0], TextNode.class);
        assertThat(textNode.getTextContent()).isEqualTo("hello world");
    }

    private static <T extends AstNode> T assertAndCast(AstNode node, Class<T> clazz) {
        assertThat(node).isInstanceOf(clazz);
        return clazz.cast(node);
    }

    private static void assertTextNode(AstNode node, String Hello_) {
        TextNode textNode1 = assertAndCast(node, TextNode.class);
        assertThat(textNode1.getTextContent()).isEqualTo(Hello_);
    }
}