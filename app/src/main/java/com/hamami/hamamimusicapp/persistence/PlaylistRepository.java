package com.hamami.hamamimusicapp.persistence;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.hamami.hamamimusicapp.Models.Playlist;
import com.hamami.hamamimusicapp.async.DeleteAsyncTask;
import com.hamami.hamamimusicapp.async.InsertAsyncTask;
import com.hamami.hamamimusicapp.async.UpdateAsyncTask;

import java.util.List;

public class PlaylistRepository {

    private PlaylistDatabase mPlaylistDatabase;

    public PlaylistRepository(Context context) {
        mPlaylistDatabase = PlaylistDatabase.getInstance(context);
    }

    public void insertPlaylistTask(Playlist playlist)
    {
        new InsertAsyncTask(mPlaylistDatabase.getPlaylistDao()).execute(playlist);
    }
    public void updatePlaylistTask(Playlist playlist)
    {
        new UpdateAsyncTask(mPlaylistDatabase.getPlaylistDao()).execute(playlist);
    }

    public LiveData<List<Playlist>> retrievePlaylistsTask()
    {
        return mPlaylistDatabase.getPlaylistDao().getPlaylists();
    }

    public List<Playlist> getPlaylistAsArrayList()
    {
        return mPlaylistDatabase.getPlaylistDao().getPlaylistsAsArrayList();
    }

    public void deletePlaylist(Playlist playlist)
    {
        new DeleteAsyncTask(mPlaylistDatabase.getPlaylistDao()).execute(playlist);
    }
//    public List<String> getPlaylistTitles()
//    {
//        return mPlaylistDatabase.getPlaylistDao().getPlaylistTitles();
//    }
}
