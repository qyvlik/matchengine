package io.github.qyvlik.matchengine.core.order;

import io.github.qyvlik.matchengine.core.order.vo.OrderSide;

import java.io.Serializable;

public class OrderBookCenter implements Serializable {
    private final OrderBookList asks = new OrderBookList(OrderSide.sell);
    private final OrderBookList bids = new OrderBookList(OrderSide.buy);

    public OrderBookList getAsks() {
        return asks;
    }

    public OrderBookList getBids() {
        return bids;
    }

    private void clearOrderBook() {
        asks.clear();
        asks.clear();
    }
}
