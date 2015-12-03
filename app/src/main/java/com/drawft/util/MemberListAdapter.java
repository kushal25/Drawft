package com.drawft.util;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.drawft.GroupDrawft;
import com.drawft.R;
import com.drawft.data.ContactTile;

import java.util.ArrayList;


public class MemberListAdapter extends BaseAdapter {
    private final Activity context;
    public ArrayList<ContactTile> list = new ArrayList<ContactTile>();
    private String action = "add";
    static OnRemoveListener removeListener = null;
    static OnAddListener addListener = null;
    private static final String TAG = MemberListAdapter.class.getSimpleName();
    private int isAdmin = 0;


    static class ViewHolder {
        public TextView tileLabel;
        public TextView tileAction;
    }

    public MemberListAdapter(Activity context, String act) {
        super();
        this.context = context;
        this.action = act;

        /*for (int i = 1; i < 30; i++) {
            ContactTile tile = new ContactTile();
            tile.setGroupName("Ashok-" + i);
            tile.setMobileNumber("919959833920");
            this.addItem(tile);
        }*/
    }

    public void addItem(ContactTile tile) {
        this.list.add(tile);
        notifyDataSetChanged();
    }

    public void addItemAtStart(ContactTile tile) {
        this.list.add(0, tile);
        notifyDataSetChanged();
    }

    public void concatList(ArrayList<ContactTile> newTiles) {
        this.list = newTiles;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public ContactTile getItem(int pos) {
        return this.list.get(pos);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setAddListener(OnAddListener listener) {
        MemberListAdapter.addListener = listener;
    }

    public void setRemoveListener(OnRemoveListener listener) {
        MemberListAdapter.removeListener = listener;
    }

    public void removeItem(int pos) {
        this.list.remove(pos);
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.group_member_tile, null);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.tileLabel = (TextView) rowView.findViewById(R.id.memberLabel);
            viewHolder.tileLabel.setTypeface(GroupDrawft.robotoLight);
            viewHolder.tileAction = (TextView) rowView.findViewById(R.id.memberTileAction);
            rowView.setTag(viewHolder);
            viewHolder.tileAction.setTypeface(GroupDrawft.fontFeather);
        }


        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        ContactTile contactTile = this.list.get(position);

        holder.tileLabel.setText(contactTile.getGroupName());
        if (this.action.equals("add")) {
            holder.tileAction.setText(R.string.icon_add_member);
        } else if (isAdmin==0 || GroupDrawft.P.MOBILE_NUMBER.equals(contactTile.getMobileNumber())) {
            holder.tileAction.setVisibility(View.GONE);
        } else {
            holder.tileAction.setText(R.string.icon_remove_member);
            holder.tileAction.setVisibility(View.VISIBLE);
        }
        holder.tileAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.imageanim));
                if (MemberListAdapter.this.action.equals("add")) {
                    MemberListAdapter.addListener.onAddMember(position);
                } else {
                    MemberListAdapter.removeListener.onRemoveMember(position);
                }
            }
        });
        return rowView;

    }

    public void setAdmin(int admin)
    {
        this.isAdmin = admin;
    }

    public static abstract interface OnRemoveListener {
        public abstract void onRemoveMember(int pos);
    }

    public static abstract interface OnAddListener {
        public abstract void onAddMember(int pos);
    }
}
