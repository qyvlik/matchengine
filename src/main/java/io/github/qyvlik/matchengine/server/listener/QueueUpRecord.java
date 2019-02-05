package io.github.qyvlik.matchengine.server.listener;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class QueueUpRecord implements Serializable {
    @JSONField(name = "s")
    private String scope;

    @JSONField(name = "k")
    private String key;

    @JSONField(name = "ki")
    private Long index;

    @JSONField(name = "d")
    private Object data;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "QueueUpRecord{" +
                "scope='" + scope + '\'' +
                ", key='" + key + '\'' +
                ", index=" + index +
                ", data=" + data +
                '}';
    }
}
