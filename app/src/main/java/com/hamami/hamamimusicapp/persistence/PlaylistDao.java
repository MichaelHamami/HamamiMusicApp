package com.hamami.hamamimusicapp.persistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.hamami.hamamimusicapp.Models.Playlist;

import java.util.List;

@SuppressWarnings("UnusedReturnValue")
@Dao
public interface PlaylistDao {

    @Insert
    long[] insertPlaylist(Playlist... playlist);

    @Query("SELECT * FROM playlists")
    LiveData<List<Playlist>> getPlaylists();

    @Query("SELECT * FROM playlists")
    List<Playlist> getPlaylistsAsArrayList();
//
//    @Query("SELECT * FROM playlists WHERE title LIKE :title")
//    List<Playlist> getSpecificPlaylist(String title);
//
//    @Query("SELECT title FROM playlists")
//    List<String> getPlaylistTitles();

    @Delete
    int delete(Playlist... playlist);

    @Update
    int update(Playlist... playlist);

}
