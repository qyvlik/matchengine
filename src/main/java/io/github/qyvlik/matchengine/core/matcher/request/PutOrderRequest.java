package io.github.qyvlik.matchengine.core.matcher.request;

import io.github.qyvlik.matchengine.core.order.vo.Order;

import java.io.Serializable;

public class PutOrderRequest implements Serializable {
    private String symbol;          // base-quote
    private Long seqId;
    private Order order;

    public PutOrderRequest() {

    }

    public PutOrderRequest(String symbol, Long seqId, Order order) {
        this.symbol = symbol;
        this.seqId = seqId;
        this.order = order;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Long getSeqId() {
        return seqId;
    }

    public void setSeqId(Long seqId) {
        this.seqId = seqId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "PutOrderRequest{" +
                "symbol='" + symbol + '\'' +
                ", seqId=" + seqId +
                ", order=" + order +
                '}';
    }
}
