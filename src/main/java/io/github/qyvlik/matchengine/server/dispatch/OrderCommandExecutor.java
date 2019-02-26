package io.github.qyvlik.matchengine.server.dispatch;

import io.github.qyvlik.matchengine.core.matcher.request.CancelOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.request.PutOrderRequest;
import io.github.qyvlik.matchengine.server.listener.IOrderCommandExecutor;
import io.github.qyvlik.matchengine.server.listener.OrderCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OrderCommandExecutor implements IOrderCommandExecutor {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private MatchEngineRequestDispatcher matchEngineRequestDispatcher;

    public OrderCommandExecutor(MatchEngineRequestDispatcher matchEngineRequestDispatcher) {
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

            logger.error("request not dispatch:{}", req);
        }
    }
}
