package org.lenchan139.lightbrowser.Settings

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

import org.lenchan139.lightbrowser.R
import org.lenchan139.lightbrowser.Settings.SettingsViewItem

/**
 * Created by len on 27/1/2017.
 */

class SettingsLVAdpter(context: Context, private val svi: List<SettingsViewItem>) : BaseAdapter() {
    private val myInflater: LayoutInflater

    init {
        myInflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return svi.size
    }

    override fun getItem(position: Int): SettingsViewItem {
        return svi[position]
    }

    override fun getItemId(position: Int): Long {
        return svi.indexOf(getItem(position)).toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView

        var holder: SettingsViewHolder?
        if (convertView == null) {
            convertView = myInflater.inflate(R.layout.settings_view_item, null)
            val ocl: View.OnClickListener = View.OnClickListener {  }
            holder = SettingsViewHolder(
                    convertView!!.findViewById(R.id.title) as TextView,
                    convertView.findViewById(R.id.time) as TextView,
                    ocl
            )
            convertView.tag = holder
        } else {
            holder = convertView.tag as SettingsViewHolder
        }
        val svi = getItem(position)
        val color_title = intArrayOf(Color.WHITE, Color.WHITE, Color.YELLOW)
        val color_time = intArrayOf(Color.WHITE, Color.WHITE, Color.YELLOW)
        val color_back = intArrayOf(Color.BLACK, Color.BLUE, Color.BLACK)
        val time_vis = intArrayOf(View.VISIBLE, View.GONE, View.VISIBLE)

        holder.title.text = svi.title
        holder.content.text = svi.content

        return convertView
    }
}
