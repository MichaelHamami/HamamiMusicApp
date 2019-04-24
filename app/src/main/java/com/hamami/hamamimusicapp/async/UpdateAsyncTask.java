package com.hamami.hamamimusicapp.async;

import android.os.AsyncTask;
import android.util.Log;

import com.hamami.hamamimusicapp.Models.Playlist;
import com.hamami.hamamimusicapp.persistence.PlaylistDao;

public class UpdateAsyncTask extends AsyncTask<Playlist,Void,Void> {

    private static final String TAG = "UpdateAsyncTask";

    private PlaylistDao mPlaylistDao;

    public UpdateAsyncTask(PlaylistDao dao) {
        mPlaylistDao = dao;
    }

    @Override
    protected Void doInBackground(Playlist... playlists) {
        Log.d(TAG, "doInBackground: thread: " +Thread.currentThread().getName());
        Log.d(TAG, "doInBackground: Update Async Called");
        mPlaylistDao.update(playlists);
        return null;
    }
}
