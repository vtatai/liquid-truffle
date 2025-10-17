package io.github.liquidTruffle;

import com.oracle.truffle.api.exception.AbstractTruffleException;
import io.github.liquidTruffle.parser.ast.AstNode;

public class LiquidRuntimeException extends AbstractTruffleException {
    public LiquidRuntimeException(String message) {
        super(message);
    }

    public LiquidRuntimeException(String message, AstNode node) {
        super(message, node);
    }
}
