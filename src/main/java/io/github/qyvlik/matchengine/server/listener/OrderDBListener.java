package io.github.qyvlik.matchengine.server.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.client.ChannelMessageHandler;
import io.github.qyvlik.jsonrpclite.core.client.RpcClient;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.jsonrpclite.core.jsonsub.pub.ChannelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;

public class OrderDBListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String host;
    private String symbol;
    private RpcClient listener;
    private RpcClient requester;
    private OrderCommandExecutor commandExecutor;
    private TreeMap<Long, QueueUpRecord> pendingRecord = new TreeMap<>();           // asc

    public OrderDBListener(String host, String symbol, OrderCommandExecutor commandExecutor) {
        this.host = host;
        this.symbol = symbol;
        this.commandExecutor = commandExecutor;
        this.listener = new RpcClient(host);
        this.requester = new RpcClient(host);

    }

    public void startupAndSub() throws IOException {
        this.listener.startup();
        this.requester.startup();

        sleep(2000L);

        String channel = "sub.append";

        String scope = "order." + symbol;

        listener.listenSub(channel, true, Lists.newArrayList(scope), new ChannelMessageHandler() {
            @Override
            public void handle(ChannelMessage channelMessage) {

                if (channelMessage.getResult() instanceof String
                        && channelMessage.getResult().toString().equalsIgnoreCase("subscribe")) {
                    return;
                }

                QueueUpRecord record = JSON.toJavaObject((JSON) channelMessage.getResult(), QueueUpRecord.class);

                Map.Entry<Long, QueueUpRecord> lastEntity = pendingRecord.lastEntry();
                Long last = lastEntity != null ? lastEntity.getKey() : null;
                if (last == null) {
                    pendingRecord.put(record.getIndex(), record);
                } else {
                    // index - last == 1
                    long gap = record.getIndex() - last;

                    if (gap > 1) {
                        // fetch for compensation
                        List<QueueUpRecord> list = getList(scope, last, record.getIndex());
                        if (list != null && list.size() > 0) {
                            for (QueueUpRecord queueUpRecord : list) {
                                pendingRecord.put(queueUpRecord.getIndex(), queueUpRecord);
                            }
                        }
                    } else if (gap == 1) {
                        pendingRecord.put(record.getIndex(), record);
                    } else {
                        // ignore
                    }
                }

                // to next

                List<QueueUpRecord> recordList = Lists.newArrayList(pendingRecord.values());
                pendingRecord.clear();

                if (commandExecutor != null) {
                    commandExecutor.exec(new OrderCommand(getSymbol(), recordList));
                }
            }
        });
    }

    public String getHost() {
        return host;
    }

    public String getSymbol() {
        return symbol;
    }

    public RpcClient getListener() {
        return listener;
    }

    public RpcClient getRequester() {
        return requester;
    }

    private void sleep(long ts) {
        try {
            Thread.sleep(ts);
        } catch (Exception e) {

        }
    }

    private List<QueueUpRecord> getList(String scope, long from, long to) {
        try {
            Future<ResponseObject> future = requester.callRpcAsync(
                    "get.list",
                    Lists.newArrayList(scope, from, to),
                    false);
            return ((JSONArray) future.get().getResult()).toJavaList(QueueUpRecord.class);
        } catch (Exception e) {
            logger.error("getList error:{}", e.getMessage());
        }
        return null;
    }
}
