package io.github.qyvlik.matchengine.server.method;

import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcService;
import io.github.qyvlik.matchengine.core.matcher.request.GetOrderRequest;
import io.github.qyvlik.matchengine.core.order.vo.Order;
import io.github.qyvlik.matchengine.server.dispatch.MatchEngineRequestDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@RpcService
@Service
public class MatchEngineMethodsService {

    @Autowired
    @Qualifier("matchEngineRequestDispatcher")
    private MatchEngineRequestDispatcher matchEngineRequestDispatcher;


    @RpcMethod(group = "matchengine", value = "pub.ping")
    public Long pubPing() {
        return System.currentTimeMillis();
    }

    @RpcMethod(group = "matchengine", value = "symbol.list")
    public List<String> getSymbolList() {
        return matchEngineRequestDispatcher.getSymbolList();
    }

    @RpcMethod(group = "matchengine", value = "order.by.id")
    public Order getOrderByOrderId(String symbol, String orderId) {
        return matchEngineRequestDispatcher.getOrderByOrderId(new GetOrderRequest(symbol, orderId));
    }

    @RpcMethod(group = "matchengine", value = "order.by.seq")
    public Order getOrderBySeqId(String symbol, Long seqId) {
        return matchEngineRequestDispatcher.getOrderBySeqId(new GetOrderRequest(symbol, seqId));
    }

    @RpcMethod(group = "matchengine", value = "order.action.latest")
    public Long getOrderActionLastSeqId(String symbol) {
        return matchEngineRequestDispatcher.getOrderActionLastSeqId(symbol);
    }
}
