package io.github.liquidTruffle.parser.ast.nodes;

import io.github.liquidTruffle.parser.ast.AstNode;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(description = "Represents numeric literal values in Liquid templates")
public class NumberLiteralNode extends AstNode {
    private final Number value;
    
    public NumberLiteralNode(Number value) {
        this.value = value;
    }
    
    @Override
    public Number executeGeneric(VirtualFrame frame) {
        return value;
    }
    
    // Expose value for testing purposes
    public Number getNumberValue() {
        return this.value;
    }
}