package io.github.qyvlik.matchengine.core.matcher.vo;

import java.io.Serializable;
import java.util.List;

public class ExecuteResult implements Serializable {
    private String symbol;
    private List<OrderSnapshot> orderSnapshots;         // order which change
    private List<MatchDetailItem> items;                // match item

    public ExecuteResult() {

    }

    public ExecuteResult(String symbol, List<OrderSnapshot> orderSnapshots, List<MatchDetailItem> items) {
        this.symbol = symbol;
        this.orderSnapshots = orderSnapshots;
        this.items = items;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<OrderSnapshot> getOrderSnapshots() {
        return orderSnapshots;
    }

    public void setOrderSnapshots(List<OrderSnapshot> orderSnapshots) {
        this.orderSnapshots = orderSnapshots;
    }

    public List<MatchDetailItem> getItems() {
        return items;
    }

    public void setItems(List<MatchDetailItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "ExecuteResult{" +
                "symbol='" + symbol + '\'' +
                ", orderSnapshots=" + orderSnapshots +
                ", items=" + items +
                '}';
    }
}
