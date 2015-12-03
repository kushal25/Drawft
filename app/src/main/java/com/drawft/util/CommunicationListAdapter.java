package com.drawft.util;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drawft.GroupDrawft;
import com.drawft.R;
import com.drawft.data.ContactTile;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

public class CommunicationListAdapter extends BaseAdapter {
    private Activity context;
    private ArrayList<ContactTile> tiles = new ArrayList<>();
    private ArrayList<String> dimens = new ArrayList<>();
    ViewHolder holder;
    ContactTile contactTile;
    private static final String TAG = CommunicationListAdapter.class.getSimpleName();
    /* DisplayImageOptions options;*/
    String newDim;
    public ArrayList<String> myColorList;

    static class ViewHolder {
        public TextView tileLabel;
        public TextView tileNumber;
        public SimpleDraweeView tileImage1;
        public SimpleDraweeView tileImage2;
        public SimpleDraweeView tileImage3;
        public Button fav;
        public Button appGroupUser;
        public Button blocked;
    }

    public CommunicationListAdapter(Activity context, ArrayList<ContactTile> initialTiles ) {
        super();
        this.context = context;
        this.tiles = initialTiles;
        /*cm = new ContactModel(context);
        gm = new GroupModel(context);*/
        /*options = new DisplayImageOptions.Builder()
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .imageScaleType(ImageScaleType.NONE)
                .build();*/
    }

    public CommunicationListAdapter(Activity context) {
        super();
        this.context = context;
        /*cm = new ContactModel(context);
        gm = new GroupModel(context);*/
        /*options = new DisplayImageOptions.Builder()
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .imageScaleType(ImageScaleType.NONE)
                .build();*/
    }

    public void addItem(ContactTile tile) {
        this.tiles.add(tile);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        this.tiles.remove(position);
        notifyDataSetChanged();
    }

    public void concatList(ArrayList<ContactTile> newTiles) {
        this.tiles = newTiles;
        notifyDataSetChanged();
    }

    public void addItemAtFirst(ContactTile tile) {
        this.tiles.add(0, tile);
        notifyDataSetChanged();
    }

    public void getColors(){
        myColorList = GroupDrawft.getColorList();
        notifyDataSetChanged();

    }


    public int getTilePositionById(String id) {
        int position = -1;
        for (int i = 0; i < this.tiles.size(); i++) {
            ContactTile tile = this.tiles.get(i);
            if (tile.getGroupId().equals(id)) {
                position = i;
                break;
            }
        }
        return position;

    }

    public String updateGetDimensions() {
        StringBuilder sb = new StringBuilder(3);
        int newH, newW;
        for (int i = 0; i < dimens.size(); i++) {
            String[] size = dimens.get(i).split("-");
            int w = Math.round(Float.parseFloat(size[0]));
            int h = Math.round(Float.parseFloat(size[1]));
            /*if (w < 80 && h < 80) {
                newW = w - Math.round(60 * w / 100);
                newH = h - Math.round(60 * h / 100);
            } else {
                newW = w - Math.round(85 * w / 100);
                newH = h - Math.round(85 * h / 100);
            }*/
            newW = w - Math.round(85 * w / 100);
            newH = h - Math.round(85 * h / 100);

            if (newH > 150) {
                newW = w - Math.round(90 * w / 100);
                newH = h - Math.round(90 * h / 100);
            }

            /*if (w < 100) {
                newW = w;
            } else {
                newW = w - Math.round(85 * w / 100);
            }
            if (h < 100) {
                newH = h;
            } else {
                newH = h - Math.round(85 * h / 100);
            }*/
            newDim = newW + "-" + newH;
            sb.append(newDim + ",");
        }
        return sb.toString();
    }

    @Override
    public int getCount() {
        return this.tiles.size();
    }

