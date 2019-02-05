package io.github.qyvlik.matchengine.core.matcher.request;

import java.io.Serializable;

public class GetOrderRequest implements Serializable {
    private String symbol;
    private Long seqId;
    private String orderId;

    public GetOrderRequest(String symbol, String orderId) {
        this.symbol = symbol;
        this.orderId = orderId;
    }

    public GetOrderRequest(String symbol, Long seqId) {
        this.symbol = symbol;
        this.seqId = seqId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getSeqId() {
        return seqId;
    }

    public void setSeqId(Long seqId) {
        this.seqId = seqId;
    }
}
