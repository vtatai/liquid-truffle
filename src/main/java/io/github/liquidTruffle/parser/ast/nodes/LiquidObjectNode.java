package io.github.liquidTruffle.parser.ast.nodes;

import io.github.liquidTruffle.parser.ast.AstNode;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(description = "Represents Liquid objects that hold a child object, which can be either a literal or a variable")
public class LiquidObjectNode extends AstNode {
    private final AstNode child;
    
    public LiquidObjectNode(AstNode child) {
        this.child = child;
    }
    
    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return child.executeGeneric(frame);
    }
    
    // Expose child for testing purposes
    public AstNode getChildNode() {
        return this.child;
    }
}