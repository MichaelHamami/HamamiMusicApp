package com.hamami.hamamimusicapp.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hamami.hamamimusicapp.IMainActivity;
import com.hamami.hamamimusicapp.Models.Item;
import com.hamami.hamamimusicapp.R;
import com.hamami.hamamimusicapp.adapters.StorageRecyclerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StorageFragment extends Fragment implements StorageRecyclerAdapter.IStorageSelector
{
    private static final String TAG = "StorgeFragment";

    // UI Components
    private RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    List<Item> mDataset = new ArrayList<>();

    private IMainActivity mIMainActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_storage,container,false);
    }

    // called after onCreateView
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, "onViewCreated: called ");
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        mDataset.add(new Item(root.getName(),Uri.fromFile(root),root));
        File[] files = root.listFiles();
        for(File singleFile : files)
        {
            if(!singleFile.isHidden())
            {
                mDataset.add(new Item(singleFile.getName(), Uri.fromFile(singleFile),singleFile));
            }
        }
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new StorageRecyclerAdapter(getContext(),mDataset,this);
        mRecyclerView = view.findViewById(R.id.storage_recyclerview);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        RecyclerView.ItemDecoration divider = new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(divider);
        updateDataSet();

    }

    private void updateDataSet() {
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIMainActivity = (IMainActivity) getActivity();


    }
    public void showPopup(Item itemStorage, View view){
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.inflate(R.menu.storage_options_menu);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.setAsRoot:
                    Log.d(TAG, "onMenuItemClick: set as root menu clicked ");
                    mIMainActivity.setRootFolder(itemStorage.getFile());
                    return true;
                default:
                    return false;
            }
        });
        //displaying the popup
        popup.show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onFolderOptionSelected(Item item,View view) {
        Log.d(TAG, "onFolderOptionSelected: called , you clicked on menu");
        showPopup(item,view);
    }
}
