package io.github.qyvlik.matchengine.core.order.vo;

import io.github.qyvlik.matchengine.core.matcher.vo.OrderSnapshot;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderBook implements Serializable {
    private Long seqId;                     // global sequence id
    private String orderId;                 // order id
    private OrderType type;                 // order type such as limit-buy, limit-sell
    private BigDecimal price;
    private BigDecimal orderStock;          // total stock
    private BigDecimal orderMoney;          // total money
    private BigDecimal stock;               // un executed stock
    private BigDecimal money;               // un executed money

    public OrderBook() {

    }

    public OrderBook(Long seqId,
                     String orderId,
                     OrderType type,
                     BigDecimal price,
                     BigDecimal orderStock,
                     BigDecimal orderMoney,
                     BigDecimal stock,
                     BigDecimal money) {
        this.seqId = seqId;
        this.orderId = orderId;
        this.type = type;
        this.price = price;
        this.orderStock = orderStock;
        this.orderMoney = orderMoney;
        this.stock = stock;
        this.money = money;
    }

    public static void modifyOrderBookStock(OrderBook orderBook, BigDecimal stock) {
        orderBook.setStock(stock);
    }

    public static OrderBook adaptorLimitPriceOrder(Order order) {
        return new OrderBook(
                order.getSeqId(),
                order.getOrderId(),
                order.getType(),
                order.getPrice(),
                order.getStock(),
                order.getMoney(),
                order.getStock(),
                order.getMoney()
        );
    }

    public static OrderSnapshot snapshotOrderBook(OrderBook orderBook, boolean putOrder) {

        BigDecimal dealStock = orderBook.getOrderStock().subtract(orderBook.getStock());
        BigDecimal dealMoney = orderBook.getOrderMoney().subtract(orderBook.getMoney());

        OrderState state = null;

        if (putOrder) {
            if (dealStock.compareTo(BigDecimal.ZERO) == 0) {
                state = OrderState.submitted;
            } else if (dealStock.compareTo(orderBook.getOrderStock()) == 0) {
                state = OrderState.filled;
            } else {
                state = OrderState.partialFill;
            }
        } else {
            if (dealStock.compareTo(BigDecimal.ZERO) == 0) {
                state = OrderState.cancel;
            } else {
                state = OrderState.partialCancel;
            }
        }

        return new OrderSnapshot(orderBook.getOrderId(), dealStock, dealMoney, state);
    }

    public Long getSeqId() {
        return seqId;
    }

    public void setSeqId(Long seqId) {
        this.seqId = seqId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOrderStock() {
        return orderStock;
    }

    public void setOrderStock(BigDecimal orderStock) {
        this.orderStock = orderStock;
    }

    public BigDecimal getOrderMoney() {
        return orderMoney;
    }

    public void setOrderMoney(BigDecimal orderMoney) {
        this.orderMoney = orderMoney;
    }

    public BigDecimal getStock() {
        return stock;
    }

    public void setStock(BigDecimal stock) {
        this.stock = stock;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }
}
