package com.drawft.model.drawfts;


public class DrawftBean {
    private long id;
    private String sentBy;
    private String fileName;
    private String sentAt;
    private String dimensions;
    private int autoMessageId;
    private int isSent;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public String getSentAt() {
        return sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String sentAt) {
        this.fileName = sentAt;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dim) {
        this.dimensions = dim;
    }

    public int getAutoMessageId() {
        return autoMessageId;
    }

    public void setAutoMessageId(int autoMessageId) {
        this.autoMessageId = autoMessageId;
    }

    public int getIsSent() {
        return isSent;
    }

    public void setIsSent(int isSent) {
        this.isSent = isSent;
    }
}
