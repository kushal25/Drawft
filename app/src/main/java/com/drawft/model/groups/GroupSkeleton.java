package com.drawft.model.groups;


import java.util.HashMap;
import java.util.Map;

public class GroupSkeleton {

    private String groupName;
    private String coverPic;
    private String createdBy;
    private String createdAt;
    private String groupId;
    private int fav;
    private int isappusing;
    private int isNotification;
    private long notificationTime = 0;
    private long blockedAt = 0;
    private int isBlocked;
    private int msgCount;
    private int viewedCount;
    private Map<String, Integer> members = new HashMap<String, Integer>();
    private String lastSynced;

    public GroupSkeleton(String gName, String cover, String owner, String groupId, int fav, int isNotification, int isappusing, int isBlocked, Map<String, Integer> members, String createdAt) {
        this.groupName = gName;
        this.createdBy = owner;
        this.groupId = groupId;
        this.fav = fav;
        this.isNotification = isNotification;
        this.isappusing = isappusing;
        this.isBlocked = isBlocked;
        this.createdAt = createdAt;
        this.msgCount = 0;
        this.viewedCount = 0;
        this.members = members;
        this.coverPic = cover;
        this.lastSynced = "";
    }

    public String getGroupName() {
        return groupName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public int getMsgCount() {
        return this.msgCount;
    }

    public int getViewedCount() {
        return this.viewedCount;
    }

    public Map<String, Integer> getMembers() {
        return this.members;
    }

    public void setMsgCount(int c) {
        this.msgCount = c;
    }

    public String getCoverPic() {
        return this.coverPic;
    }

    /*public void setMsgCount(int c) {
        this.msgCount = c;
    }*/

    public void setFav(int fav) {
        this.fav = fav;
    }

    public int getFav() {
        return this.fav;
    }

    public int getIsAppUsing() {
        return this.isappusing;
    }

    public void setIsAppUsing(int isappusing) {
        this.isappusing = isappusing;
    }

    public int getIsNotification() {
        return this.isNotification;
    }

    public void setIsNotification(int isNotification) {
        this.isNotification = isNotification;
    }

    public int getIsBlocked() {
        return this.isBlocked;
    }

    public void setIsBlocked(int isBlocked) {
        this.isBlocked = isBlocked;
    }

    public String getLastSynced() {
        return this.lastSynced;
    }

    public void setLastSynced(String lastSynced) {
        this.lastSynced = lastSynced;
    }

    public void setNotificationTime(long time) {
        this.notificationTime = time;
    }

    public long getNotificationTime() {
        return this.notificationTime;
    }

    public void setBlockedAt(long time) {
        this.blockedAt = time;
    }

    public long getBlockedAt() {
        return this.blockedAt;
    }
}