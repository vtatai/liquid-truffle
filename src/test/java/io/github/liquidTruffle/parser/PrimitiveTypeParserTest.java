package io.github.liquidTruffle.parser;

import io.github.liquidTruffle.parser.ast.nodes.BooleanLiteralNode;
import io.github.liquidTruffle.parser.ast.nodes.LiquidObjectNode;
import io.github.liquidTruffle.parser.ast.nodes.NilLiteralNode;
import io.github.liquidTruffle.parser.ast.nodes.NumberLiteralNode;
import io.github.liquidTruffle.parser.ast.nodes.StringLiteralNode;
import io.github.liquidTruffle.parser.ast.nodes.VariableNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.StringReader;

public class PrimitiveTypeParserTest {
    
    @Test
    public void parserCreatesStringLiteralNodes() {
        LiquidParserFacade parser = new LiquidParserFacade();
        var nodes = parser.parseNodes(new StringReader("{{ var | upcase:'hello' }}"));
        
        // Should have a LiquidObjectNode with a VariableNode child
        Assertions.assertThat(nodes).hasSize(1);
        Assertions.assertThat(nodes.get(0)).isInstanceOf(LiquidObjectNode.class);
        
        LiquidObjectNode objectNode = (LiquidObjectNode) nodes.get(0);
        Assertions.assertThat(objectNode.getChildNode()).isInstanceOf(VariableNode.class);
        VariableNode variableNode = (VariableNode) objectNode.getChildNode();
        Assertions.assertThat(variableNode.getFilterSpecs()).hasSize(1);
        Assertions.assertThat(variableNode.getFilterSpecs().get(0).args()).hasSize(1);
        Assertions.assertThat(variableNode.getFilterSpecs().get(0).args().get(0)).isInstanceOf(StringLiteralNode.class);
        
        StringLiteralNode stringNode = (StringLiteralNode) variableNode.getFilterSpecs().get(0).args().get(0);
        Assertions.assertThat(stringNode.getStringValue()).isEqualTo("hello");
    }
    
    @Test
    public void parserCreatesNumberLiteralNodes() {
        LiquidParserFacade parser = new LiquidParserFacade();
        var nodes = parser.parseNodes(new StringReader("{{ count | times: 5 }}"));
        
        Assertions.assertThat(nodes).hasSize(1);
        Assertions.assertThat(nodes.get(0)).isInstanceOf(LiquidObjectNode.class);
        
        LiquidObjectNode objectNode = (LiquidObjectNode) nodes.get(0);
        Assertions.assertThat(objectNode.getChildNode()).isInstanceOf(VariableNode.class);
        VariableNode variableNode = (VariableNode) objectNode.getChildNode();
        Assertions.assertThat(variableNode.getFilterSpecs()).hasSize(1);
        Assertions.assertThat(variableNode.getFilterSpecs().get(0).args()).hasSize(1);
        Assertions.assertThat(variableNode.getFilterSpecs().get(0).args().get(0)).isInstanceOf(NumberLiteralNode.class);
        
        NumberLiteralNode numberNode = (NumberLiteralNode) variableNode.getFilterSpecs().get(0).args().get(0);
        Assertions.assertThat(numberNode.getNumberValue()).isEqualTo(5);
    }
    
    @Test
    public void parserCreatesBooleanLiteralNodes() {
        LiquidParserFacade parser = new LiquidParserFacade();
        var nodes = parser.parseNodes(new StringReader("{{ flag | default:true }}"));
        
        Assertions.assertThat(nodes).hasSize(1);
        Assertions.assertThat(nodes.get(0)).isInstanceOf(LiquidObjectNode.class);
        
        LiquidObjectNode objectNode = (LiquidObjectNode) nodes.get(0);
        Assertions.assertThat(objectNode.getChildNode()).isInstanceOf(VariableNode.class);
        VariableNode variableNode = (VariableNode) objectNode.getChildNode();
        Assertions.assertThat(variableNode.getFilterSpecs()).hasSize(1);
        Assertions.assertThat(variableNode.getFilterSpecs().get(0).args()).hasSize(1);
        Assertions.assertThat(variableNode.getFilterSpecs().get(0).args().get(0)).isInstanceOf(BooleanLiteralNode.class);
        
        BooleanLiteralNode booleanNode = (BooleanLiteralNode) variableNode.getFilterSpecs().get(0).args().get(0);
        Assertions.assertThat(booleanNode.getBooleanValue()).isTrue();
    }
    
    @Test
    public void parserCreatesNullLiteralNodes() {
        LiquidParserFacade parser = new LiquidParserFacade();
        var nodes = parser.parseNodes(new StringReader("{{ value | default:nil }}"));
        
        Assertions.assertThat(nodes).hasSize(1);
        Assertions.assertThat(nodes.get(0)).isInstanceOf(LiquidObjectNode.class);
        
        LiquidObjectNode objectNode = (LiquidObjectNode) nodes.get(0);
        Assertions.assertThat(objectNode.getChildNode()).isInstanceOf(VariableNode.class);
        VariableNode variableNode = (VariableNode) objectNode.getChildNode();
        Assertions.assertThat(variableNode.getFilterSpecs()).hasSize(1);
        Assertions.assertThat(variableNode.getFilterSpecs().get(0).args()).hasSize(1);
        Assertions.assertThat(variableNode.getFilterSpecs().get(0).args().get(0)).isInstanceOf(NilLiteralNode.class);
    }
    
    @Test
    public void parserHandlesMultiplePrimitiveTypesInFilter() {
        LiquidParserFacade parser = new LiquidParserFacade();
        var nodes = parser.parseNodes(new StringReader("{{ text | slice:0,10,true }}"));
        
        Assertions.assertThat(nodes).hasSize(1);
        Assertions.assertThat(nodes.get(0)).isInstanceOf(LiquidObjectNode.class);
        
        LiquidObjectNode objectNode = (LiquidObjectNode) nodes.get(0);
        Assertions.assertThat(objectNode.getChildNode()).isInstanceOf(VariableNode.class);
        VariableNode variableNode = (VariableNode) objectNode.getChildNode();
        Assertions.assertThat(variableNode.getFilterSpecs()).hasSize(1);
        Assertions.assertThat(variableNode.getFilterSpecs().get(0).args()).hasSize(3);
        
        // First argument should be NumberLiteralNode (0)
        Assertions.assertThat(variableNode.getFilterSpecs().get(0).args().get(0)).isInstanceOf(NumberLiteralNode.class);
        NumberLiteralNode numberNode1 = (NumberLiteralNode) variableNode.getFilterSpecs().get(0).args().get(0);
        Assertions.assertThat(numberNode1.getNumberValue()).isEqualTo(0);
        
        // Second argument should be NumberLiteralNode (10)
        Assertions.assertThat(variableNode.getFilterSpecs().get(0).args().get(1)).isInstanceOf(NumberLiteralNode.class);
        NumberLiteralNode numberNode2 = (NumberLiteralNode) variableNode.getFilterSpecs().get(0).args().get(1);
        Assertions.assertThat(numberNode2.getNumberValue()).isEqualTo(10);
        
        // Third argument should be BooleanLiteralNode (true)
        Assertions.assertThat(variableNode.getFilterSpecs().get(0).args().get(2)).isInstanceOf(BooleanLiteralNode.class);
        BooleanLiteralNode booleanNode = (BooleanLiteralNode) variableNode.getFilterSpecs().get(0).args().get(2);
        Assertions.assertThat(booleanNode.getBooleanValue()).isTrue();
    }
}