package org.lenchan139.lightbrowser

import android.database.sqlite.SQLiteConstraintException
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.SpannableStringBuilder
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_custom_script.*
import org.lenchan139.lightbrowser.Class.CommonStrings
import org.lenchan139.lightbrowser.CustomScript.CustomScriptSQLiteController
import org.lenchan139.lightbrowser.CustomScript.CustomScriptUtil

class AddOrEditCustomScriptActivity : AppCompatActivity() {
    lateinit var csController : CustomScriptSQLiteController
    var isEditMode : Boolean = false
    var inEditTitle : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_custom_script)
        isEditMode = intent.getBooleanExtra(CommonStrings(this).TAG_custom_script_isEdit(),false)
        inEditTitle = intent.getStringExtra(CommonStrings(this).TAG_custom_script_editTitle())

        if(supportActionBar != null){
            supportActionBar!!.title = "Add Custom Script"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        if(isEditMode && inEditTitle != null){
            val csItem = CustomScriptUtil().getScript(this,inEditTitle!!)
            if(csItem != null) {
                edtTitle.text = SpannableStringBuilder(csItem.title)
                edtUrl.text = SpannableStringBuilder(csItem.url)
                edtScript.text = SpannableStringBuilder(csItem.script)
                edtTitle.isEnabled = false
                supportActionBar!!.title = "Edit Custom Script"
            }
        }
        csController = CustomScriptSQLiteController(this)
        btnSave.setOnClickListener {
            onSaving()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    fun onSaving(){
        if(edtTitle.text.isNotEmpty() && edtScript.text.isNotEmpty() && edtUrl.text.isNotEmpty()) {
            try {
                if(isEditMode){
                    CustomScriptUtil().updateScript(this,
                            edtTitle.text.toString().trim(),
                            edtUrl.text.toString().trim(),
                            edtScript.text.toString().trim())
                    Toast.makeText(this, "Custom Script Updated.", Toast.LENGTH_SHORT).show()
                }else {
                    CustomScriptUtil().addScript(this,
                            edtTitle.text.toString().trim(),
                            edtUrl.text.toString().trim(),
                            edtScript.text.toString().trim())
                    Toast.makeText(this, "Custom Script Added.", Toast.LENGTH_SHORT).show()
                }
                finish()
            }catch (e : SQLiteConstraintException){
                e.printStackTrace()
                Toast.makeText(this,"Error: title is exist.",Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun initWhenEdit(){

    }
}
