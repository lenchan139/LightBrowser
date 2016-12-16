package org.lenchan139.lightbrowser.Class;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by len on 10/16/16.
 */

public class Tab {
    private ArrayList<Page> currList = new ArrayList<Page>();
    int currIndex = 0;
    public Tab(Page page){
        currList.add(page);
    }

    public void addPage(Page page){
        if(currIndex != currList.size()-1) {
            currList.subList(currIndex, currList.size() - currIndex).clear();
            currList.add(page);
            currIndex = currList.size() - 1;
        }else{
            currList.add(page);
            currIndex++;
        }
        Log.v("curr:", String.valueOf(currIndex));
    }

    public boolean modifyPage(int pageIndex, Page newPage){
        boolean result = false;
            for (int i=0; i<currList.size();i++){
                if(pageIndex == i){
                    currList.set(i, newPage);
                    result = true;
                }
            }
        return result;
    }

    public boolean delPage(int PageIndex){
        boolean result = false;
        try{currList.remove(PageIndex);}catch (Exception e){result = true;}
        return result;
    }

    public Page moveToEnd(){
        currIndex = currList.size() - 1;
        return currList.get(currIndex);
    }
    public Page moveToFirst(){
        if(currList.size() >= 1) {
            currIndex = 0;
            return currList.get(currIndex);
        }else {
            return null;
        }
    }
    public Page moveToNext(){
            if (currIndex > 0 && currIndex+1 < currList.size()) {
                currIndex++;
                return currList.get(currIndex);

            } else {
                return null;
            }
    }

    public Page moveToPervious(){
        if(currIndex > 1 ){
            currIndex--;
            return currList.get(currIndex);
        }else{
            return null;
        }
    }
}
