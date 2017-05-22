package org.lenchan139.lightbrowser.History

/**
 * Created by len on 14/4/2017.
 */

class HistoryItem(var title: String?, var url: String?, var date: String?) {

    override fun toString(): String {
        return "title=$title,url=$url,date=$date"
    }
}
