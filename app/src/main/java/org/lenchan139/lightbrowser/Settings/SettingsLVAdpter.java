package org.lenchan139.lightbrowser.Settings;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.lenchan139.lightbrowser.R;
import org.lenchan139.lightbrowser.Settings.SettingsViewItem;

import java.util.List;

/**
 * Created by len on 27/1/2017.
 */

public class SettingsLVAdpter extends BaseAdapter {
    private LayoutInflater myInflater;
    private List<SettingsViewItem> svi;
    public SettingsLVAdpter(Context context, List<SettingsViewItem> svi){
        myInflater = LayoutInflater.from(context);
        this.svi = svi;
    }

    @Override
    public int getCount() {
        return svi.size();
    }

    @Override
    public SettingsViewItem getItem(int position) {
        return svi.get(position);
    }

    @Override
    public long getItemId(int position) {
        return svi.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SettingsViewHolder holder = null;
        if(convertView==null){
            convertView = myInflater.inflate(R.layout.settings_view_item, null);
            View.OnClickListener ocl = null;
            holder = new SettingsViewHolder(
                    (TextView) convertView.findViewById(R.id.title),
                    (TextView) convertView.findViewById(R.id.time),
                    ocl
            );
            convertView.setTag(holder);
        }else{
            holder = (SettingsViewHolder) convertView.getTag();
        }
        SettingsViewItem svi = (SettingsViewItem)getItem(position);
        //0 = movie, 1 = title, 2 = nine
        int color_title[] = {Color.WHITE,Color.WHITE,Color.YELLOW};
        int color_time[] = {Color.WHITE,Color.WHITE,Color.YELLOW};
        int color_back[] = {Color.BLACK,Color.BLUE,Color.BLACK};
        int time_vis[] = {View.VISIBLE,View.GONE,View.VISIBLE};

        holder.title.setText(svi.getTitle());
        holder.content.setText(svi.getContent());

        return convertView;
    }
}
