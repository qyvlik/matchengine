package io.github.qyvlik.matchengine.core.order;

import com.google.common.collect.Lists;
import io.github.qyvlik.matchengine.core.durable.entity.OrderBookBackupItem;
import io.github.qyvlik.matchengine.core.order.vo.OrderBook;
import io.github.qyvlik.matchengine.core.order.vo.OrderSide;
import io.github.qyvlik.matchengine.utils.Collections3;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class OrderBookCenter implements Serializable {
    private final OrderBookList asks = new OrderBookList(OrderSide.sell);
    private final OrderBookList bids = new OrderBookList(OrderSide.buy);

    public OrderBookList getAsks() {
        return asks;
    }

    public OrderBookList getBids() {
        return bids;
    }

    public void restoreFromBackupItem(List<OrderBookBackupItem> items) {
        if (Collections3.isEmpty(items)) {
            return;
        }
        for (OrderBookBackupItem item : items) {
            OrderBook orderBook = item.getOrderBook();
            if (orderBook.getType().isBuy()) {
                bids.putOrderBook(orderBook);
            } else if (orderBook.getType().isSell()) {
                asks.putOrderBook(orderBook);
            } else {
                throw new RuntimeException("not support type:" + orderBook.getType());
            }
        }
    }

    public List<OrderBookBackupItem> backupAsks() {
        return backupOrderBooks(getAsks());
    }

    public List<OrderBookBackupItem> backupBids() {
        return backupOrderBooks(getBids());
    }

    private List<OrderBookBackupItem> backupOrderBooks(OrderBookList orderBookList) {
        Collection<OrderBook> orderBooks = orderBookList.getOrderBookList().values();
        List<OrderBookBackupItem> itemList = Lists.newLinkedList();
        Long currentSeqId = null;
        for (OrderBook orderBook : orderBooks) {
            OrderBookBackupItem item = new OrderBookBackupItem(currentSeqId, orderBook);
            itemList.add(item);
            currentSeqId = orderBook.getSeqId();
        }

        return itemList;
    }
}
