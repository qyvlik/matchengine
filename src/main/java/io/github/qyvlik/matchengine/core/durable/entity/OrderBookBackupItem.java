package io.github.qyvlik.matchengine.core.durable.entity;

import io.github.qyvlik.matchengine.core.order.vo.OrderBook;

import java.io.Serializable;

public class OrderBookBackupItem implements Serializable {
    private Long prevSeqId;
    private OrderBook orderBook;

    public OrderBookBackupItem() {

    }

    public OrderBookBackupItem(Long prevSeqId, OrderBook orderBook) {
        this.prevSeqId = prevSeqId;
        this.orderBook = orderBook;
    }

    public Long getPrevSeqId() {
        return prevSeqId;
    }

    public void setPrevSeqId(Long prevSeqId) {
        this.prevSeqId = prevSeqId;
    }

    public OrderBook getOrderBook() {
        return orderBook;
    }

    public void setOrderBook(OrderBook orderBook) {
        this.orderBook = orderBook;
    }
}
