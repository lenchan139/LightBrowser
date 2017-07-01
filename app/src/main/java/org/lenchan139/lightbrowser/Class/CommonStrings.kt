package org.lenchan139.lightbrowser.Class

import android.net.Uri

/**
 * Created by len on 10/15/16.
 */

class CommonStrings {
    fun searchHeader(): String {
        return "https://duckduckgo.com/?q="
    }

    fun mobilizeHeader(): String {
        return "https://googleweblight.com/?lite_url="
    }

    fun homePage(): String {
        return "https://ddg.gg"
    }

    fun TAG_setting(): String {
        return "org.lenchan139.lightbrowser.settings"
    }

    fun TAG_pref_home(): String {
        return "org.lenchan139.lightbrowser.Home"
    }

    fun TAG_pref_fab(): String {
        return "org.lenchan139.lightbrowser.Fab"
    }

    fun ARRAY_pref_fab(): Array<String> {
        return arrayOf("Disable", "Home", "Refresh", "Share")
    }

    fun STR_INTENT_MainToSetting(): String {
        return "org.lenchan139.lightbrowser.STR_INTENT_MainToSetting"
    }

    fun TAG_pref_oc_bookmark_url(): String {
        return "org.lenchan139.lightbrowser.TAG_pref_oc_bookmark_url"
    }
    fun TAG_pref_custom_user_agent(): String {
        return "org.lenchan139.lightbrowser.TAG_pref_custom_user_agent"
    }
    fun TAG_pref_custom_user_agent_default(): String {
        return "org.lenchan139.lightbrowser.TAG_pref_custom_user_agent_default"
    }

    fun URL_DDG(): String {
        return "https://duckduckgo.com"
    }
}
