package io.github.liquidTruffle.parser.ast.nodes;

import io.github.liquidTruffle.parser.ast.AstNode;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(description = "Represents literal text content in Liquid templates")
public class TextNode extends AstNode {
    private final String text;
    
    public TextNode(String text) {
        this.text = text;
    }
    
    @Override
    public String executeGeneric(VirtualFrame frame) {
        return text;
    }
    
    // Expose text for testing purposes
    public String getTextContent() {
        return this.text;
    }
}