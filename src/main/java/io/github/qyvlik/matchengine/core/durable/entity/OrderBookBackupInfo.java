package io.github.qyvlik.matchengine.core.durable.entity;

import java.io.Serializable;

public class OrderBookBackupInfo implements Serializable {
    private Long backupId;
    private Long prevBackupId;
    private Long backupStartSeqId;
    private Long backupEndSeqId;

    public OrderBookBackupInfo(Long backupId, Long prevBackupId, Long backupStartSeqId, Long backupEndSeqId) {
        this.backupId = backupId;
        this.prevBackupId = prevBackupId;
        this.backupStartSeqId = backupStartSeqId;
        this.backupEndSeqId = backupEndSeqId;
    }

    public OrderBookBackupInfo() {

    }

    public Long getBackupId() {
        return backupId;
    }

    public void setBackupId(Long backupId) {
        this.backupId = backupId;
    }

    public Long getPrevBackupId() {
        return prevBackupId;
    }

    public void setPrevBackupId(Long prevBackupId) {
        this.prevBackupId = prevBackupId;
    }

    public Long getBackupStartSeqId() {
        return backupStartSeqId;
    }

    public void setBackupStartSeqId(Long backupStartSeqId) {
        this.backupStartSeqId = backupStartSeqId;
    }

    public Long getBackupEndSeqId() {
        return backupEndSeqId;
    }

    public void setBackupEndSeqId(Long backupEndSeqId) {
        this.backupEndSeqId = backupEndSeqId;
    }

    @Override
    public String toString() {
        return "OrderBookBackupInfo{" +
                "backupId=" + backupId +
                ", prevBackupId=" + prevBackupId +
                ", backupStartSeqId=" + backupStartSeqId +
                ", backupEndSeqId=" + backupEndSeqId +
                '}';
    }
}
