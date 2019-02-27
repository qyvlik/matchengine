package io.github.qyvlik.matchengine.server.dispatch;

import io.github.qyvlik.matchengine.core.matcher.request.*;
import io.github.qyvlik.matchengine.core.order.vo.Order;
import io.github.qyvlik.matchengine.server.engine.MatchEngineServer;

import java.util.List;
import java.util.concurrent.Executor;

public class MatchEngineRequestDispatcher {

    private MatchEngineServer matchEngineServer;
    private WritableExecutor writableExecutor;

    public MatchEngineRequestDispatcher(MatchEngineServer matchEngineServer, WritableExecutor writableExecutor) {
        this.matchEngineServer = matchEngineServer;
        this.writableExecutor = writableExecutor;
    }

    public List<String> getSymbolList() {
        return matchEngineServer.getSymbolList();
    }

    public Order getOrderByOrderId(GetOrderRequest request) {
        return matchEngineServer.getOrderByOrderId(request);
    }

    public Order getOrderBySeqId(GetOrderRequest request) {
        return matchEngineServer.getOrderBySeqId(request);
    }

    public void createSymbol(CreateSymbolRequest request) {
        Executor executor = writableExecutor.getBySymbol(request.getSymbol());
        if (executor != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    matchEngineServer.createSymbol(request);
                }
            });
        }
    }

    public void putOrder(PutOrderRequest request) {
        Executor executor = writableExecutor.getBySymbol(request.getSymbol());
        if (executor != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    matchEngineServer.putOrder(request);
                }
            });
        }
    }

    public void cancelOrder(CancelOrderRequest request) {
        Executor executor = writableExecutor.getBySymbol(request.getSymbol());
        if (executor != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    matchEngineServer.cancelOrder(request);
                }
            });
        }
    }

    public void backupOrderBook(BackupRequest request) {
        Executor executor = writableExecutor.getBySymbol(request.getSymbol());
        if (executor != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    matchEngineServer.backupOrderBook(request);
                }
            });
        }
    }
}
