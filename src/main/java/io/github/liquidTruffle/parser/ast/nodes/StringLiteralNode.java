package io.github.liquidTruffle.parser.ast.nodes;

import io.github.liquidTruffle.parser.ast.AstNode;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(description = "Represents string literal values in Liquid templates")
public class StringLiteralNode extends AstNode {
    private final String value;
    
    public StringLiteralNode(String value) {
        this.value = value;
    }
    
    @Override
    public String executeGeneric(VirtualFrame frame) {
        return value;
    }

    // Expose value for testing purposes
    public String getStringValue() {
        return this.value;
    }
}