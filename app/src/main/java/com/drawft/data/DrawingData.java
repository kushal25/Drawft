package com.drawft.data;

import java.util.ArrayList;

public class DrawingData implements java.io.Serializable {

    private ArrayList<DrawftInfo> myPaintData = new ArrayList<DrawftInfo>();

    public void setMyPaintData(ArrayList<DrawftInfo> paramLinkedList) {
        this.myPaintData = paramLinkedList;
    }

    public ArrayList<DrawftInfo> getMyPaintData() {
        return this.myPaintData;
    }

}