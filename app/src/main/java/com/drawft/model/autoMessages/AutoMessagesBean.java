package com.drawft.model.autoMessages;

public class AutoMessagesBean {
    int uid;
    String gid;
    String type;
    String content;

    public void setGroupId(String gid) {
        this.gid = gid;
    }

    public String getGroupId() {
        return this.gid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getUid() {
        return this.uid;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

}
