package com.drawft.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.content.Context;

import android.widget.Toast;

import com.drawft.data.DrawingData;
import com.drawft.data.DrawftInfo;


public class FileUtil {
    private static final String ROOT_FOLDER = "Group Drawft";
    //private static final String PATH_SEPERATOR = "/";
    Context context;
    CharSequence text;

    public FileUtil(Context cx) {
        this.context = cx;
        /*File f = Environment.getDataDirectory();
        text = "Saved on file " + f.getPath();
        Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show();*/
    }

    public static String getRootFolder() {
        return FileUtil.ROOT_FOLDER;
    }

    public void saveDataToBitmapFIle(String fName, Bitmap bmp) {
        try {

            String[] strings = fName.split("/");
            String groupId  = strings[0];
            String fileName = strings[1];
            FileOutputStream out = null;
            try {
                File f = new File(context.getFilesDir(),groupId);
                if(!f.exists())
                {
                    f.mkdirs();
                }
                File newf = new File(f,fileName);
                out = new FileOutputStream(newf);
                //out = this.context.openFileOutput(newf.getAbsolutePath(), 2);
                bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            /*text = "Saved on file ";
            Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show();*/
        } catch (Exception e) {
            text = "Error";
            Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show();
        }

    }

    public void saveCurrentDrawftToFIle(DrawftInfo drawingData, String fileName) {
        try {

            /*FileOutputStream out = null;
            try {
            File file1 = new File(this.context.getFilesDir(), fileName);
                out = new FileOutputStream("Drawn_image.png");
                bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/


            File file1 = new File(this.context.getFilesDir(), fileName);
            FileOutputStream fos = this.context.openFileOutput(fileName, 2);
            //FileOutputStream fos = new FileOutputStream(file1);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(drawingData);
            fos.close();
            oos.close();
            /*text = "Saved on file " + file1.getPath();
            Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show();*/
        } catch (IOException ioe) {
            text = "IOException";
            Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            text = "Error";
            Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show();
        }

    }

    public void saveDataToFIle(DrawingData drawingData, String fileName) {
        try {

            /*FileOutputStream out = null;
            try {
            File file1 = new File(this.context.getFilesDir(), fileName);
                out = new FileOutputStream("Drawn_image.png");
                bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/


            File file1 = new File(this.context.getFilesDir(), fileName);
            FileOutputStream fos = this.context.openFileOutput(fileName, 2);
            //FileOutputStream fos = new FileOutputStream(file1);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(drawingData);
            fos.close();
            oos.close();
            text = "Saved on file " + file1.getPath();
            Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show();
        } catch (IOException ioe) {
            text = "IOException";
            Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            text = "Error";
            Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show();
        }

    }


    public ArrayList<HashMap> getCurrentDrawftFromFile(String fName) {

        try {
           /* String f = this.context.getFilesDir().getAbsolutePath();
            File file1 = new File(this.context.getFilesDir(), "Drawn_image.png");

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(file1.getAbsolutePath(), options);
            return bitmap;*/


            File file1 = new File(this.context.getFilesDir(), fName);
            /*text = "Length::" + file1.length();
            Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show();*/
            FileInputStream fis = this.context.openFileInput(fName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            DrawftInfo drawingData = (DrawftInfo) ois.readObject();
            return drawingData.getCurrentDrawft();

        } catch (IOException ioe) {
            return null;
        } catch (Exception e) {

        }
        return null;
    }

    public ArrayList<DrawftInfo> getDataFromFile(String fName) {

        try {
           /* String f = this.context.getFilesDir().getAbsolutePath();
            File file1 = new File(this.context.getFilesDir(), "Drawn_image.png");

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(file1.getAbsolutePath(), options);
            return bitmap;*/


            File file1 = new File(this.context.getFilesDir(), fName);
            text = "Length::" + file1.length();
            Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show();
            FileInputStream fis = this.context.openFileInput(fName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            DrawingData drawingData = (DrawingData) ois.readObject();
            return drawingData.getMyPaintData();

        } catch (IOException ioe) {
            return null;
        } catch (Exception e) {

        }
        return null;
    }

    public Bitmap getBitmapFile(String fName) {

        try {
            File file1 = new File(this.context.getFilesDir(), fName);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(file1.getAbsolutePath(), options);
            return bitmap;

        } catch (Exception e) {

        }
        return null;
    }

    public String getFilePath(String fName) {
        File file1 = new File(this.context.getFilesDir(), fName);
        return "file://" + file1.getAbsolutePath();
    }

    public String getFilePath1(String fName){
        if(fName.contains("/")) {
            String[] strings = fName.split("/");
            String groupId = strings[0];
            String fileName = strings[1];

            File file = new File(context.getFilesDir(), groupId);
            if (!file.exists()) {
                file.mkdirs();
            }

            File file1 = new File(file, fileName);
            return "file://" + file1.getAbsolutePath();
        }
        else
        {
            File file = new File(context.getFilesDir(), fName);
            return "file://" + file.getAbsolutePath();
        }
    }



    public void DeleteRecursive(String filename) {
        File file = new File(context.getFilesDir(),filename);
        if (!file.exists())
            return;
        if (!file.isDirectory()) {
            file.delete();
            return;
        }

        String[] files = file.list();
        for (int i = 0; i < files.length; i++) {

            DeleteRecursive(filename + "/" + files[i]);
        }
        file.delete();
    }

    public static String getAbsPath(Context cx, String fName) {
        File file1 = new File(cx.getFilesDir(), fName);

        return "file://" + file1.getAbsolutePath();
    }

    public void getDirSize() {
        long size = dirSize(this.context.getFilesDir());
        text = size + "";
        Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show();
    }

    public long dirSize(File dir) {

        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // Recursive call if it's a directory
                if (fileList[i].isDirectory()) {
                    result += dirSize(fileList[i]);
                } else {
                    // Sum the file size in bytes
                    result += fileList[i].length();
                }
            }
            return result; // return the file size
        }
        return 0;
    }
}
