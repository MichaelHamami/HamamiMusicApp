package com.hamami.hamamimusicapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hamami.hamamimusicapp.R;


public class UserGuideFragment extends Fragment {

    private static final String TAG = "UserGuideFragment";

    private TextView mAppName;
    private TextView mAboutMe;
    private TextView mVersion;
    private ImageView mIconApp;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_user_guide,container,false);
    }

    // called after onCreateView
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, "onViewCreated: we get here?");
        mAboutMe = view.findViewById(R.id.about_text);
        mAppName = view.findViewById(R.id.about_app_name);
        mIconApp = view.findViewById(R.id.about_app_icon);
        mVersion = view.findViewById(R.id.about_version);

        mAboutMe.setText("My name Micahel Hamami Starter Developer");
        mVersion.setText("Version 1");
//        mIconApp.setImageResource(R.drawable.music_app);
        mAppName.setText("Music is Life");
    }

}
