package com.drawft.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


public class DrawftInfo implements Serializable {
    private ArrayList<HashMap> drawftInfo = new ArrayList<HashMap>();

    public void setCurrentDraft(ArrayList<HashMap> paramLinkedList) {
        this.drawftInfo = paramLinkedList;
    }

    public ArrayList<HashMap> getCurrentDrawft() {
        return this.drawftInfo;
    }

    public void addNewItem(ArrayList list, int color) {
        HashMap pathList = new HashMap();
        pathList.put("path_info", color);
        pathList.put("path", list);
        this.drawftInfo.add(pathList);
    }

    public void clear() {
        this.drawftInfo.clear();
    }
}
