package io.github.qyvlik.matchengine.server.engine;

import io.github.qyvlik.matchengine.core.durable.service.MatchEngineDBFactory;
import io.github.qyvlik.matchengine.core.matcher.request.CancelOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.request.CreateSymbolRequest;
import io.github.qyvlik.matchengine.core.matcher.request.GetOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.request.PutOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.vo.ExecuteResult;
import io.github.qyvlik.matchengine.core.order.vo.Order;
import io.github.qyvlik.matchengine.core.order.vo.OrderType;
import io.github.qyvlik.matchengine.server.durable.MatchEngineStoreService;
import io.github.qyvlik.matchengine.utils.OrderBuildTool;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MatchEngineServerTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private MatchEngineServer createServer() throws Exception {
        String path = "/tmp/matchengine";
        int limit = 1;

        MatchEngineDBFactory dbFactory = new MatchEngineDBFactory(path, limit);

        dbFactory.loadDBs();

        MatchEngineStoreService storeService = new MatchEngineStoreService(dbFactory);

        return new MatchEngineServer(storeService, 10);
    }

    @Test
    public void getSymbolList() throws Exception {
        MatchEngineServer server = createServer();

        String symbol = "btc-usdt";

        server.createSymbol(new CreateSymbolRequest(symbol));

        List<String> symbolList = server.getSymbolList();

        assertTrue("symbolList is not empty", symbolList != null && symbolList.size() != 0);
    }

    @Test
    public void putAndCancelOrder() throws Exception {
        MatchEngineServer server = createServer();

        String symbol = "btc-usdt";

        server.createSymbol(new CreateSymbolRequest(symbol));

        Long globalSeqId = 1L;

        PutOrderRequest sell1 = OrderBuildTool.buildRequest(globalSeqId,
                OrderType.limitSell,
                symbol,
                new BigDecimal("1000"), new BigDecimal("1"));
        ExecuteResult result1 = server.putOrder(sell1);
        Order order1 = server.getOrderByOrderId(
                new GetOrderRequest(symbol, sell1.getOrder().getOrderId()));
        assertNotNull("order1 can not be null", order1);
        logger.info("putAndCancelOrder put order1:{}", order1);

        globalSeqId++;
        PutOrderRequest buy2 = OrderBuildTool.buildRequest(globalSeqId,
                OrderType.limitBuy,
                symbol,
                new BigDecimal("1000"), new BigDecimal("2"));
        ExecuteResult result2 = server.putOrder(buy2);
        Order order2 = server.getOrderByOrderId(
                new GetOrderRequest(symbol, buy2.getOrder().getOrderId()));
        assertNotNull("order2 can not be null", order2);
        logger.info("putAndCancelOrder put order2:{}", order2);

        globalSeqId++;
        CancelOrderRequest cancel3 = OrderBuildTool.buildRequest(globalSeqId, symbol, order2.getOrderId());
        ExecuteResult result3 = server.cancelOrder(cancel3);
        order2 = server.getOrderByOrderId(
                new GetOrderRequest(symbol, buy2.getOrder().getOrderId()));
        assertNotNull("order2 can not be null", order2);

        logger.info("putAndCancelOrder cancel order2:{}", order2);
    }
}