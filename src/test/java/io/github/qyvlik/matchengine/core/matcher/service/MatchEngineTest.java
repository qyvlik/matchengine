package io.github.qyvlik.matchengine.core.matcher.service;

import io.github.qyvlik.matchengine.core.matcher.request.CancelOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.request.PutOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.vo.ExecuteResult;
import io.github.qyvlik.matchengine.core.order.OrderBookCenter;
import io.github.qyvlik.matchengine.core.order.vo.Order;
import io.github.qyvlik.matchengine.core.order.vo.OrderState;
import io.github.qyvlik.matchengine.core.order.vo.OrderType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.*;

public class MatchEngineTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void getSymbol() throws Exception {

        String symbol = "btc-usdt";

        MatchEngine matchEngine = new MatchEngine(symbol);

        String symbolFromEngine =
                matchEngine.getSymbol();

        assertEquals("symbol must equals", symbol, symbolFromEngine);
    }

    @Test
    public void getOrderBookCenter() throws Exception {
        String symbol = "btc-usdt";

        MatchEngine matchEngine = new MatchEngine(symbol);

        OrderBookCenter orderBookCenter = matchEngine.getOrderBookCenter();

        assertNotNull("orderBookCenter cannot be null", orderBookCenter);
    }

    @Test
    public void executeLimitOrder1() throws Exception {
        String symbol = "btc-usdt";

        MatchEngine matchEngine = new MatchEngine(symbol);

        Long globalSeqId = 1L;

        PutOrderRequest buy1 = buildRequest(globalSeqId,
                OrderType.limitBuy,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));

        ExecuteResult result1 = matchEngine.executeLimitOrder(buy1);

        logger.info("executeLimitOrder:{}, {}", buy1, result1);

        globalSeqId++;
        PutOrderRequest sell1 = buildRequest(globalSeqId,
                OrderType.limitSell,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));
        ExecuteResult result2 = matchEngine.executeLimitOrder(sell1);

        logger.info("executeLimitOrder:{}, {}", sell1, result2);

        OrderBookCenter orderBookCenter = matchEngine.getOrderBookCenter();

        assertTrue("order book must be empty",
                orderBookCenter.getAsks().isEmpty() && orderBookCenter.getBids().isEmpty());
    }

    @Test
    public void executeLimitOrder2() throws Exception {
        String symbol = "btc-usdt";

        MatchEngine matchEngine = new MatchEngine(symbol);

        Long globalSeqId = 1L;

        PutOrderRequest buy1 = buildRequest(globalSeqId,
                OrderType.limitBuy,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));

        ExecuteResult result1 = matchEngine.executeLimitOrder(buy1);

        logger.info("executeLimitOrder:{}, {}", buy1, result1);

        globalSeqId++;
        PutOrderRequest sell2 = buildRequest(globalSeqId,
                OrderType.limitSell,
                symbol,
                new BigDecimal("1000"), new BigDecimal("2"));
        ExecuteResult result2 = matchEngine.executeLimitOrder(sell2);

        logger.info("executeLimitOrder:{}, {}", sell2, result2);

        OrderBookCenter orderBookCenter = matchEngine.getOrderBookCenter();

        assertTrue("asks must not be empty", !orderBookCenter.getAsks().isEmpty());

        globalSeqId++;
        PutOrderRequest buy3 = buildRequest(globalSeqId,
                OrderType.limitBuy,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));

        ExecuteResult result3 = matchEngine.executeLimitOrder(buy3);

        logger.info("executeLimitOrder:{}, {}", buy3, result3);

        assertTrue("order book must be empty",
                orderBookCenter.getAsks().isEmpty() && orderBookCenter.getBids().isEmpty());
    }

    @Test
    public void executeLimitOrder3() throws Exception {
        String symbol = "btc-usdt";

        MatchEngine matchEngine = new MatchEngine(symbol);

        Long globalSeqId = 1L;

        PutOrderRequest sell1 = buildRequest(globalSeqId,
                OrderType.limitSell,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));

        ExecuteResult result1 = matchEngine.executeLimitOrder(sell1);

        logger.info("executeLimitOrder:{}, {}", sell1, result1);

        globalSeqId++;
        PutOrderRequest buy2 = buildRequest(globalSeqId,
                OrderType.limitBuy,
                symbol,
                new BigDecimal("1000"), new BigDecimal("2"));
        ExecuteResult result2 = matchEngine.executeLimitOrder(buy2);

        logger.info("executeLimitOrder:{}, {}", buy2, result2);

        OrderBookCenter orderBookCenter = matchEngine.getOrderBookCenter();

        assertTrue("bids must not be empty", !orderBookCenter.getBids().isEmpty());

        globalSeqId++;
        PutOrderRequest sell3 = buildRequest(globalSeqId,
                OrderType.limitSell,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));

        ExecuteResult result3 = matchEngine.executeLimitOrder(sell3);

        logger.info("executeLimitOrder:{}, {}", sell3, result3);

        assertTrue("order book must be empty",
                orderBookCenter.getAsks().isEmpty() && orderBookCenter.getBids().isEmpty());
    }

    @Test
    public void executeCancelOrder() throws Exception {
        String symbol = "btc-usdt";

        MatchEngine matchEngine = new MatchEngine(symbol);

        Long globalSeqId = 1L;

        PutOrderRequest sell1 = buildRequest(globalSeqId,
                OrderType.limitSell,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));

        matchEngine.executeLimitOrder(sell1);

        globalSeqId++;
        CancelOrderRequest req2 = buildRequest(globalSeqId, symbol, sell1.getOrder().getOrderId());

        matchEngine.executeCancelOrder(req2);

        OrderBookCenter orderBookCenter = matchEngine.getOrderBookCenter();

        assertTrue("order book must be empty",
                orderBookCenter.getAsks().isEmpty() && orderBookCenter.getBids().isEmpty());

        globalSeqId++;
        PutOrderRequest buy3 = buildRequest(globalSeqId,
                OrderType.limitBuy,
                symbol,
                new BigDecimal("1000"), new BigDecimal("2"));
        ExecuteResult result2 = matchEngine.executeLimitOrder(buy3);

        globalSeqId++;
        PutOrderRequest sell4 = buildRequest(globalSeqId,
                OrderType.limitSell,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));

        ExecuteResult result3 = matchEngine.executeLimitOrder(sell4);

        globalSeqId++;
        CancelOrderRequest req5 = buildRequest(globalSeqId, symbol, buy3.getOrder().getOrderId());
        ExecuteResult result4 = matchEngine.executeCancelOrder(req5);

        assertTrue("order book must be empty",
                orderBookCenter.getAsks().isEmpty() && orderBookCenter.getBids().isEmpty());


    }

    private String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private CancelOrderRequest buildRequest(Long seqId, String symbol, String orderId) {
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(orderId);
        request.setSeqId(seqId);
        request.setSymbol(symbol);
        return request;
    }

    private PutOrderRequest buildRequest(Long seqId,
                                         OrderType type,
                                         String symbol,
                                         BigDecimal price,
                                         BigDecimal amount) {
        PutOrderRequest request = new PutOrderRequest();
        request.setSymbol(symbol);
        request.setSeqId(seqId);
        request.setOrder(build(seqId, type, symbol, price, amount));
        return request;
    }

    private Order build(Long seqId,
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