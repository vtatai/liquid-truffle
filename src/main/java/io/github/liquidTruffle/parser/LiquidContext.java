package io.github.liquidTruffle.parser;

public final class LiquidContext {
    private final GlobalScopeObject globalScopeObject = new GlobalScopeObject();

    public GlobalScopeObject getGlobalScopeObject() {
        return globalScopeObject;
    }
}