    @Override
    public ContactTile getItem(int pos) {
        return this.tiles.get(pos);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        try {

            contactTile = this.tiles.get(position);
            // reuse views
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.contact_tile, null);

                holder = new ViewHolder();
                holder.tileLabel = (TextView) rowView.findViewById(R.id.tileLabel);
                holder.tileLabel.setTypeface(GroupDrawft.robotoBold);
                holder.tileNumber = (TextView) rowView.findViewById(R.id.tileNumber);
                holder.tileNumber.setTypeface(GroupDrawft.robotoBold);
                holder.tileImage1 = (SimpleDraweeView) rowView.findViewById(R.id.tileImage1);
                holder.tileImage2 = (SimpleDraweeView) rowView.findViewById(R.id.tileImage2);
                holder.tileImage3 = (SimpleDraweeView) rowView.findViewById(R.id.tileImage3);
                holder.fav = (Button) rowView.findViewById(R.id.favorite);
                holder.appGroupUser = (Button) rowView.findViewById(R.id.appGroupUser);
                holder.fav.setTypeface(GroupDrawft.fontFeather);
                holder.appGroupUser.setTypeface(GroupDrawft.fontFeather);
                holder.blocked = (Button) rowView.findViewById(R.id.blockUser);
                holder.blocked.setTypeface(GroupDrawft.fontFeather);
                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }


        /*GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]
                {Color.parseColor(myColorList.get(position % 24)), Color.parseColor(myColorList.get(position % 24))});
        gd.setStroke(5, Color.parseColor(myColorList.get(position % 24)));
        rowView.setBackgroundDrawable(gd);*/
            // String colo = myColorList.get(position % myColorList.size());
            rowView.setBackgroundColor(Color.parseColor(myColorList.get(position % myColorList.size())));
            //show favorite icon for only app users
            if (contactTile.getAppUsing() == 0 && !contactTile.getIsGroup()) {
                holder.fav.setVisibility(View.GONE);
                holder.appGroupUser.setVisibility(View.GONE);
            } else {
                holder.fav.setVisibility(View.VISIBLE);
                if (contactTile.getIsGroup()) {
                    holder.appGroupUser.setVisibility(View.VISIBLE);
                    holder.fav.setTextSize(20);
                    holder.appGroupUser.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast toast = Toast.makeText(context, "User Group", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        }
                    });
                } else {
                    holder.appGroupUser.setVisibility(View.GONE);
                    holder.fav.setTextSize(24);
                }
            }

            if (contactTile.getNotifications() > 0) {
                favorite(holder.fav);
            } else {
                unFavorite(holder.fav);
            }

            if (contactTile.getBlocked() == 1) {
                holder.blocked.setVisibility(View.VISIBLE);
                holder.tileImage1.setVisibility(View.GONE);
                holder.tileImage2.setVisibility(View.GONE);
                holder.tileImage3.setVisibility(View.GONE);
                holder.fav.setVisibility(View.GONE);
                holder.appGroupUser.setVisibility(View.GONE);
            } else {
                holder.blocked.setVisibility(View.GONE);
                setCoverDrawfts(holder.tileImage1, holder.tileImage2, holder.tileImage3, contactTile);
            }

           /* if (!contactTile.getIsGroup()) {
                contactTile.getBlocked();

            } else {
                if (gm.getBlocked(contactTile.getGroupId()) == 1) {
                    holder.blocked.setVisibility(View.VISIBLE);
                    holder.tileImage1.setVisibility(View.GONE);
                    holder.tileImage2.setVisibility(View.GONE);
                    holder.tileImage3.setVisibility(View.GONE);
                    holder.fav.setVisibility(View.GONE);
                } else {
                    holder.blocked.setVisibility(View.GONE);
                    setCoverDrawfts(holder.tileImage1, holder.tileImage2, holder.tileImage3, contactTile);
                }
            }*/

//        if (tiles.get(position).getFav() == 0) {
//            unFavorite(holder.fav);
//        } else {
//            favorite(holder.fav);
//        }

            holder.blocked.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast = Toast.makeText(context, "Blocked", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                }
            });

            holder.tileLabel.setText(contactTile.getGroupName());
            //holder.tileLabel.setText(colo);
            if (!contactTile.getIsGroup()) {
                holder.tileNumber.setText(contactTile.getGroupId());
            } else {
                holder.tileNumber.setText(contactTile.getMemberCount() + " members");
            }
            return rowView;
        } catch (Exception e) {
            return rowView;
        }

