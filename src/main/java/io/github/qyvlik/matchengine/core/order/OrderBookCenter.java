package io.github.qyvlik.matchengine.core.order;

import com.google.common.collect.Lists;
import io.github.qyvlik.matchengine.core.durable.entity.OrderBookBackupItem;
import io.github.qyvlik.matchengine.core.order.vo.OrderBook;
import io.github.qyvlik.matchengine.core.order.vo.OrderSide;

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

    private void clearOrderBook() {
        asks.clear();
        asks.clear();
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
