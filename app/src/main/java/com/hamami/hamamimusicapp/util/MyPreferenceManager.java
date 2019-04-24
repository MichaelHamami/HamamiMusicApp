package com.hamami.hamamimusicapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;

import static com.hamami.hamamimusicapp.util.Constants.LAST_ARTIST;
import static com.hamami.hamamimusicapp.util.Constants.MEDIA_QUEUE_POSITION;
import static com.hamami.hamamimusicapp.util.Constants.MUSIC_ROOT_FOLDER;
import static com.hamami.hamamimusicapp.util.Constants.NOW_PLAYING;
import static com.hamami.hamamimusicapp.util.Constants.PLAYLIST_ID;


public class MyPreferenceManager {


    private static final String TAG = "MyPreferenceManager";

    private SharedPreferences mPreferences;

    public MyPreferenceManager(Context context) {
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getPlaylistId()
    {
        return mPreferences.getString(PLAYLIST_ID,"");
    }

    public void savePlaylistId(String playlistId){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PLAYLIST_ID, playlistId);
        editor.apply();
    }

    public void saveQueuePosition(int position){
        Log.d(TAG, "saveQueuePosition: SAVING QUEUE INDEX: " + position);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(MEDIA_QUEUE_POSITION, position);
        editor.apply();
    }

    public int getQueuePosition(){
        return mPreferences.getInt(MEDIA_QUEUE_POSITION, -1);
    }

//    public void saveLastPlayedArtistImage(String url){
//        SharedPreferences.Editor editor = mPreferences.edit();
//        editor.putString(LAST_ARTIST_IMAGE, url);
//        editor.apply();
//    }

//    public String getLastPlayedArtistImage(){
//        return  mPreferences.getString(LAST_ARTIST_IMAGE, "");
//    }
    public void saveLastPlayedMedia(String mediaId){
    SharedPreferences.Editor editor = mPreferences.edit();
    editor.putString(NOW_PLAYING, mediaId);
    editor.apply();
}

    public String getLastPlayedMedia(){
        return mPreferences.getString(NOW_PLAYING, "");
    }
    public String getLastPlayedArtist(){
        return  mPreferences.getString(LAST_ARTIST, "");
    }



    public void saveLastPlayedArtist(String artist){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(LAST_ARTIST, artist);
        editor.apply();
    }

    public void saveLastRootMediaFolder(File root){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(MUSIC_ROOT_FOLDER, root.getAbsolutePath().toString());
        editor.apply();
    }

    public String getLastRootMediaFolder(){
        return mPreferences.getString(MUSIC_ROOT_FOLDER, "");
    }
}
