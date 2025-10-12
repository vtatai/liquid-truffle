package io.github.liquidTruffle.parser;

import io.github.liquidTruffle.parser.ast.nodes.LiquidObjectNode;
import io.github.liquidTruffle.parser.ast.nodes.TextNode;
import io.github.liquidTruffle.parser.ast.nodes.VariableNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.StringReader;

public class LiquidParserFacadeTest {
    @Test
    public void canParseLiquidTemplateWithTextAndObjectReference() {
        // Test the parser logic without creating LiquidRootNode
        LiquidParserFacade parser = new LiquidParserFacade();
        var nodes = parser.parseNodes(new StringReader("Hello {{ name }}, welcome to our site!"));
        
        // Assert the correct AST structure
        Assertions.assertThat(nodes).hasSize(3);
        
        // First child should be TextNode with "Hello "
        Assertions.assertThat(nodes.get(0)).isInstanceOf(TextNode.class);
        TextNode textNode1 = (TextNode) nodes.get(0);
        Assertions.assertThat(textNode1.getTextContent()).isEqualTo("Hello ");
        
        // Second child should be LiquidObjectNode with VariableNode child
        Assertions.assertThat(nodes.get(1)).isInstanceOf(LiquidObjectNode.class);
        LiquidObjectNode objectNode = (LiquidObjectNode) nodes.get(1);
        Assertions.assertThat(objectNode.getChildNode()).isInstanceOf(VariableNode.class);
        VariableNode variableNode = (VariableNode) objectNode.getChildNode();
        Assertions.assertThat(variableNode.getVariableName()).isEqualTo("name");
        Assertions.assertThat(variableNode.getFilterSpecs()).isEmpty();
        
        // Third child should be TextNode with ", welcome to our site!"
        Assertions.assertThat(nodes.get(2)).isInstanceOf(TextNode.class);
        TextNode textNode2 = (TextNode) nodes.get(2);
        Assertions.assertThat(textNode2.getTextContent()).isEqualTo(", welcome to our site!");
    }
}