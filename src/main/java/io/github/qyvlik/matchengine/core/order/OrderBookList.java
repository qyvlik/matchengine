package io.github.qyvlik.matchengine.core.order;

import com.google.common.collect.Maps;
import io.github.qyvlik.matchengine.core.order.vo.OrderBook;
import io.github.qyvlik.matchengine.core.order.vo.OrderBookKey;
import io.github.qyvlik.matchengine.core.order.vo.OrderSide;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class OrderBookList implements Serializable {
    private final ConcurrentSkipListMap<OrderBookKey, OrderBook> orderBookList;
    private final Map<String, OrderBook> orderMap;

    public OrderBookList(OrderSide orderSide) {
        Comparator<OrderBookKey> comparator = null;
        if (orderSide.equals(OrderSide.buy)) {
            // buyComparator
            comparator = new Comparator<OrderBookKey>() {
                public int compare(OrderBookKey bookItem1, OrderBookKey bookItem2) {

                    int priceCompare = bookItem2.getPrice().compareTo(bookItem1.getPrice());

                    if (priceCompare == 0) {
                        return bookItem1.getSeqId().compareTo(bookItem2.getSeqId());
                    }

                    return priceCompare;
                }
            };
        } else {
            // sellComparator
            comparator = new Comparator<OrderBookKey>() {
                public int compare(OrderBookKey bookItem1, OrderBookKey bookItem2) {

                    int priceCompare = bookItem1.getPrice().compareTo(bookItem2.getPrice());

                    if (priceCompare == 0) {
                        return bookItem1.getSeqId().compareTo(bookItem2.getSeqId());
                    }

                    return priceCompare;
                }
            };
        }
        this.orderBookList = new ConcurrentSkipListMap<OrderBookKey, OrderBook>(comparator);
        this.orderMap = Maps.newHashMap();
    }

    public void clear() {
        orderBookList.clear();
        orderMap.clear();
    }

    public boolean isEmpty() {
        return orderBookList.isEmpty();
    }

    public OrderBook getFirst() {
        return orderBookList.firstEntry().getValue();
    }

    public OrderBook removeFirst() {
        OrderBook orderBook = orderBookList.pollFirstEntry().getValue();
        if (orderBook != null) {
            orderMap.remove(orderBook.getOrderId());
        }
        return orderBook;
    }

    public void putOrderBook(OrderBook orderBook) {
        orderBookList.put(new OrderBookKey(orderBook.getPrice(), orderBook.getSeqId()), orderBook);
        orderMap.put(orderBook.getOrderId(), orderBook);
    }

    public OrderBook removeOrderBook(BigDecimal price, Long seqId) {
        OrderBook orderBook = orderBookList.remove(new OrderBookKey(price, seqId));
        if (orderBook != null) {
            orderMap.remove(orderBook.getOrderId());
        }
        return orderBook;
    }

    public OrderBook getOrderBookByOrderId(String orderId) {
        return orderMap.get(orderId);
    }

    public ConcurrentSkipListMap<OrderBookKey, OrderBook> getOrderBookList() {
        return orderBookList;
    }
}
