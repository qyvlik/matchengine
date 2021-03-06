package io.github.qyvlik.matchengine.server.durable;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.github.qyvlik.matchengine.core.durable.entity.OrderBookBackupInfo;
import io.github.qyvlik.matchengine.core.durable.entity.OrderBookBackupItem;
import io.github.qyvlik.matchengine.core.durable.service.MatchEngineDBFactory;
import io.github.qyvlik.matchengine.core.matcher.vo.ExecuteResult;
import io.github.qyvlik.matchengine.core.matcher.vo.MatchDetailItem;
import io.github.qyvlik.matchengine.core.matcher.vo.OrderSnapshot;
import io.github.qyvlik.matchengine.core.order.OrderBookCenter;
import io.github.qyvlik.matchengine.core.order.vo.Order;
import org.apache.commons.lang3.StringUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

public class MatchEngineStoreService {

    private final static String ORDER = "order:";
    private final static String ORDER_MATCH = "order-match:";
    private final static String ORDER_MATCH_LAST_ID = "order-match-last-id";
    private final static String ORDER_SEQ = "order-seq:";
    private final static String ORDER_ACTION = "order-action:";
    private final static String ORDER_ACTION_LAST_SEQ_ID = "order-action-last-seq";

    /**
     * OrderBookBackupInfo
     */
    private final static String ORDER_BOOK_BACKUP_INFO = "order-backup-info:";

    /**
     * key such as : backup:1:1, backup:1:2, ...
     * backup:${id}:${index}
     */
    private final static String ORDER_BOOK_BACKUP_ITEM = "order-backup:";

    private final static String ORDER_BOOK_LAST_BACKUP_ID = "last.backup.id";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private MatchEngineDBFactory matchEngineDBFactory;

    public MatchEngineStoreService(MatchEngineDBFactory matchEngineDBFactory) {
        this.matchEngineDBFactory = matchEngineDBFactory;
    }

    private static String keyOfOrderId(String symbol, String orderId) {
        return ORDER + orderId;
    }

    private static String keyOfOrderSeqId(String symbol, Long seqId) {
        return ORDER_SEQ + seqId;
    }

    private static String keyOfOrderAction(String symbol, Long seqId) {
        return ORDER_ACTION + seqId;
    }

    private static String keyOrOrderMatchKey(String symbol, Long matchId) {
        return ORDER_MATCH + matchId;
    }

    private static String keyOfOrderBookLastBackupId(String symbol) {
        return ORDER_BOOK_LAST_BACKUP_ID;
    }

    private static String keyOfOrderBookBackupInfo(String symbol, Long backupId) {
        return ORDER_BOOK_BACKUP_INFO + backupId;
    }

