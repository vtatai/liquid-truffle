package io.github.liquidTruffle.ast

import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.nodes.Node

abstract class AstNode : Node() {
	abstract fun executeGeneric(frame: VirtualFrame): Any?
}
