package org.lenchan139.lightbrowser

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*

import org.lenchan139.lightbrowser.Class.ClearableEditText
import org.lenchan139.lightbrowser.Class.CommonStrings
import org.lenchan139.lightbrowser.Settings.SettingsLVAdpter
import org.lenchan139.lightbrowser.Settings.SettingsViewItem

import java.util.ArrayList
import java.util.Objects

class SettingsActivity : AppCompatActivity() {
    private var listViewSetting: ListView? = null
    internal var listSetting: MutableList<SettingsViewItem> = ArrayList()
    internal lateinit var commonStrings : CommonStrings
    private var sp: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        commonStrings = CommonStrings(applicationContext)
        val ocl: View.OnClickListener? = null
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        sp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        initListView()
    }


    fun initListView() {

        listViewSetting = findViewById(R.id.settingList) as ListView
        listSetting.clear()
        listSetting.add(SettingsViewItem(getString(R.string.setting_string_homepage), sp!!.getString(commonStrings.TAG_pref_home(), getString(R.string.setting_string_default)), null))
        listSetting.add(SettingsViewItem(getString(R.string.setting_string_fabbutton), sp!!.getString(commonStrings.TAG_pref_fab(), getString(R.string.setting_string_disabled)), null))
        listSetting.add(SettingsViewItem(getString(R.string.string_pref_custom_user_agent), sp!!.getString(commonStrings.TAG_pref_custom_user_agent(), getString(R.string.setting_string_default)), null))
        listSetting.add(SettingsViewItem("Sharing Format", sp!!.getString(commonStrings.TAG_pref_sharing_format_string(), CommonStrings(applicationContext).ARRAY_pref_Sharing_Format()[0]), null))
        listSetting.add(SettingsViewItem("Search Engine", sp!!.getString(commonStrings.TAG_pref_Search_Engine_Name(),commonStrings.ARRAY_pref_Search_Engine_Default().get(0).name) , null))

        listViewSetting!!.adapter = SettingsLVAdpter(this, listSetting)

        listViewSetting!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            if (position == 0) {
                onClickHome()
            } else if (position == 1) {
                onClickFabButton()
            } else if (position == 2) {
                onClickCustomUserAgent()
            }else if(position == 3){
                onClickSharingFormat()
            }else if(position == 4){
                onClickSearchEngine()
            }
            val o = listViewSetting!!.getItemAtPosition(position)
            val str = o as SettingsViewItem//As you are using Default String Adapter
        }
    }


    //onClick function below
    override fun onResume() {
        toolbar.title = getString(R.string.setting_activity)
        super.onResume()
    }

    override fun onPause() {
        toolbar.title = getString(R.string.app_name)
        super.onPause()
    }

    fun onClickHome() {
        val txtUrl = ClearableEditText(this)

        // Set the default text to a link of the Queen
        txtUrl.hint = "Enter your homepage ..."
        txtUrl.setText(sp!!.getString(commonStrings.TAG_pref_home(), null))
        txtUrl.setPadding(30, 15, 30, 15)
        txtUrl.setSingleLine(true)
        txtUrl.imeOptions = EditorInfo.IME_ACTION_DONE
        AlertDialog.Builder(this)
                .setTitle("Custom Homepage")
                //.setMessage("Enter new URL of homepage")
                .setView(txtUrl)
                .setPositiveButton(getString(R.string.setting_string_apply)) { dialog, whichButton ->
                    var temp = txtUrl.text.toString()
                    if (!temp.startsWith("http") && !temp.contains(".")) {
                        Toast.makeText(this@SettingsActivity, getString(R.string.setting_string_), Toast.LENGTH_SHORT).show()
                    } else if (!txtUrl.text.toString().startsWith("http")) {
                        temp = "http://" + temp
                        sp!!.edit().putString(commonStrings.TAG_pref_home(), txtUrl.text.toString()).commit()
                        Toast.makeText(this@SettingsActivity, getString(R.string.setting_string_save_change), Toast.LENGTH_SHORT).show()
                    } else {
                        sp!!.edit().putString(commonStrings.TAG_pref_home(), txtUrl.text.toString()).commit()
                        Toast.makeText(this@SettingsActivity, getString(R.string.setting_string_save_change), Toast.LENGTH_SHORT).show()
                    }
                    initListView()
                }
                .setNegativeButton(getString(R.string.setting_string_cancel)) { dialog, whichButton -> }
                .show()
    }

    fun onClickCustomUserAgent() {
        val txtUrl = ClearableEditText(this)

        // Set the default text to a link of the Queen
        txtUrl.hint = getString(R.string.setting_string_enter_hint)
        txtUrl.setText(sp!!.getString(commonStrings.TAG_pref_oc_bookmark_url(), null))
        txtUrl.setPadding(30, 15, 30, 15)
        txtUrl.imeOptions = EditorInfo.IME_ACTION_DONE
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.setting_string_custom_user_agent_title))
                .setMessage("***It will make Tools/Desktop Mode to be disabled.***")
                .setView(txtUrl)
                .setNeutralButton(getString(R.string.setting_string_default), DialogInterface.OnClickListener { dialog, which ->
                    sp!!.edit().remove(commonStrings.TAG_pref_custom_user_agent()).commit()
                    Toast.makeText(this@SettingsActivity, getString(R.string.setting_string_save_change),Toast.LENGTH_SHORT).show()
                })
                .setPositiveButton(getString(R.string.setting_string_apply)) { dialog, whichButton ->
                    sp!!.edit().putString(commonStrings.TAG_pref_custom_user_agent(), txtUrl.text.toString()).commit()
                    Toast.makeText(this@SettingsActivity, getString(R.string.setting_string_save_change), Toast.LENGTH_SHORT).show()

                    initListView()
                }
                .setNegativeButton(getString(R.string.setting_string_cancel)) { dialog, whichButton -> }
                .show()
    }


    fun onClickFabButton() {
        val items = commonStrings.ARRAY_pref_fab()

        val curr = sp!!.getString(commonStrings.TAG_pref_fab(), null)
        var oldPos = 0
        for (i in items.indices) {
            if (items[i] == curr) {
                oldPos = i
            }
        }
        val dialog = AlertDialog.Builder(this).setTitle(getString(R.string.setting_string_fabbutton_mode)).setIcon(R.mipmap.ic_launcher)
                .setSingleChoiceItems(items, oldPos) { dialog, which ->
                    sp!!.edit().putString(commonStrings.TAG_pref_fab(), items[which]).apply()
                    Toast.makeText(this@SettingsActivity, getString(R.string.setting_string_save_turn_to) + sp!!.getString(commonStrings.TAG_pref_fab(), null) + "!", Toast.LENGTH_SHORT).show()

                    dialog.dismiss()
                    initListView()
                }.create()
        dialog.show()
    }

    fun onClickSharingFormat() {
        val items = commonStrings.ARRAY_pref_Sharing_Format()

        var oldPos =  sp!!.getInt(commonStrings.TAG_pref_sharing_format_int(), 0)

        val dialog = AlertDialog.Builder(this).setTitle("Sharing Format").setIcon(R.mipmap.ic_launcher)
                .setSingleChoiceItems(items, oldPos) { dialog, which ->
                    sp!!.edit().putInt(commonStrings.TAG_pref_sharing_format_int(), which).apply()
                    sp!!.edit().putString(commonStrings.TAG_pref_sharing_format_string(), items[which]).apply()
                    Toast.makeText(this@SettingsActivity, getString(R.string.setting_string_save_turn_to) + sp!!.getString(commonStrings.TAG_pref_sharing_format_string(), null) + "!", Toast.LENGTH_SHORT).show()

                    dialog.dismiss()
                    initListView()
                }.create()
        dialog.show()
    }
    fun onClickSearchEngine(){
        val preIitems = commonStrings.ARRAY_pref_Search_Engine_Default()
        var items : Array<String> = Array<String>(preIitems.count()){""}

        for ((i,preItem) in preIitems.withIndex()) {
            items[i] = preItem.name
        }



        val dialog = AlertDialog.Builder(this).setTitle("Search Engine").setIcon(R.mipmap.ic_launcher)
                .setSingleChoiceItems(items, -1) { dialog, which ->
                    sp!!.edit().putString(commonStrings.TAG_pref_Search_Engine_Url(), preIitems[which].url).apply()
                    sp!!.edit().putString(commonStrings.TAG_pref_Search_Engine_Name(), preIitems[which].name).apply()
                    Toast.makeText(this@SettingsActivity, getString(R.string.setting_string_save_turn_to) + sp!!.getString(commonStrings.TAG_pref_Search_Engine_Url(), null) + "!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    initListView()
                }.setNeutralButton("Custom...") { dialog, whichButton ->
                         val txtUrl = ClearableEditText(this)
                         // Set the default text to a link of the Queen
                        txtUrl.hint = "Enter your search engine url..."
                        txtUrl.setText(sp!!.getString(commonStrings.TAG_pref_Search_Engine_Url(), null))
                        txtUrl.setPadding(30, 15, 30, 15)
                        txtUrl.setSingleLine(true)
                        txtUrl.imeOptions = EditorInfo.IME_ACTION_DONE
                          AlertDialog.Builder(this)
                            .setTitle("Custom Search Engine")
                            .setMessage("Enter your search engine url with !@keyword for replacing")
                            .setView(txtUrl)
                            .setPositiveButton(getString(R.string.setting_string_apply)) { dialog, whichButton ->
                                sp!!.edit().putString(commonStrings.TAG_pref_Search_Engine_Url(), txtUrl.text.toString()).commit()
                                sp!!.edit().putString(commonStrings.TAG_pref_Search_Engine_Name(), "Custom: " + txtUrl.text.toString()).commit()
                                Toast.makeText(this@SettingsActivity, getString(R.string.setting_string_save_change), Toast.LENGTH_SHORT).show()

                                initListView()
                            }
                            .setNegativeButton(getString(R.string.setting_string_cancel)) { dialog, whichButton -> }
                            .show()

                     }


                .create()
            dialog.show()
        }
    }


