package com.drawft.data;


import java.util.ArrayList;

public class ContactTile implements Comparable<ContactTile> {
    String groupName;
    String mobileNumber;
    String groupId;
    int notifications;
    long notificationTime = 0;
    int appUsing;
    int blocked;
    ArrayList<String> dimensions = new ArrayList<>();
    ArrayList<String> coverPic1 = new ArrayList<>();
    boolean animateDrawfts = false;
    int memberCount = 2;

    boolean isGroup = false;

    public void setGroupName(String uName) {
        this.groupName = uName;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setMobileNumber(String mno) {
        this.mobileNumber = mno;
    }

    public String getMobileNumber() {
        return this.mobileNumber;
    }

    public void setGroupId(String id) {
        this.groupId = id;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setIsGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    public boolean getIsGroup() {
        return this.isGroup;
    }

    public void setCoverPic1(ArrayList<String> pic1) {
        this.coverPic1 = pic1;
    }

    public ArrayList<String> getCoverPic1() {
        return this.coverPic1;
    }

    public void setDimensions(ArrayList<String> dims) {
        this.dimensions = dims;
    }

    public ArrayList<String> getDimensions() {
        return this.dimensions;
    }

    public void setNotifications(int notifications) {
        this.notifications = notifications;
    }

    public int getNotifications() {
        return this.notifications;
    }

    public void setAppUsing(int appUsing) {
        this.appUsing = appUsing;
    }

    public int getAppUsing() {
        return this.appUsing;
    }

    public void setBlocked(int blocked) {
        this.blocked = blocked;
    }

    public int getBlocked() {
        return this.blocked;
    }

    public void setAnimateDrawfts(boolean anim) {
        this.animateDrawfts = anim;
    }

    public boolean getAnimateDrawfts() {
        return this.animateDrawfts;
    }

    public void setNotificationTime(long time) {
        this.notificationTime = time;
    }

    public long getNotificationTime() {
        return this.notificationTime;
    }
    public void setMemberCount(int c) {
        this.memberCount = c;
    }

    public int getMemberCount() {
        return this.memberCount;
    }

    public int compareTo(ContactTile comparestu) {
        long compareTime = (comparestu).getNotificationTime();
        if (compareTime > this.getNotificationTime()) {
            return 1;
        } else {
            return -1;
        }
    }
}
