package com.hamami.hamamimusicapp.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Songs implements Parcelable {
    private String fileSongPath;
    private String nameSong;
    private String SongLength;

    //constructor initializing values
    public Songs(String fileSongPath, String nameSong, String SongLength) {
        this.fileSongPath = fileSongPath;
        this.nameSong = nameSong;
        this.SongLength = SongLength;
    }


    protected Songs(Parcel in) {
        fileSongPath = in.readString();
        nameSong = in.readString();
        SongLength = in.readString();
    }

    public static final Creator<Songs> CREATOR = new Creator<Songs>() {
        @Override
        public Songs createFromParcel(Parcel in) {
            return new Songs(in);
        }

        @Override
        public Songs[] newArray(int size) {
            return new Songs[size];
        }
    };

    //getters
    public String getNameSong() {
        return nameSong;
    }

    public String getSongLength() {
        return SongLength;
    }

    public String getFileSong() {
        return fileSongPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileSongPath);
        dest.writeString(nameSong);
        dest.writeString(SongLength);
    }

    @Override
    public String toString() {
        return
                "file: " + fileSongPath +
                ", name: " + nameSong;
    }
}


