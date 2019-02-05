package io.github.qyvlik.matchengine.core.order.vo;

public enum OrderSide {
    buy {
        public OrderSide reverse() {
            return sell;
        }
    },
    sell {
        public OrderSide reverse() {
            return buy;
        }
    };

    public abstract OrderSide reverse();

}
