package io.github.liquidTruffle.parser;

import io.github.liquidTruffle.parser.ast.AstNode;
import io.github.liquidTruffle.parser.ast.nodes.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.StringReader;
import java.util.List;

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
        VariableRefNode variableNode = assertAndCast(objectNode.getChildNode(), VariableRefNode.class);
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
        VariableRefNode variableNode = assertAndCast(objectNode.getChildNode(), VariableRefNode.class);
        assertThat(variableNode.getName()).isEqualTo("name");
        assertThat(objectNode.getFilters().length).isEqualTo(1);
        FilterNode filterNode = assertAndCast(objectNode.getFilters()[0], FilterNode.class);
        assertThat(filterNode.getFilterFunction().name()).isEqualTo("append");

        assertTextNode(nodes.get(2), ", welcome to our site!");
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