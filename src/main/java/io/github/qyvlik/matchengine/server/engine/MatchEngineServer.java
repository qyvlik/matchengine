package io.github.qyvlik.matchengine.server.engine;

import com.google.common.collect.Maps;
import io.github.qyvlik.matchengine.core.durable.entity.OrderBookBackupItem;
import io.github.qyvlik.matchengine.core.matcher.request.*;
import io.github.qyvlik.matchengine.core.matcher.service.MatchEngine;
import io.github.qyvlik.matchengine.core.matcher.vo.ExecuteResult;
import io.github.qyvlik.matchengine.core.order.OrderBookCenter;
import io.github.qyvlik.matchengine.core.order.vo.Order;
import io.github.qyvlik.matchengine.server.durable.MatchEngineStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class MatchEngineServer {

    private final Map<String, MatchEngine> engineMap = Maps.newConcurrentMap();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private long backupExecuteTimes;
    private MatchEngineStoreService matchEngineStoreService;

    public MatchEngineServer(MatchEngineStoreService matchEngineStoreService, long backupExecuteTimes) {
        this.matchEngineStoreService = matchEngineStoreService;
        this.backupExecuteTimes = backupExecuteTimes;
    }

    public List<String> getSymbolList() {
        return matchEngineStoreService.getSymbolList();
    }

    public Order getOrderByOrderId(GetOrderRequest request) {
        return matchEngineStoreService.getOrderByOrderId(request.getSymbol(), request.getOrderId());
    }

    public Order getOrderBySeqId(GetOrderRequest request) {
        return matchEngineStoreService.getOrderBySeqId(request.getSymbol(), request.getSeqId());
    }

    // atomic
    public void createSymbol(CreateSymbolRequest request) {
        matchEngineStoreService.createSymbol(request.getSymbol());
    }

    // atomic
    public ExecuteResult putOrder(PutOrderRequest request) {
        MatchEngine matchEngine = engineMap.computeIfAbsent(
                request.getSymbol(), symbol -> new MatchEngine(symbol));

        ExecuteResult result =
                matchEngine.executeLimitOrder(request);

        if (result.getItems() == null || result.getItems().size() == 0) {
            // repeat order
            return result;
        }

        matchEngineStoreService.storeOrderForPutOrder(
                request.getSymbol(), request.getOrder(), result);

        if (matchEngine.getCurrentExecuteTimes() % backupExecuteTimes == 0) {
            backupOrderBook(new BackupRequest(request.getSymbol()));
        }

        return result;
    }

    // atomic
    public ExecuteResult cancelOrder(CancelOrderRequest request) {
        MatchEngine matchEngine = engineMap.computeIfAbsent(
                request.getSymbol(), symbol -> new MatchEngine(symbol));

        ExecuteResult result =
                matchEngine.executeCancelOrder(request);

        if (result.getItems() == null || result.getItems().size() == 0) {
            // already cancel order
            return result;
        }

        matchEngineStoreService.storeOrderForCancelOrder(
                request.getSymbol(), request.getSeqId(), request.getOrderId(), result);

        if (matchEngine.getCurrentExecuteTimes() % backupExecuteTimes == 0) {
            backupOrderBook(new BackupRequest(request.getSymbol()));
        }

        return result;
    }

    public Long getLastSeqId(String symbol) {
        return matchEngineStoreService.getOrderActionLastSeqId(symbol);
    }

    // atomic
    public Long backupOrderBook(BackupRequest request) {
        MatchEngine matchEngine = engineMap.computeIfAbsent(
                request.getSymbol(), symbol -> new MatchEngine(symbol));

        OrderBookCenter orderBookCenter = matchEngine.getOrderBookCenter();

        return matchEngineStoreService.backupOrderBookCenter(request.getSymbol(), orderBookCenter);
    }

    public void restoreFromDB(String symbol) {
        logger.info("restoreFromDB start:{}", symbol);
        Long backupId = matchEngineStoreService.getOrderBookLastBackupId(symbol);
        List<OrderBookBackupItem> items =
                matchEngineStoreService.getOrderBookBackupItems(symbol, backupId);

        MatchEngine matchEngine = engineMap.computeIfAbsent(
                symbol, k -> new MatchEngine(k));

        long restoreEndSeqId = matchEngine.getOrderBookCenter().restoreFromBackupItem(items);

        long latestSeqId = matchEngineStoreService.getOrderActionLastSeqId(symbol);

        if (restoreEndSeqId >= latestSeqId) {
            logger.info("restoreFromDB end:{}", symbol);
            return;
        }

        long iter = restoreEndSeqId + 1;

        logger.info("restoreFromDB symbol:{}, from:{}", symbol, iter);

        do {
            long currentSeqId = iter;
            iter += 1;

            String orderAction = matchEngineStoreService.getOrderAction(symbol, currentSeqId);
            Order order = matchEngineStoreService.getOrderBySeqId(symbol, currentSeqId);

            if (orderAction.startsWith("submit-")) {
                matchEngine.executeLimitOrder(new PutOrderRequest(symbol, currentSeqId, order));
            } else if (orderAction.startsWith("cancel-")) {
                matchEngine.executeCancelOrder(new CancelOrderRequest(symbol, order.getOrderId(), currentSeqId));
            }

        } while (iter <= latestSeqId);

        logger.info("restoreFromDB end:{} from:{}, to:{}", symbol, restoreEndSeqId, iter);
    }

}