    private static String keyOfOrderBookBackupItem(String symbol, Long backupId, Long seqId) {
        return ORDER_BOOK_BACKUP_ITEM + backupId + ":" + seqId;
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

        // save order-action
        writeBatch.put(
                bytes(keyOfOrderAction(symbol, takerOrder.getSeqId())),
                bytes("submit-" + takerOrder.getOrderId())
        );

        // save or update order
        if (result.getOrderSnapshots() != null && result.getOrderSnapshots().size() > 0) {
            for (OrderSnapshot orderSnapshot : result.getOrderSnapshots()) {

                // save taker order
                if (orderSnapshot.getOrderId().equals(takerOrder.getOrderId())) {

                    Order order = new Order(takerOrder);

                    order.setDealMoney(orderSnapshot.getDealMoney());
                    order.setDealStock(orderSnapshot.getDealStock());

                    order.setState(orderSnapshot.getState());

                    String takerOrderIdKey = keyOfOrderId(symbol, order.getOrderId());
                    String takerOrderSeqKey = keyOfOrderSeqId(symbol, order.getSeqId());
                    String takerOrderValue = JSON.toJSONString(order);
                    writeBatch.put(bytes(takerOrderIdKey), bytes(takerOrderValue));
                    writeBatch.put(bytes(takerOrderSeqKey), bytes(takerOrderIdKey));

                    continue;
                }

                String orderRawValue = asString(
                        db.get(bytes(keyOfOrderId(symbol, orderSnapshot.getOrderId()))));

                if (StringUtils.isBlank(orderRawValue)) {

                    continue;
                }

                Order updateOrder = JSON.parseObject(orderRawValue, Order.class);
                updateOrder.setDealMoney(orderSnapshot.getDealMoney());
                updateOrder.setDealStock(orderSnapshot.getDealStock());
                updateOrder.setState(orderSnapshot.getState());

                // update order
                writeBatch.put(
                        bytes(keyOfOrderId(symbol, updateOrder.getOrderId())),
                        bytes(JSON.toJSONString(updateOrder)));
            }
        }


        Long lastMatchId = getOrderMatchLastId(db);

        List<MatchDetailItem> items = result.getItems();

        if (items != null && items.size() > 0) {
            for (MatchDetailItem matchDetailItem : items) {
                lastMatchId += 1;
                String orderMatchKey = keyOrOrderMatchKey(symbol, lastMatchId);
                matchDetailItem.setId(lastMatchId);
                String orderMatchValue = JSON.toJSONString(matchDetailItem);

                writeBatch.put(bytes(orderMatchKey), bytes(orderMatchValue));
            }

            writeBatch.put(bytes(ORDER_MATCH_LAST_ID), bytes(lastMatchId + ""));      //  update the last match id
        }

        writeBatch.put(bytes(ORDER_ACTION_LAST_SEQ_ID), bytes(takerOrder.getSeqId() + ""));      //  update the last seq id

        db.write(writeBatch);
    }

    /**
     * store order for cancel order
     *
     * @param symbol
     * @param cancelOrderId
     * @param result
     */
    public void storeOrderForCancelOrder(String symbol, Long seqId, String cancelOrderId, ExecuteResult result) {
        if (StringUtils.isBlank(symbol)) {
            throw new RuntimeException("symbol is empty");
        }

        if (symbol.equalsIgnoreCase("sys")) {
            throw new RuntimeException("symbol:" + symbol + " is invalidate");
        }

        DB db = matchEngineDBFactory.createDBBySymbol(symbol, false);
        WriteBatch writeBatch = db.createWriteBatch();


        // save order-action
        writeBatch.put(
                bytes(keyOfOrderAction(symbol, seqId)),
                bytes("submit-" + cancelOrderId)
        );

        // update cancel order
        if (result.getOrderSnapshots() != null && result.getOrderSnapshots().size() > 0) {
            for (OrderSnapshot orderSnapshot : result.getOrderSnapshots()) {

                String orderRawValue = asString(
                        db.get(bytes(keyOfOrderId(symbol, orderSnapshot.getOrderId()))));

                if (StringUtils.isBlank(orderRawValue)) {

                    continue;
                }

                Order updateOrder = JSON.parseObject(orderRawValue, Order.class);
                updateOrder.setDealMoney(orderSnapshot.getDealMoney());
                updateOrder.setDealStock(orderSnapshot.getDealStock());
                updateOrder.setState(orderSnapshot.getState());

                // update cancel order
                writeBatch.put(
                        bytes(keyOfOrderId(symbol, updateOrder.getOrderId())),
                        bytes(JSON.toJSONString(updateOrder)));
            }
        }

        Long lastMatchId = getOrderMatchLastId(db);

        List<MatchDetailItem> items = result.getItems();

        if (items != null && items.size() > 0) {
            for (MatchDetailItem matchDetailItem : items) {
                lastMatchId += 1;
                String orderMatchKey = keyOrOrderMatchKey(symbol, lastMatchId);
                matchDetailItem.setId(lastMatchId);
                String orderMatchValue = JSON.toJSONString(matchDetailItem);

                writeBatch.put(bytes(orderMatchKey), bytes(orderMatchValue));
            }

            writeBatch.put(bytes(ORDER_MATCH_LAST_ID), bytes(lastMatchId + ""));      //  update the last match id
        }

        writeBatch.put(bytes(ORDER_ACTION_LAST_SEQ_ID), bytes(seqId + ""));      //  update the last seq id

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
        writeBatch.delete(bytes(keyOfOrderId(symbol, order.getOrderId())));
        writeBatch.delete(bytes(keyOfOrderSeqId(symbol, order.getSeqId())));

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

        writeBatch.delete(bytes(keyOrOrderMatchKey(symbol, matchId)));

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
                db.get(bytes(keyOfOrderId(symbol, orderId))));

