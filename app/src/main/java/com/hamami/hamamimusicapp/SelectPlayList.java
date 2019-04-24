package com.hamami.hamamimusicapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hamami.hamamimusicapp.Models.Songs;
import com.hamami.hamamimusicapp.adapters.SelectPlaylistRecyclerAdapter;
import com.hamami.hamamimusicapp.util.DialogCreateNewPlaylist;

import java.util.ArrayList;

public class SelectPlayList extends Fragment implements SelectPlaylistRecyclerAdapter.IPlaylistSelector, DialogCreateNewPlaylist.OnInputListener {

    private static final String TAG = "SelectPlayList";

    // UI Components
    private RecyclerView mRecyclerView;
    private Button mButtonCreate;

    // Interface To send data to mainActivity
    private IMainActivity mIMainActivity;

    //Vars
    private SelectPlaylistRecyclerAdapter mAdapter;
    private ArrayList<String> fragmentsTitles = new ArrayList<>();
    private Songs songSelected;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_selectplaylist,container,false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, "onViewCreated: called");
        initRecyclerView(view);
//        mRecyclerView = view.findViewById(R.id.reycler_view_selectPlaylist);
        mButtonCreate = view.findViewById(R.id.ButtonCreateNewPlaylist);

        mButtonCreate.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Called: opening dialog");
            openDialog();
        });
    }

    private void initRecyclerView(View view)
    {
        mRecyclerView = view.findViewById(R.id.reycler_view_selectPlaylist);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mAdapter = new SelectPlaylistRecyclerAdapter(getContext(),fragmentsTitles,this);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Called");

        if (getArguments() != null){

            fragmentsTitles = getArguments().getStringArrayList("playlistTitles");
            songSelected = getArguments().getParcelable("songSelected");
        }

    }

    private void openDialog()
    {
        DialogCreateNewPlaylist dialog = new DialogCreateNewPlaylist();
        dialog.show(getFragmentManager(),"DialogSelectPlaylist");
        dialog.setTargetFragment(SelectPlayList.this,1);
    }

    @Override
    public void onPlaylistSelected(int position)
    {
        Log.d(TAG, "onPlaylistSelected: Called");
        // send to mainActivity the method to work
        mIMainActivity.addSongToPlaylistFromSelectFragment(songSelected,fragmentsTitles.get(position));
    }

    // input from the Dialog
    @Override
    public void sendInput(String input)
    {
        Log.d(TAG, "sendInput: Got the input: "+input);
        // send the arguments to main activity
        mIMainActivity.addNewPlaylist(songSelected,input);
        Log.d(TAG, "sendInput: after addNewPlaylistCalled");
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container,getFragmentManager().findFragmentByTag(getString(R.string.fragment_main))).commit();

    }

    @Override
    public boolean isPlaylistExists(String playlist)
    {
        for(int i=0;i<fragmentsTitles.size();i++)
        {
            if(fragmentsTitles.get(i).equals(playlist))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIMainActivity = (IMainActivity) getActivity();
    }
}

