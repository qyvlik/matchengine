package io.github.qyvlik.matchengine.core.matcher.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.qyvlik.matchengine.core.matcher.request.CancelOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.request.PutOrderRequest;
import io.github.qyvlik.matchengine.core.matcher.vo.ExecuteResult;
import io.github.qyvlik.matchengine.core.matcher.vo.MatchDetailItem;
import io.github.qyvlik.matchengine.core.matcher.vo.MatchEvent;
import io.github.qyvlik.matchengine.core.matcher.vo.OrderSnapshot;
import io.github.qyvlik.matchengine.core.order.OrderBookCenter;
import io.github.qyvlik.matchengine.core.order.OrderBookList;
import io.github.qyvlik.matchengine.core.order.vo.Order;
import io.github.qyvlik.matchengine.core.order.vo.OrderBook;
import io.github.qyvlik.matchengine.core.order.vo.OrderState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class MatchEngine {
    public static final int TAKER_ONLY_PLACE = 0;
    public static final int MAKER_FILL_AND_TAKER_PART = 1;
    public static final int MAKER_PART_AND_TAKER_FILL = 2;
    public static final int MAKER_FILL_AND_TAKER_FILL = 3;
    private final OrderBookCenter orderBookCenter = new OrderBookCenter();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String symbol;

    public MatchEngine(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderBookCenter getOrderBookCenter() {
        return orderBookCenter;
    }

    public ExecuteResult executeCancelOrder(CancelOrderRequest request) {
        OrderBook cancelledOrderBook = null;
        OrderBookList orderBookList = orderBookCenter.getBids();
        cancelledOrderBook = orderBookList.getOrderBookByOrderId(request.getOrderId());
        if (cancelledOrderBook == null) {
            orderBookList = orderBookCenter.getAsks();
            cancelledOrderBook = orderBookList.getOrderBookByOrderId(request.getOrderId());
        }

        if (cancelledOrderBook == null) {
            logger.error("matchEngine executeCancelOrder return:{} not in engine", request.getOrderId());
            return new ExecuteResult();
        }

        ExecuteResult result = new ExecuteResult();

        MatchDetailItem item = new MatchDetailItem();
        item.setEvent(MatchEvent.cancel);
        item.setMatchIndex(0);
        item.setSeqId(request.getSeqId());
        item.setSymbol(request.getSymbol());

        item.setTimestamp(System.currentTimeMillis());

        item.setCancelOrderType(cancelledOrderBook.getType());
        item.setCancelOrderId(cancelledOrderBook.getOrderId());
        item.setCancelPrice(cancelledOrderBook.getPrice());

        if (cancelledOrderBook.getOrderStock().compareTo(cancelledOrderBook.getStock()) > 0) {
            item.setCancelOrderState(OrderState.partialCancel);
        } else {
            item.setCancelOrderState(OrderState.cancel);
        }

        orderBookList.removeOrderBook(cancelledOrderBook.getPrice(), cancelledOrderBook.getSeqId());

        BigDecimal unFillStock = cancelledOrderBook.getStock();
        BigDecimal price = cancelledOrderBook.getPrice();
        BigDecimal unFillMoney = unFillStock.multiply(price);

        switch (cancelledOrderBook.getType()) {
            case limitBuy:
            case limitSell: {
                item.setCancelUnFillStock(unFillStock);                    // 币本位，需要提供给深度进行更新
                item.setCancelUnFillMoney(unFillMoney);                    // 归还钱
            }
            break;

            default:
                throw new RuntimeException("not support orderType!");
        }

        result.setOrderSnapshots(Lists.newArrayList(
                OrderBook.snapshotOrderBook(cancelledOrderBook, false)));
        result.setItems(Lists.newArrayList(item));

        return result;
    }


    public ExecuteResult executeLimitOrder(PutOrderRequest request) {
        ExecuteResult result = new ExecuteResult();

        Order exchangeOrder = request.getOrder();
        OrderBook orderBook = null;
        OrderBookList orderBookList = null;
        if (exchangeOrder.getType().isBuy()) {
            orderBookList = orderBookCenter.getBids();
        } else {
            orderBookList = orderBookCenter.getAsks();
        }

        orderBook = orderBookList.getOrderBookByOrderId(exchangeOrder.getOrderId());

        // repeat order
        if (orderBook != null) {
            logger.info("executeLimitOrder repeat error:{}", request);
            return result;
        }

        OrderBook takerOrder = OrderBook.adaptorLimitPriceOrder(exchangeOrder);

        OrderBookList makerOrderBookList = null;
        OrderBookList takerOrderBookList = null;

        if (takerOrder.getType().isBuy()) {
            makerOrderBookList = orderBookCenter.getAsks();
            takerOrderBookList = orderBookCenter.getBids();
        } else {
            makerOrderBookList = orderBookCenter.getBids();
            takerOrderBookList = orderBookCenter.getAsks();
        }

        int matchFlag = TAKER_ONLY_PLACE;

        List<MatchDetailItem> detailItems = Lists.newLinkedList();
        Map<String, OrderSnapshot> orderSnapshotMap = Maps.newHashMap();

        while (BigDecimal.ZERO.compareTo(takerOrder.getStock()) < 0) {
            if (makerOrderBookList.isEmpty()) {
                placeTakerOrder(exchangeOrder,
                        takerOrder,
                        takerOrderBookList,
                        matchFlag,
                        detailItems);

                orderSnapshotMap.put(takerOrder.getOrderId(),
                        OrderBook.snapshotOrderBook(takerOrder, true));
                break;
            }

            OrderBook makerOrder = makerOrderBookList.getFirst();

            int priceCompare = takerOrder.getPrice()
                    .compareTo(makerOrder.getPrice());

            // 无法成交的价格，直接添加到深度中
            if ((takerOrder.getType().isBuy() && priceCompare < 0)
                    || (takerOrder.getType().isSell() && priceCompare > 0)) {
                placeTakerOrder(exchangeOrder,
                        takerOrder,
                        takerOrderBookList,
                        matchFlag,
                        detailItems);
                orderSnapshotMap.put(takerOrder.getOrderId(),
                        OrderBook.snapshotOrderBook(takerOrder, true));
                break;
            }

            MatchDetailItem matchDetailItem = new MatchDetailItem();
            matchDetailItem.setEvent(MatchEvent.match);
            matchDetailItem.setSeqId(exchangeOrder.getSeqId());
            matchDetailItem.setSymbol(exchangeOrder.getSymbol());
            matchDetailItem.setMatchIndex(detailItems.size());

            matchDetailItem.setTimestamp(System.currentTimeMillis());

            matchDetailItem.setTakerOrderId(takerOrder.getOrderId());
            matchDetailItem.setTakerOrderType(takerOrder.getType());
            matchDetailItem.setTakerPrice(takerOrder.getPrice());

            matchDetailItem.setMakerOrderId(makerOrder.getOrderId());
            matchDetailItem.setMakerOrderType(makerOrder.getType());

            matchDetailItem.setMakerPrice(makerOrder.getPrice());              // maker price

            int amountCompare = makerOrder.getStock().compareTo(
                    takerOrder.getStock());

            BigDecimal fillStock = null;
            BigDecimal fillMoney = null;
            if (amountCompare > 0) {
                // taker was filled
                matchFlag = MAKER_PART_AND_TAKER_FILL;

                fillStock = takerOrder.getStock();

                OrderBook.modifyOrderBookStock(takerOrder, BigDecimal.ZERO);

                BigDecimal makerUnFillStock = makerOrder.getStock().subtract(fillStock);
                OrderBook.modifyOrderBookStock(makerOrder, makerUnFillStock);

                matchDetailItem.setMakerOrderState(OrderState.partialFill);
                matchDetailItem.setTakerOrderState(OrderState.filled);

            } else if (amountCompare < 0) {
                // maker was filled
                matchFlag = MAKER_FILL_AND_TAKER_PART;

                fillStock = makerOrder.getStock();

                OrderBook.modifyOrderBookStock(makerOrder, BigDecimal.ZERO);

                BigDecimal takerUnFillStock = takerOrder.getStock().subtract(fillStock);
                OrderBook.modifyOrderBookStock(takerOrder, takerUnFillStock);

                matchDetailItem.setMakerOrderState(OrderState.filled);
                matchDetailItem.setTakerOrderState(OrderState.partialFill);

            } else {
                // both taker and maker are filled
                matchFlag = MAKER_FILL_AND_TAKER_FILL;

                fillStock = takerOrder.getStock();

                OrderBook.modifyOrderBookStock(takerOrder, BigDecimal.ZERO);
                OrderBook.modifyOrderBookStock(makerOrder, BigDecimal.ZERO);

                matchDetailItem.setMakerOrderState(OrderState.filled);
                matchDetailItem.setTakerOrderState(OrderState.filled);
            }

            fillMoney = makerOrder.getPrice().multiply(fillStock);

            matchDetailItem.setFillStock(fillStock);
            matchDetailItem.setFillMoney(fillMoney);

            // taker free money
            if (takerOrder.getType().isBuy()) {
                BigDecimal priceGap = takerOrder.getPrice().subtract(makerOrder.getPrice());
                BigDecimal refundMoney = priceGap.abs().multiply(matchDetailItem.getFillStock());
                matchDetailItem.setRefundMoney(refundMoney);
            }

            orderSnapshotMap.put(takerOrder.getOrderId(),
                    OrderBook.snapshotOrderBook(takerOrder, true));

            orderSnapshotMap.put(makerOrder.getOrderId(),
                    OrderBook.snapshotOrderBook(makerOrder, true));

            detailItems.add(matchDetailItem);

            // 如果 taker 不是完全成交，则忽略
            if (matchFlag != MAKER_PART_AND_TAKER_FILL) {
                OrderBook removedMakerOrderBook =
                        makerOrderBookList.removeFirst();
            }

            // 如果 taker 不是部分成交，既 taker 是完全成交，那么应该退出循环
            if (matchFlag != MAKER_FILL_AND_TAKER_PART) {
                break;
            }
        }

        result.setOrderSnapshots(Lists.newArrayList(orderSnapshotMap.values()));
        result.setItems(detailItems);

        return result;
    }

    // place taker  into order book
    private void placeTakerOrder(Order exchangeOrder,
                                 OrderBook takerOrder,
                                 OrderBookList takerOrderBookListService,
                                 int matchFlag,
                                 List<MatchDetailItem> matchResultItemList) {
        takerOrderBookListService.putOrderBook(takerOrder);
        OrderState orderState = matchFlag != TAKER_ONLY_PLACE
                ? OrderState.partialFill
                : OrderState.submitted;

        int nextIndex = matchResultItemList.size();

        MatchDetailItem placeItem = adaptorPlaceItem(
                exchangeOrder.getSeqId(),
                nextIndex,
                exchangeOrder,
                orderState,
                takerOrder);

        matchResultItemList.add(placeItem);
    }


    private MatchDetailItem adaptorPlaceItem(Long sequenceId,
                                             Integer index,
                                             Order takerExchangeOrder,
                                             OrderState orderState,
                                             OrderBook takerOrder) {
        MatchDetailItem placeItem = new MatchDetailItem();
        placeItem.setEvent(MatchEvent.place);
        placeItem.setSeqId(sequenceId);
        placeItem.setMatchIndex(index);
        placeItem.setSymbol(takerExchangeOrder.getSymbol());

        placeItem.setTimestamp(System.currentTimeMillis());

        placeItem.setTakerOrderId(takerOrder.getOrderId());
        placeItem.setTakerOrderType(takerOrder.getType());
        placeItem.setTakerOrderState(orderState);

        placeItem.setTakerPrice(takerOrder.getPrice());
        placeItem.setTakerUnFillStock(takerOrder.getStock());
        placeItem.setTakerUnFillMoney(takerOrder.getMoney());

        return placeItem;
    }

}
