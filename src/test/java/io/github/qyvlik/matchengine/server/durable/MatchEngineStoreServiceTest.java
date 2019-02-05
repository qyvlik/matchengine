package io.github.qyvlik.matchengine.server.durable;

import io.github.qyvlik.matchengine.core.durable.service.MatchEngineDBFactory;
import io.github.qyvlik.matchengine.core.matcher.request.CancelOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.request.PutOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.service.MatchEngine;
import io.github.qyvlik.matchengine.core.matcher.vo.ExecuteResult;
import io.github.qyvlik.matchengine.core.matcher.vo.MatchDetailItem;
import io.github.qyvlik.matchengine.core.order.OrderBookCenter;
import io.github.qyvlik.matchengine.core.order.vo.Order;
import io.github.qyvlik.matchengine.core.order.vo.OrderState;
import io.github.qyvlik.matchengine.core.order.vo.OrderType;
import org.iq80.leveldb.DB;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MatchEngineStoreServiceTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private MatchEngineStoreService createService() throws Exception {
        String path = "/tmp/matchengine";
        int limit = 1;

        MatchEngineDBFactory dbFactory = new MatchEngineDBFactory(path, limit);

        dbFactory.loadDBs();

        return new MatchEngineStoreService(dbFactory);
    }

    @Test
    public void createSymbol() throws Exception {
        String symbol = "btc-usdt";
        MatchEngineStoreService matchEngineStoreService = createService();
        DB db = matchEngineStoreService.createSymbol(symbol);
        assertTrue("db cannot be empty", db != null);
    }

    @Test
    public void getSymbolList() throws Exception {
        String symbol = "btc-usdt";
        MatchEngineStoreService matchEngineStoreService = createService();
        DB db = matchEngineStoreService.createSymbol(symbol);
        assertTrue("db cannot be empty", db != null);

        List<String> symbolList = matchEngineStoreService.getSymbolList();

        logger.info("getSymbolList:{}", symbolList);
    }

    @Test
    public void storeOrderForPutOrder() throws Exception {
        String symbol = "btc-usdt";
        MatchEngineStoreService matchEngineStoreService = createService();
        DB db = matchEngineStoreService.createSymbol(symbol);
        assertTrue("db cannot be empty", db != null);

        MatchEngine matchEngine = new MatchEngine(symbol);

        Long globalSeqId = 1L;

        PutOrderRequest buy1 = buildRequest(globalSeqId,
                OrderType.limitBuy,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));

        ExecuteResult result1 = matchEngine.executeLimitOrder(buy1);

        matchEngineStoreService.storeOrderForPutOrder(symbol, buy1.getOrder(), result1);

//        logger.info("executeLimitOrder:{}, {}", buy1, result1);

        globalSeqId++;
        PutOrderRequest sell1 = buildRequest(globalSeqId,
                OrderType.limitSell,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));
        ExecuteResult result2 = matchEngine.executeLimitOrder(sell1);

//        logger.info("executeLimitOrder:{}, {}", sell1, result2);

        OrderBookCenter orderBookCenter = matchEngine.getOrderBookCenter();

        assertTrue("order book must be empty",
                orderBookCenter.getAsks().isEmpty() && orderBookCenter.getBids().isEmpty());

        matchEngineStoreService.storeOrderForPutOrder(symbol, sell1.getOrder(), result2);

        Order order1 = matchEngineStoreService.getOrderByOrderId(symbol, buy1.getOrder().getOrderId());

        logger.info("storeOrderForPutOrder order1:{}", order1);

        Order order2 = matchEngineStoreService.getOrderByOrderId(symbol, sell1.getOrder().getOrderId());

        logger.info("storeOrderForPutOrder order2:{}", order2);

        Long matchLastId = matchEngineStoreService.getMatchLastId(symbol);

        logger.info("storeOrderForPutOrder matchLastId:{}", matchLastId);

        MatchDetailItem item1 = matchEngineStoreService.getMatchItemByMatchId(symbol, 1L);

        logger.info("storeOrderForPutOrder item1:{}", item1);

        MatchDetailItem item2 = matchEngineStoreService.getMatchItemByMatchId(symbol, 2L);

        logger.info("storeOrderForPutOrder item2:{}", item2);

    }

    @Test
    public void storeOrderForCancelOrder() throws Exception {
        String symbol = "btc-usdt";

        MatchEngineStoreService matchEngineStoreService = createService();
        DB db = matchEngineStoreService.createSymbol(symbol);
        assertTrue("db cannot be empty", db != null);

        MatchEngine matchEngine = new MatchEngine(symbol);

        Long globalSeqId = 1L;

        PutOrderRequest sell1 = buildRequest(globalSeqId,
                OrderType.limitSell,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));

        ExecuteResult result1 = matchEngine.executeLimitOrder(sell1);

        matchEngineStoreService.storeOrderForPutOrder(symbol, sell1.getOrder(), result1);

        globalSeqId++;
        CancelOrderRequest req2 = buildRequest(globalSeqId, symbol, sell1.getOrder().getOrderId());

        ExecuteResult result2 = matchEngine.executeCancelOrder(req2);

        OrderBookCenter orderBookCenter = matchEngine.getOrderBookCenter();

        assertTrue("order book must be empty",
                orderBookCenter.getAsks().isEmpty() && orderBookCenter.getBids().isEmpty());

        matchEngineStoreService.storeOrderForCancelOrder(symbol, sell1.getOrder().getOrderId(), result2);


        Order order1 = matchEngineStoreService.getOrderByOrderId(symbol, sell1.getOrder().getOrderId());
        logger.info("storeOrderForPutOrder order1:{}", order1);
        boolean deleteOrderResult =
                matchEngineStoreService.deleteOrder(symbol, sell1.getOrder().getOrderId());
        assertTrue("delete order must success", deleteOrderResult);
        order1 = matchEngineStoreService.getOrderByOrderId(symbol, sell1.getOrder().getOrderId());
        assertNull("order is delete, can not get from disk", order1);


        Long matchLastId = matchEngineStoreService.getMatchLastId(symbol);
        logger.info("storeOrderForPutOrder matchLastId:{}", matchLastId);


        MatchDetailItem item1 = matchEngineStoreService.getMatchItemByMatchId(symbol, 1L);
        logger.info("storeOrderForPutOrder item1:{}", item1);


        MatchDetailItem item2 = matchEngineStoreService.getMatchItemByMatchId(symbol, 2L);
        logger.info("storeOrderForPutOrder item2:{}", item2);
    }

    @Test
    public void backupOrderBookCenter() throws Exception {
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