        if (StringUtils.isBlank(orderRawValue)) {
            return null;
        }


        return JSON.parseObject(orderRawValue, Order.class);
    }

    private Long getOrderMatchLastId(DB db) {
        Long lastMatchId = 0L;
        String lastMatchIdString = asString(db.get(bytes(ORDER_MATCH_LAST_ID)));
        if (StringUtils.isNotBlank(lastMatchIdString)) {
            lastMatchId = Long.parseLong(lastMatchIdString);
        }
        return lastMatchId;
    }

    private String getOrderAction(DB db, String symbol, Long seqId) {
        return asString(db.get(bytes(keyOfOrderAction(symbol, seqId))));
    }

    private Long getOrderActionLastSeqId(DB db) {
        Long lastMatchId = 0L;
        String lastSeqIdString = asString(db.get(bytes(ORDER_ACTION_LAST_SEQ_ID)));
        if (StringUtils.isNotBlank(lastSeqIdString)) {
            lastMatchId = Long.parseLong(lastSeqIdString);
        }
        return lastMatchId;
    }

    public Long getOrderActionLastSeqId(String symbol) {
        DB db = matchEngineDBFactory.createDBBySymbol(symbol, false);
        return getOrderActionLastSeqId(db);
    }

    /**
     * @param symbol
     * @param seqId
     * @return `submit-xxxx` or `cancel-xxx`
     */
    public String getOrderAction(String symbol, Long seqId) {
        DB db = matchEngineDBFactory.createDBBySymbol(symbol, false);
        return getOrderAction(db, symbol, seqId);
    }

    public Long getLastMatchId(String symbol) {
        DB db = matchEngineDBFactory.createDBBySymbol(symbol, false);
        return getOrderMatchLastId(db);
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
                db.get(bytes(keyOrOrderMatchKey(symbol, matchId))));

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
                db.get(bytes(keyOfOrderSeqId(symbol, seqId)));

        if (orderIdRawValue == null || orderIdRawValue.length == 0) {
            return null;
        }

        String orderRawValue = asString(db.get(orderIdRawValue));
        if (StringUtils.isBlank(orderRawValue)) {
            return null;
        }

        return JSON.parseObject(orderRawValue, Order.class);
    }

    private long getOrderBookLastBackupId(DB db, String symbol) {
        byte[] rawValue = db.get(bytes(keyOfOrderBookLastBackupId(symbol)));
        if (rawValue == null || rawValue.length == 0) {
            return 0L;
        }
        try {
            return Long.parseLong(asString(rawValue));
        } catch (Exception e) {
            logger.error("getOrderBookLastBackupId parseLong error:{}", e.getMessage());
            return 0L;
        }
    }

    private OrderBookBackupInfo getOrderBookBackupInfo(DB db, String symbol, Long backupId) {
        byte[] rawValue = db.get(bytes(keyOfOrderBookBackupInfo(symbol, backupId)));
        if (rawValue == null || rawValue.length == 0) {
            return null;
        }

        try {
            return JSON.parseObject(asString(rawValue)).toJavaObject(OrderBookBackupInfo.class);
        } catch (Exception e) {
            logger.error("getOrderBookBackupInfo parseObject error:{}", e.getMessage());
            return null;
        }
    }

    private OrderBookBackupItem getOrderBookBackupItem(DB db, String symbol, Long backupId, Long seqId) {
        String key = keyOfOrderBookBackupItem(symbol, backupId, seqId);
        byte[] rawValue = db.get(bytes(key));
        if (rawValue == null || rawValue.length == 0) {
            return null;
        }

        try {
            return JSON.parseObject(asString(rawValue)).toJavaObject(OrderBookBackupItem.class);
        } catch (Exception e) {
            logger.error("getOrderBookBackupItem parseObject error:{}", e.getMessage());
            return null;
        }
    }

    public Long backupOrderBookCenter(String symbol, OrderBookCenter orderBookCenter) {
        if (StringUtils.isBlank(symbol)) {
            throw new RuntimeException("symbol is empty");
        }

        if (symbol.equalsIgnoreCase("sys")) {
            throw new RuntimeException("symbol:" + symbol + " is invalidate");
        }

        logger.info("backupOrderBookCenter start symbol:{}", symbol);

        DB db = matchEngineDBFactory.createDBBySymbol(symbol, false);

        long latestBackupId = getOrderBookLastBackupId(db, symbol);

        long currentBackupId = latestBackupId + 1;

        WriteBatch writeBatch = db.createWriteBatch();

        Long backupStartSeqId = null;
        Long backupEndSeqId = null;

        List<OrderBookBackupItem> asks = orderBookCenter.backupAsks();
        List<OrderBookBackupItem> bids = orderBookCenter.backupBids();

        List<OrderBookBackupItem> items = Lists.newLinkedList();
        items.addAll(asks);
        items.addAll(bids);

        int itemsSize = items.size();

        for (OrderBookBackupItem item : items) {
            String key = keyOfOrderBookBackupItem(symbol, currentBackupId, item.getOrderBook().getSeqId());
            String value = JSON.toJSONString(item);
            writeBatch.put(bytes(key), bytes(value));

            if (backupStartSeqId == null) {
                backupStartSeqId = item.getOrderBook().getSeqId();
            }
            backupEndSeqId = item.getOrderBook().getSeqId();
        }

        asks.clear();
        bids.clear();
        items.clear();

        OrderBookBackupInfo orderBookBackupInfo = new OrderBookBackupInfo(
                currentBackupId, latestBackupId, backupStartSeqId, backupEndSeqId);

        // update latest backup id
        writeBatch.put(bytes(keyOfOrderBookLastBackupId(symbol)), bytes(currentBackupId + ""));

        // update the backup info
        writeBatch.put(bytes(keyOfOrderBookBackupInfo(symbol, currentBackupId)), bytes(JSON.toJSONString(orderBookBackupInfo)));

        db.write(writeBatch);

        logger.info("backupOrderBookCenter end symbol:{}, itemsSize:{}", symbol, itemsSize);

        return currentBackupId;
    }

    public Long getOrderBookLastBackupId(String symbol) {
        DB db = matchEngineDBFactory.createDBBySymbol(symbol, false);
        return getOrderBookLastBackupId(db, symbol);
    }

    public List<OrderBookBackupItem> getOrderBookBackupItems(String symbol, Long backupId) {
        DB db = matchEngineDBFactory.createDBBySymbol(symbol, false);

        OrderBookBackupInfo orderBookBackupInfo = getOrderBookBackupInfo(db, symbol, backupId);

        List<OrderBookBackupItem> items = Lists.newLinkedList();

        if (orderBookBackupInfo == null
                || orderBookBackupInfo.getBackupStartSeqId() == null) {
            return items;
        }

        long currentSeqId = orderBookBackupInfo.getBackupEndSeqId();

        while (true) {
            OrderBookBackupItem item = getOrderBookBackupItem(db, symbol, backupId, currentSeqId);
            if (item == null) {
                break;
            }
            items.add(item);

            if (item.getPrevSeqId() == null) {
                break;
            }

            currentSeqId = item.getPrevSeqId();         // prev seq id
        }

        items.sort(new Comparator<OrderBookBackupItem>() {
            @Override
            public int compare(OrderBookBackupItem o1, OrderBookBackupItem o2) {
                return o1.getOrderBook().getSeqId().compareTo(o2.getOrderBook().getSeqId());
            }
        });

        return items;
    }

}
