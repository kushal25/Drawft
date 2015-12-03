package com.drawft.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drawft.GroupDrawft;
import com.drawft.R;
import com.drawft.data.SingleDrawft;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CustomAdapter extends BaseAdapter {
    private Activity context;
    private final ArrayList<SingleDrawft> names = new ArrayList<SingleDrawft>();
    private static final String TAG = CustomAdapter.class.getSimpleName();
    String you = "";
    /*DisplayImageOptions options;*/
    // private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    String time = "now";
    public int offset = 7;
    public String id;
    private List<String> myColorList = GroupDrawft.getColorList();
    private JSONObject colorMapper = new JSONObject();
    ViewHolder holder = null;
    List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());


    static class ViewHolder {
        public TextView text;
        public SimpleDraweeView image;
        public TextView timeStamp;
        public Button statusBtn;
        public RelativeLayout relLayout;
    }

    public CustomAdapter(Activity context, String yourName, String id) {
        super();
        this.context = context;
        this.you = yourName;
        this.id = id;

        /*options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_launcher)
                .showImageOnFail(R.drawable.ic_launcher)
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .imageScaleType(ImageScaleType.NONE)
                .displayer(new RoundedBitmapDisplayer(20))
                .build();*/

        // ImageLoader imageLoader=new ImageLoader(context.getApplicationContext());

    }

    public void addItem(SingleDrawft sD) {
        this.names.add(sD);
        // int i = this.names.size();
        //if (i > 3)
        notifyDataSetChanged();
    }


    public void addItemAtFirst(ArrayList<SingleDrawft> sD) {
        for (int i = 0; i < sD.size(); i++) {
            this.names.add(0, sD.get(i));
        }
        notifyDataSetChanged();
    }

    /*public void addItemAtStart(SingleDrawft sD) {
        this.names.add(sD);
        //int i = this.names.size();
        //if (i > 3)
        notifyDataSetChanged();
    }*/
    public boolean exists(String url) {
        return !displayedImages.contains(url);
    }


    @Override
    public int getCount() {
        return this.names.size();
    }

    @Override
    public SingleDrawft getItem(int pos) {
        return this.names.get(pos);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getTilePositionById(String id) {
        int position = -1;
        for (int i = 0; i < this.names.size(); i++) {
            SingleDrawft tile = this.names.get(i);
            if (tile.getId() != null && tile.getId().equals(id)) {
                position = i;
                break;
            }
        }
        return position;

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_sigle_item, null);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) rowView.findViewById(R.id.label);
            viewHolder.text.setTypeface(GroupDrawft.robotoLight);
            viewHolder.image = (SimpleDraweeView) rowView.findViewById(R.id.imageView);
            viewHolder.timeStamp = (TextView) rowView.findViewById(R.id.timeStamp);
            viewHolder.statusBtn = (Button) rowView.findViewById(R.id.drawft_status);
            viewHolder.timeStamp.setTypeface(GroupDrawft.robotoLight);
            viewHolder.statusBtn.setTypeface(GroupDrawft.fontFeather);
            viewHolder.relLayout = (RelativeLayout) rowView.findViewById(R.id.content_wrapper);
            rowView.setTag(viewHolder);
        }
        // fill data
        try {
            holder = (ViewHolder) rowView.getTag();


            SingleDrawft singleDrawft = this.names.get(position);

            int w = singleDrawft.getDrawftW();
            int h = singleDrawft.getdrawftH();
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //RelativeLayout.LayoutParams layoutParams_time = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            RelativeLayout.LayoutParams layoutWrapper = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        /*RelativeLayout.LayoutParams tParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if ((position % 2) != 0) {
            holder.timeStamp.setPadding(0, 0, 0, 100);
            holder.timeStamp.setHeight(holder.timeStamp.getHeight() + 100);
            //tParams.setMargins(0, 0, 0, 100);
        } else {
            tParams.setMargins(0, 0, 0, 0);
            holder.timeStamp.setPadding(0, 0, 0, 0);
            holder.timeStamp.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }*/
            //holder.timeStamp.setLayoutParams(tParams);
            if (singleDrawft.getIsSent() == 1) {
                holder.statusBtn.setVisibility(View.GONE);
                holder.statusBtn.clearAnimation();
            } else if (singleDrawft.getIsSent() == 2) {
                RotateAnimation rotateAnimation = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setDuration(1600);
                rotateAnimation.setRepeatCount(RotateAnimation.INFINITE);
                holder.statusBtn.setAnimation(rotateAnimation);
                rotateAnimation.start();
            } else {
                holder.statusBtn.setVisibility(View.VISIBLE);
                holder.statusBtn.clearAnimation();
            }
            if (!singleDrawft.getSentAt().equals("now") && !singleDrawft.getSentAt().equals("new")) {
                Calendar cl = Calendar.getInstance();
                cl.setTimeInMillis(Long.parseLong(singleDrawft.getSentAt()));
                DateFormat df = new SimpleDateFormat("dd MMM HH:mm");
                time = df.format(cl.getTime());
            }

            holder.timeStamp.setVisibility(View.VISIBLE);
            holder.text.setTextSize(14);
            try {

                if (colorMapper.isNull(singleDrawft.getDrawftText())) {
                    colorMapper.put(singleDrawft.getDrawftText(), Color.parseColor(myColorList.get(position % 24)));
                }
                holder.text.setTextColor((int) colorMapper.get(singleDrawft.getDrawftText()));

            } catch (JSONException e) {
                holder.text.setTextColor(Color.parseColor("#5a6a7a"));
            }
            holder.text.setTextSize(16);
            if (singleDrawft.getBitmapUrl() == null) {
                holder.text.setTextColor(Color.parseColor("#5a6a7a"));
                holder.image.getLayoutParams().width = 0;
                holder.image.getLayoutParams().height = 0;
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                holder.text.setText(singleDrawft.getDrawftText());
                String[] sender = singleDrawft.getDrawftText().split(" ");
                if (singleDrawft.getSentAt().equals("new")) {
                    time = "";
                    layoutWrapper.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    holder.relLayout.setBackgroundResource(0);
                    holder.timeStamp.setVisibility(View.GONE);
                    holder.text.setTextSize(22);
                    holder.text.setTextColor(Color.parseColor("#bbbbbb"));
                    holder.text.setText(R.string.new_drawfts_icon);
                    holder.text.setTypeface(GroupDrawft.fontFeather);
                } else {
                    if (sender[0].toString().equals("You")) {
                        layoutWrapper.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        holder.relLayout.setBackgroundResource(R.drawable.drawft_left_radius);
                    } else {
                        holder.relLayout.setBackgroundResource(R.drawable.drawft_border);
                        layoutWrapper.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    }
                    holder.timeStamp.setText(time);
                }


            } else {

                int newW = w - Math.round(60 * w / 100);
                int newH = h - Math.round(60 * h / 100);
                holder.image.getLayoutParams().width = newW;
                holder.image.getLayoutParams().height = newH;
                layoutWrapper.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                holder.text.setText(singleDrawft.getDrawftText());

            /*RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(newW, newH);
            imageParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);*/
                //imageParams.setMargins(10, 10, 10, 10);
                //imageParams.addRule(RelativeLayout.BELOW, R.id.label);

            /*layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.imageView);*/
                // layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                layoutParams.setMargins(0, 0, -100, 0);
                holder.timeStamp.setText(time);
                holder.relLayout.setBackgroundResource(R.drawable.drawft_border);
            /*layoutParams_time.addRule(RelativeLayout.RIGHT_OF, R.id.imageView);
            layoutParams_time.addRule(RelativeLayout.BELOW, R.id.label);*/
                if (this.you.equals(singleDrawft.getDrawftText())) {
                    layoutWrapper.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                    layoutWrapper.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    holder.relLayout.setBackgroundResource(R.drawable.drawft_left_radius);

                    holder.text.setText("You");
                    holder.timeStamp.setText(time);
                    //layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    // layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                /*imageParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);*/

               /* layoutParams.removeRule(RelativeLayout.RIGHT_OF);
                layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.imageView);*/

                /*layoutParams_time.removeRule(RelativeLayout.RIGHT_OF);
                layoutParams_time.addRule(RelativeLayout.LEFT_OF, R.id.imageView);*/


                }

                //holder.image.setLayoutParams(imageParams);
           /* Log.d(TAG, "On GetView URL = " + singleDrawft.getBitmapUrl());
            Log.d(TAG, "Width = " + newW + "-" + w + " Height = " + newH + "-" + h);*/
            }
            holder.relLayout.setLayoutParams(layoutWrapper);
            holder.text.setLayoutParams(layoutParams);
       /* holder.timeStamp.setLayoutParams(layoutParams_time);*/
            //holder.image.setLayoutParams(layoutParams_time);
            if (singleDrawft.getBitmapUrl() != null) {
                holder.image.setImageURI(Uri.parse(singleDrawft.getBitmapUrl()));

            }
            //ImageLoader.getInstance().displayImage(singleDrawft.getBitmapUrl(), holder.image, animateFirstListener);
        } catch (Exception e) {

        }

        return rowView;
    }


    /*private class AnimateFirstDisplayListener extends SimpleImageLoadingListener {


        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;

                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    *//*Animation zoomin = AnimationUtils.loadAnimation(context, R.anim.zoomin);
                    imageView.setAnimation(zoomin);*//*
                    //FadeInBitmapDisplayer.animate(imageView, 2000);

                    displayedImages.add(imageUri);
                }
            }
        }
    }
*/
    public void destroy() {
        this.context = null;
        this.holder.image = null;
        this.holder = null;
    }


} 