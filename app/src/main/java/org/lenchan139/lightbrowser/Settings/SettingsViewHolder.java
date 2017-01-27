package org.lenchan139.lightbrowser.Settings;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by len on 27/1/2017.
 */

public class SettingsViewHolder  {
     TextView title, content;
    View.OnClickListener clickHandler;
    public SettingsViewHolder(TextView title, TextView content, View.OnClickListener clickListener){
        this.title = title;
        this.content = content;
        this.clickHandler = clickListener;
    }
}
