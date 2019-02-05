package io.github.qyvlik.matchengine.utils;

import io.github.qyvlik.matchengine.core.matcher.request.CancelOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.request.PutOrderRequest;
import io.github.qyvlik.matchengine.core.order.vo.Order;
import io.github.qyvlik.matchengine.core.order.vo.OrderState;
import io.github.qyvlik.matchengine.core.order.vo.OrderType;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderBuildTool {

    public static CancelOrderRequest buildRequest(Long seqId, String symbol, String orderId) {
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(orderId);
        request.setSeqId(seqId);
        request.setSymbol(symbol);
        return request;
    }

    public static PutOrderRequest buildRequest(Long seqId,
                                               OrderType type,
                                               String symbol,
                                               BigDecimal price,
                                               BigDecimal amount) {
        PutOrderRequest request = new PutOrderRequest();
        request.setSymbol(symbol);
        request.setSeqId(seqId);
        request.setOrder(OrderBuildTool.build(seqId, type, symbol, price, amount));
        return request;
    }

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static Order build(Long seqId,
                              OrderType type,
                              String symbol,
                              BigDecimal price,
                              BigDecimal amount) {
        Order order = new Order();
        order.setSeqId(seqId);
        order.setOrderId(uuid());
        order.setSymbol(symbol);
        order.setQuote("");
        order.setBase("");
        order.setType(type);
        order.setUserId("nobody");
        order.setPrice(price);
        order.setStock(amount);
        order.setMoney(price.multiply(amount));
        order.setDealStock(BigDecimal.ZERO);
        order.setDealMoney(BigDecimal.ZERO);
        order.setState(OrderState.submitting);
        order.setCreateTime(System.currentTimeMillis());
        order.setUpdateTime(System.currentTimeMillis());

        return order;
    }
}
