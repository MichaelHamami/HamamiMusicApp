package com.hamami.hamamimusicapp.persistence;

import android.util.Log;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hamami.hamamimusicapp.Models.Songs;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Converter {
    private static final String TAG = "Converter";
    @TypeConverter
    public static ArrayList<Songs> fromString(String value) {
        Log.d(TAG, "fromString: Converter used return ArrayList songs");
        Type listType = new TypeToken<ArrayList<Songs>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }
    @TypeConverter
    public static String fromArrayList(ArrayList<Songs> list) {
        Log.d(TAG, "fromArrayList: Converter used return String");
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}
