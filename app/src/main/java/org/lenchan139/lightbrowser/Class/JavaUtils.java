package org.lenchan139.lightbrowser.Class;

import android.content.Intent;
import android.os.Parcelable;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by len on 28/9/2017.
 */

public class JavaUtils {
    public Parcelable[] listToPracelable(ArrayList<Intent> list){
        return list.toArray(new Parcelable[]{});
    }
}
