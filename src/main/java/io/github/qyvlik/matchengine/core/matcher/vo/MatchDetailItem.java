package io.github.qyvlik.matchengine.core.matcher.vo;

import io.github.qyvlik.matchengine.core.order.vo.OrderState;
import io.github.qyvlik.matchengine.core.order.vo.OrderType;

import java.io.Serializable;
import java.math.BigDecimal;

public class MatchDetailItem implements Serializable {
    private Long id;
    private Long seqId;
    private Integer matchIndex;
    private MatchEvent event;
    private String symbol;
    private String quote;
    private String base;
    private Long timestamp;

    private String takerOrderId;
    private OrderType takerOrderType;
    private OrderState takerOrderState;
    private BigDecimal takerPrice;
    private BigDecimal takerUnFillStock;
    private BigDecimal takerUnFillMoney;

    private String makerOrderId;
    private OrderType makerOrderType;
    private OrderState makerOrderState;
    private BigDecimal makerPrice;
    private BigDecimal fillStock;
    private BigDecimal fillMoney;
    private BigDecimal refundMoney;         // only for taker is buyer
    private BigDecimal refundStock;

    private String cancelOrderId;
    private OrderType cancelOrderType;
    private BigDecimal cancelPrice;
    private BigDecimal cancelUnFillStock;         // only for event is cancel
    private BigDecimal cancelUnFillMoney;         // only for event is cancel
    private OrderState cancelOrderState;

    public MatchDetailItem() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSeqId() {
        return seqId;
    }

    public void setSeqId(Long seqId) {
        this.seqId = seqId;
    }

    public Integer getMatchIndex() {
        return matchIndex;
    }

    public void setMatchIndex(Integer matchIndex) {
        this.matchIndex = matchIndex;
    }

    public MatchEvent getEvent() {
        return event;
    }

    public void setEvent(MatchEvent event) {
        this.event = event;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTakerOrderId() {
        return takerOrderId;
    }

    public void setTakerOrderId(String takerOrderId) {
        this.takerOrderId = takerOrderId;
    }

    public OrderType getTakerOrderType() {
        return takerOrderType;
    }

    public void setTakerOrderType(OrderType takerOrderType) {
        this.takerOrderType = takerOrderType;
    }

    public OrderState getTakerOrderState() {
        return takerOrderState;
    }

    public void setTakerOrderState(OrderState takerOrderState) {
        this.takerOrderState = takerOrderState;
    }

    public BigDecimal getTakerPrice() {
        return takerPrice;
    }

    public void setTakerPrice(BigDecimal takerPrice) {
        this.takerPrice = takerPrice;
    }

    public BigDecimal getTakerUnFillStock() {
        return takerUnFillStock;
    }

    public void setTakerUnFillStock(BigDecimal takerUnFillStock) {
        this.takerUnFillStock = takerUnFillStock;
    }

    public BigDecimal getTakerUnFillMoney() {
        return takerUnFillMoney;
    }

    public void setTakerUnFillMoney(BigDecimal takerUnFillMoney) {
        this.takerUnFillMoney = takerUnFillMoney;
    }

    public String getMakerOrderId() {
        return makerOrderId;
    }

    public void setMakerOrderId(String makerOrderId) {
        this.makerOrderId = makerOrderId;
    }

    public OrderType getMakerOrderType() {
        return makerOrderType;
    }

    public void setMakerOrderType(OrderType makerOrderType) {
        this.makerOrderType = makerOrderType;
    }

    public OrderState getMakerOrderState() {
        return makerOrderState;
    }

    public void setMakerOrderState(OrderState makerOrderState) {
        this.makerOrderState = makerOrderState;
    }

    public BigDecimal getMakerPrice() {
        return makerPrice;
    }

    public void setMakerPrice(BigDecimal makerPrice) {
        this.makerPrice = makerPrice;
    }

    public BigDecimal getFillStock() {
        return fillStock;
    }

    public void setFillStock(BigDecimal fillStock) {
        this.fillStock = fillStock;
    }

    public BigDecimal getFillMoney() {
        return fillMoney;
    }

    public void setFillMoney(BigDecimal fillMoney) {
        this.fillMoney = fillMoney;
    }

    public BigDecimal getRefundMoney() {
        return refundMoney;
    }

    public void setRefundMoney(BigDecimal refundMoney) {
        this.refundMoney = refundMoney;
    }

    public BigDecimal getRefundStock() {
        return refundStock;
    }

    public void setRefundStock(BigDecimal refundStock) {
        this.refundStock = refundStock;
    }

    public String getCancelOrderId() {
        return cancelOrderId;
    }

    public void setCancelOrderId(String cancelOrderId) {
        this.cancelOrderId = cancelOrderId;
    }

    public OrderType getCancelOrderType() {
        return cancelOrderType;
    }

    public void setCancelOrderType(OrderType cancelOrderType) {
        this.cancelOrderType = cancelOrderType;
    }

    public BigDecimal getCancelPrice() {
        return cancelPrice;
    }

    public void setCancelPrice(BigDecimal cancelPrice) {
        this.cancelPrice = cancelPrice;
    }

    public BigDecimal getCancelUnFillStock() {
        return cancelUnFillStock;
    }

    public void setCancelUnFillStock(BigDecimal cancelUnFillStock) {
        this.cancelUnFillStock = cancelUnFillStock;
    }

    public BigDecimal getCancelUnFillMoney() {
        return cancelUnFillMoney;
    }

    public void setCancelUnFillMoney(BigDecimal cancelUnFillMoney) {
        this.cancelUnFillMoney = cancelUnFillMoney;
    }

    public OrderState getCancelOrderState() {
        return cancelOrderState;
    }


    public void setCancelOrderState(OrderState cancelOrderState) {
        this.cancelOrderState = cancelOrderState;
    }


    @Override
    public String toString() {
        return "MatchDetailItem{" +
                "id=" + id +
                ", seqId=" + seqId +
                ", matchIndex=" + matchIndex +
                ", event=" + event +
                ", symbol='" + symbol + '\'' +
                ", quote='" + quote + '\'' +
                ", base='" + base + '\'' +
                ", timestamp=" + timestamp +
                ", takerOrderId='" + takerOrderId + '\'' +
                ", takerOrderType=" + takerOrderType +
                ", takerOrderState=" + takerOrderState +
                ", takerPrice=" + takerPrice +
                ", takerUnFillStock=" + takerUnFillStock +
                ", takerUnFillMoney=" + takerUnFillMoney +
                ", makerOrderId='" + makerOrderId + '\'' +
                ", makerOrderType=" + makerOrderType +
                ", makerOrderState=" + makerOrderState +
                ", makerPrice=" + makerPrice +
                ", fillStock=" + fillStock +
                ", fillMoney=" + fillMoney +
                ", refundMoney=" + refundMoney +
                ", refundStock=" + refundStock +
                ", cancelOrderId='" + cancelOrderId + '\'' +
                ", cancelOrderType=" + cancelOrderType +
                ", cancelPrice=" + cancelPrice +
                ", cancelUnFillStock=" + cancelUnFillStock +
                ", cancelUnFillMoney=" + cancelUnFillMoney +
                ", cancelOrderState=" + cancelOrderState +
                '}';
    }
}
