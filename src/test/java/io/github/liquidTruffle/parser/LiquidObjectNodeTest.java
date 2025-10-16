package io.github.liquidTruffle.parser;

import io.github.liquidTruffle.parser.ast.nodes.LiquidObjectNode;
import io.github.liquidTruffle.parser.ast.nodes.NumberLiteralNode;
import io.github.liquidTruffle.parser.ast.nodes.StringLiteralNode;
import io.github.liquidTruffle.parser.ast.nodes.VariableRefNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.StringReader;

public class LiquidObjectNodeTest {

    @Test
    public void canParseObjectWithStringLiteral() {
        LiquidParserFacade parser = new LiquidParserFacade();
        var nodes = parser.parseNodes(new StringReader("{{ \"hello\" }}"));

        Assertions.assertThat(nodes).hasSize(1);
        Assertions.assertThat(nodes.get(0)).isInstanceOf(LiquidObjectNode.class);

        LiquidObjectNode objectNode = (LiquidObjectNode) nodes.get(0);
        Assertions.assertThat(objectNode.getChildNode()).isInstanceOf(StringLiteralNode.class);

        StringLiteralNode stringLiteral = (StringLiteralNode) objectNode.getChildNode();
        Assertions.assertThat(stringLiteral.getStringValue()).isEqualTo("hello");
    }

    @Test
    public void canParseObjectWithVariable() {
        LiquidParserFacade parser = new LiquidParserFacade();
        var nodes = parser.parseNodes(new StringReader("{{ name }}"));

        Assertions.assertThat(nodes).hasSize(1);
        Assertions.assertThat(nodes.get(0)).isInstanceOf(LiquidObjectNode.class);

        LiquidObjectNode objectNode = (LiquidObjectNode) nodes.get(0);
        Assertions.assertThat(objectNode.getChildNode()).isInstanceOf(VariableRefNode.class);

        VariableRefNode variableNode = (VariableRefNode) objectNode.getChildNode();
        Assertions.assertThat(variableNode.getName()).isEqualTo("name");
    }

    @Test
    public void canParseObjectWithNumberLiteral() {
        LiquidParserFacade parser = new LiquidParserFacade();
        var nodes = parser.parseNodes(new StringReader("{{ 42 }}"));

        Assertions.assertThat(nodes).hasSize(1);
        Assertions.assertThat(nodes.get(0)).isInstanceOf(LiquidObjectNode.class);

        LiquidObjectNode objectNode = (LiquidObjectNode) nodes.get(0);
        Assertions.assertThat(objectNode.getChildNode()).isInstanceOf(NumberLiteralNode.class);
    }

    @Test
    public void canParseMixedContentWithObjects() {
        LiquidParserFacade parser = new LiquidParserFacade();
        var nodes = parser.parseNodes(new StringReader("Hello {{ name }}, you have {{ count }} messages!"));

        Assertions.assertThat(nodes).hasSize(5);

        // First text node
        Assertions.assertThat(nodes.get(0).getClass().getSimpleName()).isEqualTo("TextNode");

        // First object node with variable
        Assertions.assertThat(nodes.get(1)).isInstanceOf(LiquidObjectNode.class);
        LiquidObjectNode objectNode1 = (LiquidObjectNode) nodes.get(1);
        Assertions.assertThat(objectNode1.getChildNode()).isInstanceOf(VariableRefNode.class);

        // Text node
        Assertions.assertThat(nodes.get(2).getClass().getSimpleName()).isEqualTo("TextNode");

        // Second object node with variable
        Assertions.assertThat(nodes.get(3)).isInstanceOf(LiquidObjectNode.class);
        LiquidObjectNode objectNode2 = (LiquidObjectNode) nodes.get(3);
        Assertions.assertThat(objectNode2.getChildNode()).isInstanceOf(VariableRefNode.class);

        // Final text node
        Assertions.assertThat(nodes.get(4).getClass().getSimpleName()).isEqualTo("TextNode");
    }
}