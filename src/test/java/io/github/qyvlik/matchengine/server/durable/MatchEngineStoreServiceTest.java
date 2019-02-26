package io.github.qyvlik.matchengine.server.durable;

import io.github.qyvlik.matchengine.core.durable.service.MatchEngineDBFactory;
import io.github.qyvlik.matchengine.core.matcher.request.CancelOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.request.PutOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.service.MatchEngine;
import io.github.qyvlik.matchengine.core.matcher.vo.ExecuteResult;
import io.github.qyvlik.matchengine.core.matcher.vo.MatchDetailItem;
import io.github.qyvlik.matchengine.core.order.OrderBookCenter;
import io.github.qyvlik.matchengine.core.order.vo.Order;
import io.github.qyvlik.matchengine.core.order.vo.OrderType;
import io.github.qyvlik.matchengine.utils.OrderBuildTool;
import org.iq80.leveldb.DB;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

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

        PutOrderRequest buy1 = OrderBuildTool.buildRequest(globalSeqId,
                OrderType.limitBuy,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));

        ExecuteResult result1 = matchEngine.executeLimitOrder(buy1);

        matchEngineStoreService.storeOrderForPutOrder(symbol, buy1.getOrder(), result1);

//        logger.info("executeLimitOrder:{}, {}", buy1, result1);

        globalSeqId++;
        PutOrderRequest sell1 = OrderBuildTool.buildRequest(globalSeqId,
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

        Long matchLastId = matchEngineStoreService.getLastMatchId(symbol);

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

        PutOrderRequest sell1 = OrderBuildTool.buildRequest(globalSeqId,
                OrderType.limitSell,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));

        ExecuteResult result1 = matchEngine.executeLimitOrder(sell1);

        matchEngineStoreService.storeOrderForPutOrder(symbol, sell1.getOrder(), result1);

        globalSeqId++;
        CancelOrderRequest req2 = OrderBuildTool.buildRequest(globalSeqId, symbol, sell1.getOrder().getOrderId());

        ExecuteResult result2 = matchEngine.executeCancelOrder(req2);

        OrderBookCenter orderBookCenter = matchEngine.getOrderBookCenter();

        assertTrue("order book must be empty",
                orderBookCenter.getAsks().isEmpty() && orderBookCenter.getBids().isEmpty());

        matchEngineStoreService.storeOrderForCancelOrder(symbol, req2.getSeqId(), sell1.getOrder().getOrderId(), result2);


        Order order1 = matchEngineStoreService.getOrderByOrderId(symbol, sell1.getOrder().getOrderId());
        logger.info("storeOrderForPutOrder order1:{}", order1);
        boolean deleteOrderResult =
                matchEngineStoreService.deleteOrder(symbol, sell1.getOrder().getOrderId());
        assertTrue("delete order must success", deleteOrderResult);
        order1 = matchEngineStoreService.getOrderByOrderId(symbol, sell1.getOrder().getOrderId());
        assertNull("order is delete, can not get from disk", order1);


        Long matchLastId = matchEngineStoreService.getLastMatchId(symbol);
        logger.info("storeOrderForPutOrder matchLastId:{}", matchLastId);


        MatchDetailItem item1 = matchEngineStoreService.getMatchItemByMatchId(symbol, 1L);
        logger.info("storeOrderForPutOrder item1:{}", item1);


        MatchDetailItem item2 = matchEngineStoreService.getMatchItemByMatchId(symbol, 2L);
        logger.info("storeOrderForPutOrder item2:{}", item2);
    }

    @Test
    public void backupOrderBookCenter() throws Exception {
    }
}