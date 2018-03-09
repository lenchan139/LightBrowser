package org.lenchan139.lightbrowser

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView

import org.lenchan139.lightbrowser.History.CustomScriptItem
import org.lenchan139.lightbrowser.History.HistroySQLiteController

import java.util.ArrayList
import android.app.ActivityManager
import android.support.v4.app.FragmentActivity
import android.util.Log


class SearchActivity : AppCompatActivity() {
    private var histroySQLiteController: HistroySQLiteController? = null
    internal var historyList = ArrayList<CustomScriptItem>()
    internal lateinit var hList: ListView
    internal lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        histroySQLiteController = HistroySQLiteController(this)
        hList = findViewById(R.id.list) as ListView
        editText = findViewById(R.id.editText) as EditText
        editText.setText(intent.getStringExtra("para"))
        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            hideKeybord()
            finish()
        }
        val window = this.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        editText.setSelection(0, editText.text.length)
        //update

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                updateListView()
                //editText.setSelection(editText.text.length)
            }
        })
        editText.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            // If the event is a key-down event on the "enter" button
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                // Perform action on key press
                onEnter()

                return@OnKeyListener true
            }
            false
        })
    }


    override fun onPause() {
        finish()
        super.onPause()
    }

    private fun updateListView() {
        historyList.clear()
        try {
            historyList = histroySQLiteController!!.getHistoryBySearchUrl(editText.text.toString())
        } catch (e: SQLiteException) {
            e.printStackTrace()
            hList.adapter = null
        }

        val showlist = arrayOfNulls<String>(historyList.size)
        for (i in historyList.indices) {
            showlist[i] = historyList[i].title + "\n" + historyList[i].url
        }

        val adapter = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, showlist)
        hList.adapter = adapter
        hList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            editText.setText(historyList[position].url)
            onEnter()
        }
    }

    private fun onEnter() {
        val intent = Intent(this@SearchActivity, MainActivity::class.java)
        intent.putExtra("InURL", editText.text.toString())
        startActivity(intent)
        finish()
    }

    fun hideKeybord() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}



