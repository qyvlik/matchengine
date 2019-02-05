package io.github.qyvlik.matchengine.core.matcher.request;

import java.io.Serializable;

public class CancelOrderRequest implements Serializable {
    private String symbol;          // base-quote
    private Long seqId;             // seq id for cancel
    private String orderId;

    public CancelOrderRequest() {

    }

    public CancelOrderRequest(String symbol, String orderId, Long seqId) {
        this.symbol = symbol;
        this.orderId = orderId;
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

    @Override
    public String toString() {
        return "CancelOrderRequest{" +
                "symbol='" + symbol + '\'' +
                ", orderId='" + orderId + '\'' +
                ", seqId=" + seqId +
                '}';
    }
}
