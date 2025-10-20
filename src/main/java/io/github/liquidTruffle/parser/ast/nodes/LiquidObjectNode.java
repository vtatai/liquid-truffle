package io.github.liquidTruffle.parser.ast.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import io.github.liquidTruffle.parser.ast.AstNode;

@NodeInfo(description = "Represents Liquid objects that hold a single child representing the entire filter chain")
public class LiquidObjectNode extends AstNode {
    @Child
    private AstNode child;  // Either a literal/variable or the root of a filter chain

    public LiquidObjectNode(AstNode child) {
        this.child = child;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return child.executeGeneric(frame);
    }

    // Expose child for testing purposes
    public AstNode getChild() {
        return this.child;
    }
}