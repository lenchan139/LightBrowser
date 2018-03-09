package org.lenchan139.lightbrowser

import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView

import org.lenchan139.lightbrowser.History.CustomScriptItem
import org.lenchan139.lightbrowser.History.HistroySQLiteController

import java.util.ArrayList

class HistoryActivity : AppCompatActivity() {
    private var histroySQLiteController: HistroySQLiteController? = null
    internal lateinit var historyList: ArrayList<CustomScriptItem>
    internal lateinit var hList: ListView
    internal lateinit var fab: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        histroySQLiteController = HistroySQLiteController(this)
        historyList = histroySQLiteController!!.history
        hList = findViewById(R.id.historyList) as ListView
        fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { finish() }
        supportActionBar?.title = "History"
        val showlist = arrayOfNulls<String>(historyList.size)
        for (i in historyList.indices) {
            showlist[i] = historyList[i].title + "\n" + historyList[i].url
        }

        val adapter = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, showlist)
        hList.adapter = adapter
        hList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val intent = Intent(this@HistoryActivity, MainActivity::class.java)
            intent.putExtra("InURL", historyList[position].url)
            startActivity(intent)
            finish()
        }
    }


}
