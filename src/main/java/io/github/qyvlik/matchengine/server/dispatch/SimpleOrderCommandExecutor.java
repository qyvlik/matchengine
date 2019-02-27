package io.github.qyvlik.matchengine.server.dispatch;

import io.github.qyvlik.matchengine.core.matcher.request.BackupRequest;
import io.github.qyvlik.matchengine.core.matcher.request.CancelOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.request.CreateSymbolRequest;
import io.github.qyvlik.matchengine.core.matcher.request.PutOrderRequest;
import io.github.qyvlik.matchengine.server.listener.OrderCommand;
import io.github.qyvlik.matchengine.server.listener.OrderCommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SimpleOrderCommandExecutor implements OrderCommandExecutor {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private MatchEngineRequestDispatcher matchEngineRequestDispatcher;

    public SimpleOrderCommandExecutor(MatchEngineRequestDispatcher matchEngineRequestDispatcher) {
        this.matchEngineRequestDispatcher = matchEngineRequestDispatcher;
    }

    @Override
    public void exec(OrderCommand command) {
        List<Object> requestList = OrderCommand.convert(command);
        if (requestList == null || requestList.size() == 0) {
            return;
        }
        for (Object req : requestList) {

            if (req instanceof PutOrderRequest) {
                matchEngineRequestDispatcher.putOrder((PutOrderRequest) req);
                continue;
            }

            if (req instanceof CancelOrderRequest) {
                matchEngineRequestDispatcher.cancelOrder((CancelOrderRequest) req);
                continue;
            }

//            if (req instanceof BackupRequest) {
//                matchEngineRequestDispatcher.backupOrderBook((BackupRequest) req);
//                continue;
//            }
//
//            if (req instanceof CreateSymbolRequest) {
//                matchEngineRequestDispatcher.createSymbol((CreateSymbolRequest) req);
//                continue;
//            }

            logger.error("request not dispatch:{}", req);
        }
    }
}
