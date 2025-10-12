package io.github.liquidTruffle.parser.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

public abstract class AstNode extends Node {
    public abstract Object executeGeneric(VirtualFrame frame);
}