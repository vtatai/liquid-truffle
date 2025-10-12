package io.github.liquidTruffle.parser.ast.nodes;

import io.github.liquidTruffle.parser.ast.AstNode;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(description = "Represents boolean literal values (true/false) in Liquid templates")
public class BooleanLiteralNode extends AstNode {
    private final boolean value;
    
    public BooleanLiteralNode(boolean value) {
        this.value = value;
    }
    
    @Override
    public Boolean executeGeneric(VirtualFrame frame) {
        return value;
    }
    
    // Expose value for testing purposes
    public boolean getBooleanValue() {
        return this.value;
    }
}