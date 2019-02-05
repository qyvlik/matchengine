package io.github.qyvlik.matchengine.server.durable;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.github.qyvlik.matchengine.core.durable.service.MatchEngineDBFactory;
import io.github.qyvlik.matchengine.core.matcher.vo.ExecuteResult;
import io.github.qyvlik.matchengine.core.matcher.vo.MatchDetailItem;
import io.github.qyvlik.matchengine.core.matcher.vo.OrderSnapshot;
import io.github.qyvlik.matchengine.core.order.OrderBookCenter;
import io.github.qyvlik.matchengine.core.order.vo.Order;
import org.apache.commons.lang3.StringUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;

import java.util.Collection;
import java.util.List;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

public class MatchEngineStoreService {

    private final static String ORDER = "order:";
    private final static String MATCH = "match:";
    private final static String LAST_MATCH_ID = "last.match.id";
    private final static String SEQ = "seq:";
    private final static String BACKUP = "backup:";

    private MatchEngineDBFactory matchEngineDBFactory;

    public MatchEngineStoreService(MatchEngineDBFactory matchEngineDBFactory) {
        this.matchEngineDBFactory = matchEngineDBFactory;
    }

    private static String orderIdKey(String symbol, String orderId) {
        return ORDER + orderId;
    }

    private static String orderSeqKey(String symbol, Long seqId) {
        return SEQ + seqId;
    }

    private static String orderMatchKey(String symbol, Long matchId) {
        return MATCH + matchId;
    }

    public List<String> getSymbolList() {
        Collection<String> cols = matchEngineDBFactory.getDbMap().keySet();
        List<String> list = Lists.newArrayList();
        for (String symbol : cols) {
            if (!symbol.equalsIgnoreCase("sys")) {
                list.add(symbol);
            }
        }
        return list;
    }

    public DB createSymbol(String symbol) {
        if (StringUtils.isBlank(symbol)) {
            throw new RuntimeException("symbol is empty");
        }
        return matchEngineDBFactory.createDBBySymbol(symbol, true);
    }

    /**
     * store order for putOrderRequest
     *
     * @param symbol
     * @param takerOrder
     * @param result
     */
    public void storeOrderForPutOrder(String symbol, Order takerOrder, ExecuteResult result) {
        if (StringUtils.isBlank(symbol)) {
            throw new RuntimeException("symbol is empty");
        }

        if (symbol.equalsIgnoreCase("sys")) {
            throw new RuntimeException("symbol:" + symbol + " is invalidate");
        }

        DB db = matchEngineDBFactory.createDBBySymbol(symbol, false);

        WriteBatch writeBatch = db.createWriteBatch();

        // save or update order
        if (result.getOrderSnapshots() != null && result.getOrderSnapshots().size() > 0) {
            for (OrderSnapshot orderSnapshot : result.getOrderSnapshots()) {

                // save taker order
                if (orderSnapshot.getOrderId().equals(takerOrder.getOrderId())) {

                    Order order = new Order(takerOrder);

                    order.setDealMoney(orderSnapshot.getDealMoney());
                    order.setDealStock(orderSnapshot.getDealStock());

                    order.setState(orderSnapshot.getState());

                    String takerOrderIdKey = orderIdKey(symbol, order.getOrderId());
                    String takerOrderSeqKey = orderSeqKey(symbol, order.getSeqId());
                    String takerOrderValue = JSON.toJSONString(order);
                    writeBatch.put(bytes(takerOrderIdKey), bytes(takerOrderValue));
                    writeBatch.put(bytes(takerOrderSeqKey), bytes(takerOrderIdKey));

                    continue;
                }

                String orderRawValue = asString(
                        db.get(bytes(orderIdKey(symbol, orderSnapshot.getOrderId()))));

                if (StringUtils.isBlank(orderRawValue)) {

                    continue;
                }

                Order updateOrder = JSON.parseObject(orderRawValue, Order.class);
                updateOrder.setDealMoney(orderSnapshot.getDealMoney());
                updateOrder.setDealStock(orderSnapshot.getDealStock());
                updateOrder.setState(orderSnapshot.getState());

                // update order
                writeBatch.put(
                        bytes(orderIdKey(symbol, updateOrder.getOrderId())),
                        bytes(JSON.toJSONString(updateOrder)));
            }
        }


        Long lastMatchId = getMatchLastId(db);

        List<MatchDetailItem> items = result.getItems();

        if (items != null && items.size() > 0) {
            for (MatchDetailItem matchDetailItem : items) {
                lastMatchId += 1;
                String orderMatchKey = orderMatchKey(symbol, lastMatchId);
                matchDetailItem.setId(lastMatchId);
                String orderMatchValue = JSON.toJSONString(matchDetailItem);

                writeBatch.put(bytes(orderMatchKey), bytes(orderMatchValue));
            }

            writeBatch.put(bytes(LAST_MATCH_ID), bytes(lastMatchId + ""));      //  update the last match id
        }

        db.write(writeBatch);
    }

