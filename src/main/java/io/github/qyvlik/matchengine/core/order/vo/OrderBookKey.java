package io.github.qyvlik.matchengine.core.order.vo;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderBookKey implements Serializable {
    private BigDecimal price;
    private Long seqId;

    public OrderBookKey() {

    }

    public OrderBookKey(BigDecimal price, Long sequenceId) {
        this.price = price;
        this.seqId = sequenceId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getSeqId() {
        return seqId;
    }

    public void setSeqId(Long seqId) {
        this.seqId = seqId;
    }
}
