package org.lenchan139.lightbrowser.Class

import android.util.Log

import java.util.ArrayList

/**
 * Created by len on 10/16/16.
 */

class Tab(page: Page) {
    private val currList = ArrayList<Page>()
    internal var currIndex = 0

    init {
        currList.add(page)
    }

    fun addPage(page: Page) {
        if (currIndex != currList.size - 1) {
            currList.subList(currIndex, currList.size - currIndex).clear()
            currList.add(page)
            currIndex = currList.size - 1
        } else {
            currList.add(page)
            currIndex++
        }
        Log.v("curr:", currIndex.toString())
    }

    fun modifyPage(pageIndex: Int, newPage: Page): Boolean {
        var result = false
        for (i in currList.indices) {
            if (pageIndex == i) {
                currList[i] = newPage
                result = true
            }
        }
        return result
    }

    fun delPage(PageIndex: Int): Boolean {
        var result = false
        try {
            currList.removeAt(PageIndex)
        } catch (e: Exception) {
            result = true
        }

        return result
    }

    fun moveToEnd(): Page {
        currIndex = currList.size - 1
        return currList[currIndex]
    }

    fun moveToFirst(): Page? {
        if (currList.size >= 1) {
            currIndex = 0
            return currList[currIndex]
        } else {
            return null
        }
    }

    fun moveToNext(): Page? {
        if (currIndex > 0 && currIndex + 1 < currList.size) {
            currIndex++
            return currList[currIndex]

        } else {
            return null
        }
    }

    fun moveToPervious(): Page? {
        if (currIndex > 1) {
            currIndex--
            return currList[currIndex]
        } else {
            return null
        }
    }
}
