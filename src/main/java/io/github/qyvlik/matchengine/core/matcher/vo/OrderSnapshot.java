package io.github.qyvlik.matchengine.core.matcher.vo;

import io.github.qyvlik.matchengine.core.order.vo.OrderState;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderSnapshot implements Serializable {
    private String orderId;
    private BigDecimal dealStock;
    private BigDecimal dealMoney;
    private OrderState state;

    public OrderSnapshot() {

    }

    public OrderSnapshot(String orderId) {
        this.orderId = orderId;
    }

    public OrderSnapshot(String orderId, BigDecimal dealStock, BigDecimal dealMoney, OrderState state) {
        this.orderId = orderId;
        this.dealStock = dealStock;
        this.dealMoney = dealMoney;
        this.state = state;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    @Override
    public String toString() {
        return "OrderSnapshot{" +
                "orderId='" + orderId + '\'' +
                ", dealStock=" + dealStock +
                ", dealMoney=" + dealMoney +
                ", state=" + state +
                '}';
    }
}
