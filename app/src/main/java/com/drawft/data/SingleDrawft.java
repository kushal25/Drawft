package com.drawft.data;

import android.graphics.Bitmap;


public class SingleDrawft {
    Bitmap bmp;
    String text;
    int drawftW = 50, drawftH = 50;
    String bitmapUrl;
    String sentBy;
    String id;
    String sentAt;
    private int isSent = 1;

    public void setDrawftImage(Bitmap bmp) {
        this.bmp = bmp;
    }

    public void setDrawftText(String text) {
        this.text = text;
    }

    public void setW(int w) {
        if (w > this.drawftW)
            this.drawftW = w;
    }

    public void setH(int h) {
        if (h > this.drawftH)
            this.drawftH = h;
    }

    public String getSentAt() {
        return sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }

    public Bitmap getDrawftBitmap() {
        return this.bmp;
    }

    public String getDrawftText() {
        return this.text;
    }

    public int getDrawftW() {
        return this.drawftW;
    }

    public int getdrawftH() {
        return this.drawftH;
    }

    public String getBitmapUrl() {
        return bitmapUrl;
    }

    public void setBitmapUrl(String url) {
        this.bitmapUrl = url;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String by) {
        this.sentBy = by;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIsSent() {
        return isSent;
    }

    public void setIsSent(int isSent) {
        this.isSent = isSent;
    }
}