    /**
     * store order for cancel order
     *
     * @param symbol
     * @param cancelOrderId
     * @param result
     */
    public void storeOrderForCancelOrder(String symbol, String cancelOrderId, ExecuteResult result) {
        if (StringUtils.isBlank(symbol)) {
            throw new RuntimeException("symbol is empty");
        }

        if (symbol.equalsIgnoreCase("sys")) {
            throw new RuntimeException("symbol:" + symbol + " is invalidate");
        }

        DB db = matchEngineDBFactory.createDBBySymbol(symbol, false);
        WriteBatch writeBatch = db.createWriteBatch();

        // update cancel order
        if (result.getOrderSnapshots() != null && result.getOrderSnapshots().size() > 0) {
            for (OrderSnapshot orderSnapshot : result.getOrderSnapshots()) {

                String orderRawValue = asString(
                        db.get(bytes(orderIdKey(symbol, orderSnapshot.getOrderId()))));

                if (StringUtils.isBlank(orderRawValue)) {

                    continue;
                }

                Order updateOrder = JSON.parseObject(orderRawValue, Order.class);
                updateOrder.setDealMoney(orderSnapshot.getDealMoney());
                updateOrder.setDealStock(orderSnapshot.getDealStock());
                updateOrder.setState(orderSnapshot.getState());

                // update cancel order
                writeBatch.put(
                        bytes(orderIdKey(symbol, updateOrder.getOrderId())),
                        bytes(JSON.toJSONString(updateOrder)));
            }
        }

        Long lastMatchId = getMatchLastId(db);

        List<MatchDetailItem> items = result.getItems();

        if (items != null && items.size() > 0) {
            for (MatchDetailItem matchDetailItem : items) {
                lastMatchId += 1;
                String orderMatchKey = orderMatchKey(symbol, lastMatchId);
                matchDetailItem.setId(lastMatchId);
                String orderMatchValue = JSON.toJSONString(matchDetailItem);

                writeBatch.put(bytes(orderMatchKey), bytes(orderMatchValue));
            }

            writeBatch.put(bytes(LAST_MATCH_ID), bytes(lastMatchId + ""));      //  update the last match id
        }

        db.write(writeBatch);
    }

    public boolean deleteOrder(String symbol, String orderId) {
        if (StringUtils.isBlank(symbol)) {
            throw new RuntimeException("symbol is empty");
        }

        if (symbol.equalsIgnoreCase("sys")) {
            throw new RuntimeException("symbol:" + symbol + " is invalidate");
        }

        Order order = getOrderByOrderId(symbol, orderId);

        if (order == null) {
            return false;
        }

        if (!order.getState().isFinal()) {
            return false;
        }

        DB db = matchEngineDBFactory.createDBBySymbol(symbol, false);
        WriteBatch writeBatch = db.createWriteBatch();
        writeBatch.delete(bytes(orderIdKey(symbol, order.getOrderId())));
        writeBatch.delete(bytes(orderSeqKey(symbol, order.getSeqId())));

        db.write(writeBatch);
        return true;
    }