//        setCoverDrawfts(holder.tileImage1, holder.tileImage2, holder.tileImage3, contactTile);
    }

    public void favorite(TextView b) {
        //b.setTextColor(Color.parseColor("#FF8888"));
        b.setText(R.string.icon_tile_notification);
        b.setTextSize(24);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(context, "New Drawfts Available", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
        });
    }

    public void unFavorite(TextView b) {
        //b.setTextColor(Color.parseColor("#FFFFFF"));
        b.setText(R.string.icon_tile_user);
        //b.setTextSize(20);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(context, "App User", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
        });
    }

    public void setCoverDrawfts(SimpleDraweeView i1, SimpleDraweeView i2, SimpleDraweeView i3, ContactTile contactTile) {
        dimens = contactTile.getDimensions();
        if (dimens.size() != 0) {
            int newW1 = 0;
            int newH1 = 0;
            int newW2 = 0, newH2 = 0, newW3 = 0, newH3 = 0;
            String size1[], size2[], size3[];
            String sb_builder = updateGetDimensions();
            String dimen[] = sb_builder.split(",");
            switch (dimens.size()) {
                case 1:
                    size1 = dimen[0].split("-");
                    newW1 = Integer.parseInt(size1[0]);
                    newH1 = Integer.parseInt(size1[1]);
                    break;

                case 2:
                    size1 = dimen[0].split("-");
                    newW1 = Integer.parseInt(size1[0]);
                    newH1 = Integer.parseInt(size1[1]);
                    size2 = dimen[1].split("-");
                    newW2 = Integer.parseInt(size2[0]);
                    newH2 = Integer.parseInt(size2[1]);
                    break;

                case 3:
                    size1 = dimen[0].split("-");
                    newW1 = Integer.parseInt(size1[0]);
                    newH1 = Integer.parseInt(size1[1]);
                    size2 = dimen[1].split("-");
                    newW2 = Integer.parseInt(size2[0]);
                    newH2 = Integer.parseInt(size2[1]);
                    size3 = dimen[2].split("-");
                    newW3 = Integer.parseInt(size3[0]);
                    newH3 = Integer.parseInt(size3[1]);
                    break;
            }
            LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) i1.getLayoutParams();
            params1.width = newW1;
            params1.height = newH1;
            params1.gravity = Gravity.CENTER;
            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) i2.getLayoutParams();
            params2.width = newW2;
            params2.height = newH2;
            params2.gravity = Gravity.CENTER;
            LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams) i3.getLayoutParams();
            params3.width = newW3;
            params3.height = newH3;
            params3.gravity = Gravity.CENTER;
        }


        if (contactTile.getCoverPic1().size() != 0)
            switch (contactTile.getCoverPic1().size()) {
                case 1:
                    i1.setVisibility(View.VISIBLE);
                    i2.setVisibility(View.GONE);
                    i3.setVisibility(View.GONE);
                    i1.setImageURI(Uri.parse(contactTile.getCoverPic1().get(0)));
                    /*ImageLoader.getInstance().displayImage(contactTile.getCoverPic1().get(0), i1);*/
                    break;

                case 2:
                    i1.setVisibility(View.VISIBLE);
                    i2.setVisibility(View.VISIBLE);
                    i3.setVisibility(View.GONE);
                    i1.setImageURI(Uri.parse(contactTile.getCoverPic1().get(0)));
                    i2.setImageURI(Uri.parse(contactTile.getCoverPic1().get(1)));
                    /*ImageLoader.getInstance().displayImage(contactTile.getCoverPic1().get(0), i1);
                    ImageLoader.getInstance().displayImage(contactTile.getCoverPic1().get(1), i2);*/
                    break;

                case 3:
                    i1.setVisibility(View.VISIBLE);
                    i2.setVisibility(View.VISIBLE);
                    i3.setVisibility(View.VISIBLE);
                    i1.setImageURI(Uri.parse(contactTile.getCoverPic1().get(0)));
                    i2.setImageURI(Uri.parse(contactTile.getCoverPic1().get(1)));
                    i3.setImageURI(Uri.parse(contactTile.getCoverPic1().get(2)));
                    /*ImageLoader.getInstance().displayImage(contactTile.getCoverPic1().get(0), i1);
                    ImageLoader.getInstance().displayImage(contactTile.getCoverPic1().get(1), i2);
                    ImageLoader.getInstance().displayImage(contactTile.getCoverPic1().get(2), i3);*/
                    break;
            }
        else {
            i1.setVisibility(View.GONE);
            i2.setVisibility(View.GONE);
            i3.setVisibility(View.GONE);
        }
    }

    public void destroy() {
        this.context = null;
        this.tiles = null;
        this.dimens = null;
        /*this.holder.tileImage1.setImageDrawable(null);
        this.holder.tileImage2.setImageDrawable(null);
        this.holder.tileImage3.setImageDrawable(null);*/
        this.holder.tileImage1 = null;
        this.holder.tileImage2 = null;
        this.holder.tileImage3 = null;
        this.holder = null;
    }
}

