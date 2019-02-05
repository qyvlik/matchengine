package io.github.qyvlik.matchengine.core.matcher.service;

import io.github.qyvlik.matchengine.core.matcher.request.CancelOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.request.PutOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.vo.ExecuteResult;
import io.github.qyvlik.matchengine.core.order.OrderBookCenter;
import io.github.qyvlik.matchengine.core.order.vo.Order;
import io.github.qyvlik.matchengine.core.order.vo.OrderState;
import io.github.qyvlik.matchengine.core.order.vo.OrderType;
import io.github.qyvlik.matchengine.utils.OrderBuildTool;
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

        PutOrderRequest buy1 = OrderBuildTool.buildRequest(globalSeqId,
                OrderType.limitBuy,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));

        ExecuteResult result1 = matchEngine.executeLimitOrder(buy1);

        logger.info("executeLimitOrder:{}, {}", buy1, result1);

        globalSeqId++;
        PutOrderRequest sell1 = OrderBuildTool.buildRequest(globalSeqId,
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

        PutOrderRequest buy1 = OrderBuildTool.buildRequest(globalSeqId,
                OrderType.limitBuy,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));

        ExecuteResult result1 = matchEngine.executeLimitOrder(buy1);

        logger.info("executeLimitOrder:{}, {}", buy1, result1);

        globalSeqId++;
        PutOrderRequest sell2 = OrderBuildTool.buildRequest(globalSeqId,
                OrderType.limitSell,
                symbol,
                new BigDecimal("1000"), new BigDecimal("2"));
        ExecuteResult result2 = matchEngine.executeLimitOrder(sell2);

        logger.info("executeLimitOrder:{}, {}", sell2, result2);

        OrderBookCenter orderBookCenter = matchEngine.getOrderBookCenter();

        assertTrue("asks must not be empty", !orderBookCenter.getAsks().isEmpty());

        globalSeqId++;
        PutOrderRequest buy3 = OrderBuildTool.buildRequest(globalSeqId,
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

        PutOrderRequest sell1 = OrderBuildTool.buildRequest(globalSeqId,
                OrderType.limitSell,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));

        ExecuteResult result1 = matchEngine.executeLimitOrder(sell1);

        logger.info("executeLimitOrder:{}, {}", sell1, result1);

        globalSeqId++;
        PutOrderRequest buy2 = OrderBuildTool.buildRequest(globalSeqId,
                OrderType.limitBuy,
                symbol,
                new BigDecimal("1000"), new BigDecimal("2"));
        ExecuteResult result2 = matchEngine.executeLimitOrder(buy2);

        logger.info("executeLimitOrder:{}, {}", buy2, result2);

        OrderBookCenter orderBookCenter = matchEngine.getOrderBookCenter();

        assertTrue("bids must not be empty", !orderBookCenter.getBids().isEmpty());

        globalSeqId++;
        PutOrderRequest sell3 = OrderBuildTool.buildRequest(globalSeqId,
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

        PutOrderRequest sell1 = OrderBuildTool.buildRequest(globalSeqId,
                OrderType.limitSell,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));

        matchEngine.executeLimitOrder(sell1);

        globalSeqId++;
        CancelOrderRequest req2 = OrderBuildTool.buildRequest(globalSeqId, symbol, sell1.getOrder().getOrderId());

        matchEngine.executeCancelOrder(req2);

        OrderBookCenter orderBookCenter = matchEngine.getOrderBookCenter();

        assertTrue("order book must be empty",
                orderBookCenter.getAsks().isEmpty() && orderBookCenter.getBids().isEmpty());

        globalSeqId++;
        PutOrderRequest buy3 = OrderBuildTool.buildRequest(globalSeqId,
                OrderType.limitBuy,
                symbol,
                new BigDecimal("1000"), new BigDecimal("2"));
        ExecuteResult result2 = matchEngine.executeLimitOrder(buy3);

        globalSeqId++;
        PutOrderRequest sell4 = OrderBuildTool.buildRequest(globalSeqId,
                OrderType.limitSell,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));

        ExecuteResult result3 = matchEngine.executeLimitOrder(sell4);

        globalSeqId++;
        CancelOrderRequest req5 = OrderBuildTool.buildRequest(globalSeqId, symbol, buy3.getOrder().getOrderId());
        ExecuteResult result4 = matchEngine.executeCancelOrder(req5);

        assertTrue("order book must be empty",
                orderBookCenter.getAsks().isEmpty() && orderBookCenter.getBids().isEmpty());


    }

}