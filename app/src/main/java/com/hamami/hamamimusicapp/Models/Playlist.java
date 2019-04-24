package com.hamami.hamamimusicapp.Models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.hamami.hamamimusicapp.persistence.Converter;

import java.util.ArrayList;
import java.util.Objects;

@Entity(tableName = "playlists")
public class Playlist implements Parcelable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "songs")
    @TypeConverters(Converter.class)
    private ArrayList<Songs> songs;

    public Playlist(@NonNull String title,ArrayList<Songs> songs) {
        this.title = title;
        this.songs = songs;
    }

    @Ignore
    public Playlist() {

    }

    protected Playlist(Parcel in) {
        title = Objects.requireNonNull(in.readString());
        songs = in.createTypedArrayList(Songs.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeTypedList(songs);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };


    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public ArrayList<Songs> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<Songs> songs) {
        this.songs = songs;
    }

    @Override
    @NonNull
    public String toString() {
        return "Playlist{" +
                "title='" + title + '\'' +
                ", songs=" + songs +
                '}';
    }



}
