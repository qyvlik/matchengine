package io.github.qyvlik.matchengine.server.engine;

import com.google.common.collect.Maps;
import io.github.qyvlik.matchengine.core.matcher.request.CancelOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.request.CreateSymbolRequest;
import io.github.qyvlik.matchengine.core.matcher.request.GetOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.request.PutOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.service.MatchEngine;
import io.github.qyvlik.matchengine.core.matcher.vo.ExecuteResult;
import io.github.qyvlik.matchengine.core.order.vo.Order;
import io.github.qyvlik.matchengine.server.durable.MatchEngineStoreService;

import java.util.List;
import java.util.Map;

public class MatchEngineServer {

    private final Map<String, MatchEngine> engineMap = Maps.newConcurrentMap();

    private MatchEngineStoreService matchEngineStoreService;

    public MatchEngineServer(MatchEngineStoreService matchEngineStoreService) {
        this.matchEngineStoreService = matchEngineStoreService;
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
                request.getSymbol(), request.getOrderId(), result);

        return result;
    }

    // atomic
//    public Long backupOrderBook(BackupRequest request) {
//        MatchEngine matchEngine = engineMap.computeIfAbsent(
//                request.getSymbol(), symbol -> new MatchEngine(symbol));
//
//        OrderBookCenter orderBookCenter = matchEngine.getOrderBookCenter();
//
//        return matchEngineStoreService.backupOrderBookCenter(request.getSymbol(), orderBookCenter);
//    }

}
