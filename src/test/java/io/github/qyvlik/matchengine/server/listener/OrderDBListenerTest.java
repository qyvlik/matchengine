package io.github.qyvlik.matchengine.server.listener;

import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.client.RpcClient;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.matchengine.core.matcher.request.CancelOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.request.PutOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.service.MatchEngine;
import io.github.qyvlik.matchengine.core.matcher.vo.ExecuteResult;
import io.github.qyvlik.matchengine.core.order.vo.Order;
import io.github.qyvlik.matchengine.core.order.vo.OrderType;
import io.github.qyvlik.matchengine.utils.OrderBuildTool;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Future;

public class OrderDBListenerTest {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void startupAndSub() throws Exception {

        OrderDBListener listener = new OrderDBListener("ws://localhost:17711/orderdb",
                "btc-usdt", new IOrderCommandExecutor() {
            @Override
            public void exec(OrderCommand command) {
                logger.info("command:{}", command);
            }
        });

        listener.startupAndSub(0L);

        try {
            Thread.sleep(3000000);
        } catch (Exception e) {

        }
    }

    @Test
    public void startupAndSubAndMatch() throws Exception {

        String symbol = "btc-usdt";

        final MatchEngine matchEngine = new MatchEngine(symbol);

        OrderDBListener listener = new OrderDBListener("ws://localhost:17711/orderdb",
                symbol, new IOrderCommandExecutor() {
            @Override
            public void exec(OrderCommand command) {

                List<Object> requestList = OrderCommand.convert(command);

                if (requestList == null || requestList.size() == 0) {
                    return;
                }

                List<ExecuteResult> executeResultList = Lists.newLinkedList();

                for (Object req : requestList) {

                    if (req instanceof PutOrderRequest) {
                        ExecuteResult result = matchEngine.executeLimitOrder((PutOrderRequest) req);
                        executeResultList.add(result);
                        continue;
                    }

                    if (req instanceof CancelOrderRequest) {
                        ExecuteResult result = matchEngine.executeCancelOrder((CancelOrderRequest) req);
                        executeResultList.add(result);
                        continue;
                    }
                }

                logger.info("executeResultList:{}", executeResultList);
            }
        });

        listener.startupAndSub(0L);

        try {
            Thread.sleep(3000000);
        } catch (Exception e) {

        }
    }

    @Test
    public void submitOrder() throws Exception {
        RpcClient writeClient = new RpcClient("ws://localhost:17711/orderdb", 500, 20000);
        writeClient.startup();

        Thread.sleep(2000);


        String symbol = "btc-usdt";

        int index = 10;
        while (index-- > 0) {

            Order order = OrderBuildTool.build(null,
                    OrderType.limitBuy,
                    symbol,
                    new BigDecimal("1000.0"),
                    new BigDecimal("0.0001"));

            String keyPrefix = "submit-";

            writeClient.callRpcAsync(
                    "append",
                    Lists.newArrayList("order.btc-usdt", keyPrefix + order.getOrderId(), order));
        }

        Future<ResponseObject> resFuture3 =
                writeClient.callRpcAsync(
                        "get.latest.index",
                        Lists.newArrayList("order.btc-usdt"), false);
        ResponseObject resObj3 = resFuture3.get();
        logger.info("get.latest.index:{}", resObj3.getResult());

    }

}