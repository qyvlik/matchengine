package io.github.qyvlik.matchengine.core.matcher.request;

import java.io.Serializable;

public class CreateSymbolRequest implements Serializable {
    private String symbol;

    public CreateSymbolRequest() {
    }

    public CreateSymbolRequest(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
