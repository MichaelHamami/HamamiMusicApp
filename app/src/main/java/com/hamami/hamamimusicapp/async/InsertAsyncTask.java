package com.hamami.hamamimusicapp.async;

import android.os.AsyncTask;
import android.util.Log;

import com.hamami.hamamimusicapp.Models.Playlist;
import com.hamami.hamamimusicapp.persistence.PlaylistDao;

public class InsertAsyncTask extends AsyncTask<Playlist,Void,Void> {

    private static final String TAG = "InsertAsyncTask";

    private PlaylistDao mPlaylistDao;
    public InsertAsyncTask(PlaylistDao dao) {
        mPlaylistDao = dao;
    }

    @Override
    protected Void doInBackground(Playlist... playlists) {
        Log.d(TAG, "doInBackground: thread: " +Thread.currentThread().getName());
        Log.d(TAG, "doInBackground: InsertAsync Called");
        mPlaylistDao.insertPlaylist(playlists);
        return null;
    }
}
