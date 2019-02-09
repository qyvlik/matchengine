package io.github.qyvlik.matchengine.server.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import io.github.qyvlik.jsonrpclite.core.client.ChannelMessageHandler;
import io.github.qyvlik.jsonrpclite.core.client.RpcClient;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.response.ResponseObject;
import io.github.qyvlik.jsonrpclite.core.jsonsub.pub.ChannelMessage;
import io.github.qyvlik.matchengine.utils.Collections3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;

public class OrderDBListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String host;
    private String symbol;
    private RpcClient listener;
    private RpcClient requester;
    private IOrderCommandExecutor commandExecutor;
    private TreeMap<Long, QueueUpRecord> pendingRecord = new TreeMap<>();           // asc

    public OrderDBListener(String host, String symbol, IOrderCommandExecutor commandExecutor) {
        this.host = host;
        this.symbol = symbol;
        this.commandExecutor = commandExecutor;
        this.listener = new RpcClient(host);
        this.requester = new RpcClient(host);
    }

    public void startupAndSub(Long seqId) throws IOException {
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

                if (channelMessage.getError() != null) {
                    logger.error("startupAndSub failure : channelMessage:{}", channelMessage);
                    return;
                }

                QueueUpRecord record = JSON.toJavaObject((JSON) channelMessage.getResult(), QueueUpRecord.class);

                Map.Entry<Long, QueueUpRecord> lastEntity = pendingRecord.lastEntry();
                Long last = lastEntity != null ? lastEntity.getKey() : null;

                long gap = 0;           // todo use enum

                if (last == null) {
                    if (seqId != null && seqId < record.getIndex()) {
                        gap = -2;
                    } else {
                        // seqId bigger than current record's index
                        // todo, but the record' index not order
                        gap = -1;
                    }
                } else {
                    gap = record.getIndex() - last;
                }

                if (gap == -2) {
                    List<QueueUpRecord> list = getList(scope, seqId, record.getIndex());
                    if (list != null && list.size() > 0) {
                        for (QueueUpRecord queueUpRecord : list) {
                            pendingRecord.put(queueUpRecord.getIndex(), queueUpRecord);
                        }
                    }
                } else if (gap == -1) {
                    pendingRecord.put(record.getIndex(), record);
                } else if (gap == 1) {
                    pendingRecord.put(record.getIndex(), record);
                } else if (gap > 1) {
                    // fetch for compensation
                    // `last` must not be null
                    List<QueueUpRecord> list = getList(scope, last, record.getIndex());
                    if (list != null && list.size() > 0) {
                        for (QueueUpRecord queueUpRecord : list) {
                            pendingRecord.put(queueUpRecord.getIndex(), queueUpRecord);
                        }
                    }
                } else {
                    // ignore
                }

                List<QueueUpRecord> recordList = Lists.newArrayList(pendingRecord.values());
                pendingRecord.clear();

                recordList.sort(new Comparator<QueueUpRecord>() {
                    @Override
                    public int compare(QueueUpRecord o1, QueueUpRecord o2) {
                        // asc
                        return o1.getIndex().compareTo(o2.getIndex());
                    }
                });

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
        List<List<Long>> splitList = Collections3.splitRangeToList(from, to, 100);
        if (Collections3.isEmpty(splitList)) {
            return null;
        }

        List<QueueUpRecord> recordList = Lists.newLinkedList();

        for (List<Long> req : splitList) {
            long begin = req.get(0);
            long end = req.get(1);
            if (begin == end) {
                try {
                    Future<ResponseObject> future = requester.callRpcAsync(
                            "get.by.index",
                            Lists.newArrayList(scope, begin),
                            false);
                    recordList.add(
                            JSON.toJavaObject(
                                    (JSON) future.get().getResult(),
                                    QueueUpRecord.class
                            )
                    );
                } catch (Exception e) {
                    logger.error("getList error:{}", e.getMessage());
                }
            } else {
                try {
                    Future<ResponseObject> future = requester.callRpcAsync(
                            "get.list",
                            Lists.newArrayList(scope, begin, end),
                            false);
                    recordList.addAll(
                            ((JSONArray) future.get().getResult()).toJavaList(QueueUpRecord.class)
                    );
                } catch (Exception e) {
                    logger.error("getList error:{}", e.getMessage());
                }
            }
        }
        // remove all null object
        recordList.removeAll(Collections.singleton(null));

        return recordList;
    }
}
