package io.github.qyvlik.matchengine.core.order.vo;

import java.io.Serializable;
import java.math.BigDecimal;

public class Order implements Serializable {
    private Long seqId;                     // global sequence id for put order
    private String orderId;                 // order id
    private String symbol;                  // quote + base
    private String quote;
    private String base;
    private OrderType type;                 // order type such as limit-buy, limit-sell
    private String userId;
    private BigDecimal price;
    private BigDecimal stock;               // total stock
    private BigDecimal money;               // total money
    private BigDecimal dealStock;           // deal stock
    private BigDecimal dealMoney;           // deal money
    private OrderState state;
    private Long createTime;
    private Long updateTime;

    public Order() {

    }

    public Order(Order order) {
        this.setSeqId(order.getSeqId());
        this.setOrderId(order.getOrderId());
        this.setSymbol(order.getSymbol());
        this.setQuote(order.getQuote());
        this.setBase(order.getBase());
        this.setType(order.getType());
        this.setUserId(order.getUserId());
        this.setPrice(order.getPrice());
        this.setStock(order.getStock());
        this.setMoney(order.getMoney());
        this.setDealStock(order.getDealStock());
        this.setDealMoney(order.getDealMoney());
        this.setState(order.getState());
        this.setCreateTime(order.getCreateTime());
        this.setUpdateTime(order.getUpdateTime());
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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

    public BigDecimal getDealStock() {
        return dealStock;
    }

    public void setDealStock(BigDecimal dealStock) {
        this.dealStock = dealStock;
    }

    public BigDecimal getDealMoney() {
        return dealMoney;
    }

    public void setDealMoney(BigDecimal dealMoney) {
        this.dealMoney = dealMoney;
    }

    public OrderState getState() {
        return state;
    }

    public void setState(OrderState state) {
        this.state = state;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Order{" +
                "seqId=" + seqId +
                ", orderId='" + orderId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", quote='" + quote + '\'' +
                ", base='" + base + '\'' +
                ", type=" + type +
                ", userId='" + userId + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", money=" + money +
                ", dealStock=" + dealStock +
                ", dealMoney=" + dealMoney +
                ", state=" + state +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
