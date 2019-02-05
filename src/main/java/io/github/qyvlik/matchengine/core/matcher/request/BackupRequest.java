package io.github.qyvlik.matchengine.core.matcher.request;

import java.io.Serializable;

public class BackupRequest implements Serializable {
    private String symbol;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