    public void deleteMatch(String symbol, Long matchId) {
        if (StringUtils.isBlank(symbol)) {
            throw new RuntimeException("symbol is empty");
        }

        if (symbol.equalsIgnoreCase("sys")) {
            throw new RuntimeException("symbol:" + symbol + " is invalidate");
        }

        DB db = matchEngineDBFactory.createDBBySymbol(symbol, false);

        WriteBatch writeBatch = db.createWriteBatch();

        writeBatch.delete(bytes(orderMatchKey(symbol, matchId)));

        db.write(writeBatch);
    }

    public Order getOrderByOrderId(String symbol, String orderId) {
        if (StringUtils.isBlank(symbol)) {
            throw new RuntimeException("symbol is empty");
        }

        if (symbol.equalsIgnoreCase("sys")) {
            throw new RuntimeException("symbol:" + symbol + " is invalidate");
        }

        DB db = matchEngineDBFactory.createDBBySymbol(symbol, false);

        String orderRawValue = asString(
                db.get(bytes(orderIdKey(symbol, orderId))));

        if (StringUtils.isBlank(orderRawValue)) {
            return null;
        }


        return JSON.parseObject(orderRawValue, Order.class);
    }

    private Long getMatchLastId(DB db) {
        Long lastMatchId = 0L;
        String lastMatchIdString = asString(db.get(bytes(LAST_MATCH_ID)));
        if (StringUtils.isNotBlank(lastMatchIdString)) {
            lastMatchId = Long.parseLong(lastMatchIdString);
        }
        return lastMatchId;
    }

    public Long getMatchLastId(String symbol) {
        DB db = matchEngineDBFactory.createDBBySymbol(symbol, false);
        return getMatchLastId(db);
    }

    public MatchDetailItem getMatchItemByMatchId(String symbol, Long matchId) {
        if (StringUtils.isBlank(symbol)) {
            throw new RuntimeException("symbol is empty");
        }

        if (symbol.equalsIgnoreCase("sys")) {
            throw new RuntimeException("symbol:" + symbol + " is invalidate");
        }

        DB db = matchEngineDBFactory.createDBBySymbol(symbol, false);
        String matchRawValue = asString(
                db.get(bytes(orderMatchKey(symbol, matchId))));

        if (StringUtils.isBlank(matchRawValue)) {
            return null;
        }

        return JSON.parseObject(matchRawValue, MatchDetailItem.class);
    }

    public Order getOrderBySeqId(String symbol, Long seqId) {
        if (StringUtils.isBlank(symbol)) {
            throw new RuntimeException("symbol is empty");
        }

        if (symbol.equalsIgnoreCase("sys")) {
            throw new RuntimeException("symbol:" + symbol + " is invalidate");
        }

        DB db = matchEngineDBFactory.createDBBySymbol(symbol, false);
        byte[] orderIdRawValue =
                db.get(bytes(orderSeqKey(symbol, seqId)));

        if (orderIdRawValue == null || orderIdRawValue.length == 0) {
            return null;
        }

        String orderRawValue = asString(db.get(orderIdRawValue));
        if (StringUtils.isBlank(orderRawValue)) {
            return null;
        }

        return JSON.parseObject(orderRawValue, Order.class);
    }

    public Long backupOrderBookCenter(String symbol, OrderBookCenter orderBookCenter) {
        if (StringUtils.isBlank(symbol)) {
            throw new RuntimeException("symbol is empty");
        }

        if (symbol.equalsIgnoreCase("sys")) {
            throw new RuntimeException("symbol:" + symbol + " is invalidate");
        }

        // todo
        return null;
    }

}
