package org.lenchan139.lightbrowser

import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_custom_script.*
import org.lenchan139.lightbrowser.Class.CommonStrings
import org.lenchan139.lightbrowser.CustomScript.CustomScriptUtil

class CustomScriptActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_script)
        if(supportActionBar != null){
            supportActionBar!!.title = "Custom Script Settings"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        fab.setOnClickListener {
            gotoAddScript()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
        fullUpdateListview()
    }

    fun gotoAddScript(){
        val intent = Intent(this, AddOrEditCustomScriptActivity::class.java)
        startActivity(intent)
    }
    fun fullUpdateListview(){
        val showlist = CustomScriptUtil().getFullCustomScriptList(this)
        val scriptlist = CustomScriptUtil().getRawFullCustomScriptList(this)!!
        val adapter = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, showlist)
        listView.adapter = adapter
        listView.setOnItemClickListener { adpterView, view, i, l ->
            val dialog = AlertDialog.Builder(this)
            val menu = arrayOf("Edit","Delete")
            dialog.setTitle(scriptlist[i].title)
                    .setItems(menu, DialogInterface.OnClickListener { dialogInterface, i1 ->
                        if(i1==0){
                            val intent = Intent(this, AddOrEditCustomScriptActivity().javaClass)
                            intent.putExtra(CommonStrings(this).TAG_custom_script_isEdit(),
                                    true)
                            intent.putExtra(CommonStrings(this).TAG_custom_script_editTitle(),
                                    scriptlist[i].title)
                            startActivity(intent)
                        }else if(i1==1){
                            try {

                                CustomScriptUtil().delScript(this,scriptlist[i].title!!)
                                Toast.makeText(this,"Compelete!",Toast.LENGTH_SHORT).show()
                                fullUpdateListview()
                            }catch (e:SQLiteException){
                                e.printStackTrace()
                                Toast.makeText(this,"Error: Deletion Failed.",Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                    .show()
        }

    }
}
