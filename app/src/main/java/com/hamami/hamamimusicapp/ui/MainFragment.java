package com.hamami.hamamimusicapp.ui;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.hamami.hamamimusicapp.IMainActivity;
import com.hamami.hamamimusicapp.MainActivity;
import com.hamami.hamamimusicapp.Models.Playlist;
import com.hamami.hamamimusicapp.Models.Songs;
import com.hamami.hamamimusicapp.R;
import com.hamami.hamamimusicapp.adapters.ViewPagerAdapter;
import com.hamami.hamamimusicapp.persistence.PlaylistRepository;

import java.io.File;
import java.util.ArrayList;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    // layout
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    // vars
    private ViewPagerAdapter mViewPagerAdapter;

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public TabLayout getTabLayout() {
        return mTabLayout;
    }

    private IMainActivity mIMainActivity;
    // Repository object
    private PlaylistRepository mPlaylistRepository;
    private ArrayList<Playlist> mPlaylists;

//    public MainFragment(){}

    public static MainFragment newInstance(ArrayList<Playlist> array_playlist){
        Log.d(TAG, "MainFragment new Instance called!");
        MainFragment mainFragment = new MainFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("array_playlist",array_playlist);
        mainFragment.setArguments(args);
        return mainFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");
        if (getArguments() != null){
            Log.d(TAG, "playListFragment, OnCreate: try getArguments!");
            mPlaylists = getArguments().getParcelableArrayList("array_playlist");
            setRetainInstance(true);
        }
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView: called");
        return inflater.inflate(R.layout.fragment_main_viewpager,container,false);
    }

    // called after onCreateView
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, "onViewCreated: called");
        // trying something new
        mViewPager = ((MainActivity) getActivity()).mViewPagerActivity;
        mTabLayout = view.findViewById(R.id.tabLayout_main);
//        mTabLayout = ((MainActivity) getActivity()).mTabLayoutActivity;
        mViewPagerAdapter = ((MainActivity) getActivity()).mViewPagerAdapterActivity;

        if(mViewPager.getAdapter() == null)
        {
            mViewPager.setAdapter(mViewPagerAdapter);
        }
        Log.d(TAG, "onViewCreated: try setup viewpager");
        if(mViewPager.getVisibility() == View.GONE)
        {
            mViewPager.setVisibility(View.VISIBLE);
        }
//        mIMainActivity.setupViewPager(mViewPager);
        mTabLayout.setupWithViewPager(mViewPager);
//        mViewPager = view.findViewById(R.id.tabLayout);
//        mTabLayout = view.findViewById(R.id.tabLayout);

//        if(mPlaylists != null)
//        {
//            if (mPlaylists.size() ==0)
//            {
//                Log.d(TAG, "onViewCreated: problem !!!!");
//            }
//            else
//            {
//                Log.d(TAG, "onViewCreated: try setup viewpager");
//                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
////                    ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getFragmentManager());
//                    mViewPager.setAdapter(viewPagerAdapter);
//                mIMainActivity.setupViewPager(mViewPager);
////                setupViewPager();
//                Log.d(TAG, "onViewCreated: setup tablayout");
//                mTabLayout.setupWithViewPager(mViewPager);
//            }
//        }
    }

    private void setupViewPager() {
//        ViewPagerAdapter viewPagerAdapter = ((ViewPagerAdapter)((MainActivity)(getActivity())).mViewPager.getAdapter());
        ViewPagerAdapter viewPagerAdapter = ((ViewPagerAdapter)mViewPager.getAdapter());
        Log.d(TAG, "setupViewPager: the adpater is : "+viewPagerAdapter);
        if(mPlaylists.size() != 0)
        {
            boolean foundTitle = false;
            Log.d(TAG, "setupViewPager: We get playlist's from Database Size is:"+mPlaylists.size());
            for(int i = 0; i < mPlaylists.size();i++)
            {

                viewPagerAdapter.addFragment(PlaylistFragment.newInstance(mPlaylists.get(i),true),mPlaylists.get(i).getTitle());
            }
            viewPagerAdapter.notifyDataSetChanged();
        }
        else
        {
            Log.d(TAG, "setupViewPager: We get playlist from Storage");
            Playlist playlistFromStorage = retrivePlaylistFromStorage();
            viewPagerAdapter.addFragment(PlaylistFragment.newInstance(playlistFromStorage,false),playlistFromStorage.getTitle());
            ArrayList<Songs> sonlistinu = new ArrayList<>();
            sonlistinu.add(playlistFromStorage.getSongs().get(0));
            viewPagerAdapter.addFragment(PlaylistFragment.newInstance(new Playlist("Favorite",sonlistinu),false),"Favorite");
            viewPagerAdapter.notifyDataSetChanged();
        }
    }

    private Playlist retrivePlaylistFromStorage()
    {
        ArrayList<File> songsFiles = new ArrayList<>();
//        songsFiles =  findSongs(Environment.getExternalStorageDirectory());
        songsFiles =  findSongs(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        ArrayList<Songs> songsList = new ArrayList<>();
        for (int i = 0; i < songsFiles.size(); i++) {
            Songs song = new Songs(
                    songsFiles.get(i).getAbsolutePath(),
                    songsFiles.get(i).getName().replaceAll(" .mp3"," "),
                    getTimeSong(songsFiles.get(i))
            );
            songsList.add(song);
        }
        Playlist playlist = new Playlist("AllMusic",songsList);
        return playlist;
    }

    public ArrayList<File> findSongs(File root) {
        ArrayList<File> al = new ArrayList<File>();
        File[] files = root.listFiles();
        for (File singleFile : files) {
            if (singleFile.isDirectory() && !singleFile.isHidden()) {
                al.addAll(findSongs(singleFile));
            } else {
                if (singleFile.getName().endsWith(".mp3")) {
                    al.add(singleFile);
                }
            }
        }
        return al;
    }


    public String getTimeSong(File file) {
        // load data file
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(file.getAbsolutePath());

        String time;
        // convert duration to minute:seconds
        String duration =
                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        long dur = Long.parseLong(duration);
        String seconds = String.valueOf((dur % 60000) / 1000);

        String minutes = String.valueOf(dur / 60000);
        if (seconds.length() == 1) {
            time = "0" + minutes + ":0" + seconds;
        } else {
            time = "0" + minutes + ":" + seconds;
        }
        // close object
        metaRetriever.release();
        return time;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIMainActivity = (IMainActivity) getActivity();
    }
}
