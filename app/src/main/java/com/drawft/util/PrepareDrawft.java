package com.drawft.util;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.drawft.data.SingleDrawft;
import com.drawft.model.drawfts.DrawftModel;
import com.drawft.model.groups.GroupModel;
import com.drawft.model.members.GroupMemberModel;
import com.drawft.view.PaintView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PrepareDrawft {
    Context context;
    public static final String imgType = ".png";
    private static final int minBitmapW = 200, minBitmapH = 200;
    protected PaintView __drawingPaintView = null;
    protected FileUtil __fileUtil = null;

    public PrepareDrawft(Context cx) {
        this.context = cx;
        this.__fileUtil = new FileUtil(this.context);
        this.__drawingPaintView = new PaintView(this.context);
        this.__drawingPaintView.initialize(1080, 1920);
    }

    public String onReceive(String groupId, List list, String fName, String by, String time, String res) {
        try {
            DrawftModel _db = DrawftModel.getR(this.context);
            if (_db.checkDrawftExistence(groupId, fName + PrepareDrawft.imgType)) {
                this.__drawingPaintView.empty();
                DrawftModel.closeDBConnection(_db);
                return "";
            }
            String[] resolution = res.split("X");
            this.__drawingPaintView.setImageDimensions(Integer.parseInt(resolution[1]), Integer.parseInt(resolution[0]));
            ArrayList<HashMap> localPath = (ArrayList<HashMap>) list;
            for (HashMap a : localPath) {
                this.__drawingPaintView.drawPath(a);
            }
            this.__drawingPaintView.invalidate();
            DrawftModel.closeDBConnection(_db);
            return this.prepareBitmap(groupId, by, fName, time);
        } catch (Exception e) {
            return "";
        }
    }

    private String prepareBitmap(String groupId, String by, String fName, String time) {
        try {
            String fileName = fName + PrepareDrawft.imgType;
            String dim = this.__drawingPaintView.getDimensionsString();
            int[] val = getBitmapWidthAndHeight(dim);
            int[] offset = this.__drawingPaintView.getIntersectionPoint();
            int w = this.__drawingPaintView.mBitmap.getWidth();
            int h = this.__drawingPaintView.mBitmap.getHeight();
            Bitmap b = this.formBitmap(offset, val, w, h, this.__drawingPaintView.mBitmap);
            if (b != null) {
                this.__fileUtil.saveDataToBitmapFIle(groupId + "/" + fileName, b);
                DrawftModel _db = DrawftModel.getRW(this.context);
                _db.addNewRecord(groupId, by, fileName, time, dim, 0, 1);
                DrawftModel.closeDBConnection(_db);
            }
            this.__drawingPaintView.empty();
            return dim;
        } catch (Exception e) {
            return "";
        }

    }

    public SingleDrawft prepareSingleDrawft(int isSent, String fName, String dim, String by, String sentAt) {

        SingleDrawft singleDrawft = new SingleDrawft();
        singleDrawft.setIsSent(isSent);
        if (fName == null && dim == null) {
            singleDrawft.setDrawftText(by);
            singleDrawft.setSentAt(sentAt);
        } else {
            // Bitmap bmp = this.__fileUtil.getBitmapFile(fName);
            singleDrawft.setId(fName);
            singleDrawft.setDrawftText(by);
            singleDrawft.setDrawftImage(null);
            singleDrawft.setBitmapUrl(this.__fileUtil.getFilePath1(fName));
            singleDrawft.setSentAt(sentAt);
            //Log.d("getBitmapUrl", singleDrawft.getBitmapUrl());

            try {
                String[] size = dim.split("-");
                float w = Float.parseFloat(size[0]);
                float h = Float.parseFloat(size[1]);
                singleDrawft.setW(Math.round(w));
                singleDrawft.setH(Math.round(h));

            } catch (Exception e) {
                singleDrawft.setW(0);
                singleDrawft.setH(0);
            }
        }
        return singleDrawft;
    }

    public int[] getBitmapWidthAndHeight(String dim) {
        String[] size = dim.split("-");
        int w = Math.round(Float.parseFloat(size[0]));
        int h = Math.round(Float.parseFloat(size[1]));
        int[] dims = {w, h};
        return dims;
    }

    public Bitmap formBitmap(int[] offset, int[] val, int w, int h, Bitmap originalBitmap) {
        if ((offset[0] - 10) < 0) {
            offset[0] = 0;
        } else {
            offset[0] = offset[0] - 10;
        }
        if ((offset[1] - 10) < 0) {
            offset[1] = 0;
        } else {
            offset[1] = offset[1] - 10;
        }
        int imgW = (val[0] < PrepareDrawft.minBitmapW) ? val[0] + 25 : (val[0] + 25 > w) ? w : val[0] + 25;
        int imgH = (val[1] < PrepareDrawft.minBitmapH) ? val[1] + 25 : (val[1] + 25 > h) ? h : val[1] + 25;

        if ((offset[0] + imgW) > w) {
            imgW = (w - offset[0]);
        }
        if ((offset[1] + imgH) > h) {
            imgH = (h - offset[1]);
        }
        Bitmap b = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        try {
            b = Bitmap.createBitmap(originalBitmap, offset[0], offset[1], imgW, imgH);
        } catch (OutOfMemoryError e) {

        }
        return b;
    }

    public static void onDeleteGroup(Context ctx, String groupId) {
        GroupModel db = GroupModel.getRW(ctx);
        DrawftModel dm = DrawftModel.getRW(ctx);
        GroupMemberModel gmm = GroupMemberModel.getRW(ctx);
        db.delGroup(groupId);
        dm.delDrawfts(groupId);
        gmm.delGroupMembers(groupId);
        GroupModel.closeDBConnection(db);
        DrawftModel.closeDBConnection(dm);
        GroupMemberModel.closeDBConnection(gmm);
        FileUtil fu = new FileUtil(ctx);
        fu.DeleteRecursive(groupId);
        /*ImageLoader.getInstance().clearDiskCache();
        ImageLoader.getInstance().clearMemoryCache();*/
    }

    public void onDestroy() {
        this.context = null;
        this.__drawingPaintView = null;
        this.__fileUtil = null;
    }
}
