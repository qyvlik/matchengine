package io.github.qyvlik.matchengine.server.listener;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.github.qyvlik.matchengine.core.matcher.request.CancelOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.request.PutOrderRequest;
import io.github.qyvlik.matchengine.core.order.vo.Order;

import java.io.Serializable;
import java.util.List;

public class OrderCommand implements Serializable {
    private String symbol;
    private List<QueueUpRecord> recordList;


    public OrderCommand() {

    }

    public OrderCommand(String symbol, List<QueueUpRecord> recordList) {
        this.symbol = symbol;
        this.recordList = recordList;
    }

    public static List<Object> convert(OrderCommand command) {
        List<Object> list = Lists.newLinkedList();

        if (command.getRecordList() == null || command.getRecordList().size() == 0) {
            return list;
        }

        for (QueueUpRecord record : command.getRecordList()) {
            Object request = convert(command.getSymbol(), record);
            if (request != null) {
                list.add(request);
            }
        }

        return list;
    }

    public static Object convert(String symbol, QueueUpRecord record) {
        if (record == null) {
            return null;
        }
        String key = record.getKey();

        Long seqId = record.getIndex();
        JSONObject data = (JSONObject) record.getData();

        if (key.startsWith("cancel-")) {
            return new CancelOrderRequest(symbol, data.getString("orderId"), seqId);
        } else if (key.startsWith("submit-")) {
            return new PutOrderRequest(symbol, seqId, data.toJavaObject(Order.class));
        }
        return null;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<QueueUpRecord> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<QueueUpRecord> recordList) {
        this.recordList = recordList;
    }

    @Override
    public String toString() {
        return "OrderCommand{" +
                "symbol='" + symbol + '\'' +
                ", recordList=" + recordList +
                '}';
    }
}
