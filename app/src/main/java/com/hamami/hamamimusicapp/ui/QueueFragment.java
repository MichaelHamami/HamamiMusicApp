package com.hamami.hamamimusicapp.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hamami.hamamimusicapp.IMainActivity;
import com.hamami.hamamimusicapp.Models.Playlist;
import com.hamami.hamamimusicapp.Models.Songs;
import com.hamami.hamamimusicapp.R;
import com.hamami.hamamimusicapp.adapters.QueuePlaylistRecyclerAdapter;
import com.hamami.hamamimusicapp.util.SimpleItemTouchHelperCallback;

import java.io.File;
import java.util.ArrayList;

public class QueueFragment extends Fragment implements QueuePlaylistRecyclerAdapter.IMediaSelector
{
    private static final String TAG = "QueueFragment";

    // UI Components
    private RecyclerView mRecyclerView;

    //Vars
    // the title we will get from bundle
    private String mPlaylistTitle;
    private QueuePlaylistRecyclerAdapter mAdapter;
    private ArrayList<MediaMetadataCompat> mMediaList = new ArrayList<>();
    private ArrayList<Songs> mSongsList = new ArrayList<>();
    private IMainActivity mIMainActivity;
    private MediaMetadataCompat mSelectedMedia;
    private boolean mIsPlaylistInDatabase;
    private Playlist mPlaylistFragment;

    // Touch Helper
//    private PlaylistRepository mPlaylistRepository;
    private ItemTouchHelper mItemTouchHelper;

//    public static PlaylistFragment newInstance(ArrayList<Songs> songsArray,String title){
public static QueueFragment newInstance(Playlist playlist){
    Log.d(TAG, "QueueFragment new Instance called!");
        QueueFragment queueFragment = new QueueFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("songLists",playlist.getSongs());
        args.putString("title",playlist.getTitle());
        queueFragment.setArguments(args);
        return queueFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            Log.d(TAG, "playListFragment, OnCreate: try getArguments!");
            if (mSongsList.size() ==0)
            {
                Toast.makeText(getContext(),"we get arguments",Toast.LENGTH_LONG).show();
                mSongsList = getArguments().getParcelableArrayList("songLists");
                addToMediaList(mSongsList);
                mPlaylistTitle = getArguments().getString("title");
                mPlaylistFragment = new Playlist(mPlaylistTitle,mSongsList);
            }
            setRetainInstance(true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_playlist,container,false);
    }

    // called after onCreateView
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        initRecyclerView(view);

        if(savedInstanceState != null)
        {
            mAdapter.setSelectedIndex(savedInstanceState.getInt("selected_index"));
        }

    }
    private void getSelectedMediaItem(String mediaId)
    {
        for(MediaMetadataCompat mediaItem: mMediaList)
        {
            if(mediaItem.getDescription().getMediaId().equals(mediaId))
            {
                mSelectedMedia = mediaItem;
                mAdapter.setSelectedIndex(mAdapter.getIndexOfItem(mSelectedMedia));
                break;
            }
        }
    }

    public int getSelectedIndex()
    {
       return mAdapter.getSelectedIndex();
    }

    private void initRecyclerView(View view)
    {
            mRecyclerView = view.findViewById(R.id.reycler_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mAdapter = new QueuePlaylistRecyclerAdapter(getActivity(),mSongsList,mMediaList,this);
            Log.d(TAG, "initRecyclerView: called , Song list size is:"+mSongsList.size()+" and MediaList size is:" +mMediaList.size());
            mRecyclerView.setAdapter(mAdapter);

            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
            mItemTouchHelper = new ItemTouchHelper(callback);
            mItemTouchHelper.attachToRecyclerView(mRecyclerView);


            updateDataSet();

    }

    private void updateDataSet() {
        mAdapter.notifyDataSetChanged();
        if(mIMainActivity.getMyPreferenceManager().getLastPlayedArtist().equals(mPlaylistTitle))
        {
            getSelectedMediaItem(mIMainActivity.getMyPreferenceManager().getLastPlayedMedia());
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIMainActivity = (IMainActivity) getActivity();
    }

    @Override
    public void onMediaSelected(int position)
    {
        Log.d(TAG, "onSongSelected: list item is clicked! +List size is: "+mMediaList.size());
        mIMainActivity.getMyApplicationInstance().setMediaItems(mMediaList);
        mSelectedMedia = mMediaList.get(position);
        mAdapter.setSelectedIndex(position);
        mIMainActivity.onMediaSelected(mPlaylistTitle,mSelectedMedia,position);
        saveLastPlayedSongProperties();

    }

    @Override
    public void onSongOptionSelected(int position,View view)
    {
        Log.d(TAG, "onSongOptionSelected: you clicked on menu good job");
        showPopup(position,view);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder)
    {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onFinishedDrag(ArrayList<Songs> songsList, ArrayList<MediaMetadataCompat> mediaList)
    {
        Log.d(TAG, "onFinishedDrag: Song list we get size is: "+songsList.size()+" and MediaList we get size is: " +mediaList.size());
//        mPlaylistFragment.getSongs().clear();
//        mMediaList.clear();
//        mSongsList.clear();
//        mMediaList.addAll(mediaList);
//        mSongsList.addAll(songsList);
//        mPlaylistFragment.setSongs(songsList);
//        mAdapter.notifyDataSetChanged();
        mIMainActivity.onFinishedDragInQueueFragment(mediaList);
    }

    public void showPopup(final int postion, View view){
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.inflate(R.menu.queue_options_menu);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.playMenu:
                        Toast.makeText(getContext(), "play menu clicked", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onMenuItemClick: play menu clicked ");
                        onMediaSelected(postion);
                        return true;
                    case R.id.deleteMenu:
                        Log.d(TAG, "onMenuItemClick: delete menu  clicked ");
                        Toast.makeText(getContext(), "delete menu  clicked", Toast.LENGTH_SHORT).show();
                        deleteSongFromList(postion);
                        return true;
                    case R.id.addAsFavorite:
                        Log.d(TAG, "onMenuItemClick: add to Favorite menu  clicked ");
                        Toast.makeText(getContext(), "add to Favorite menu  clicked", Toast.LENGTH_SHORT).show();
                        mIMainActivity.addSongToPlaylist(mSongsList.get(postion),"Favorite");
                        return true;

                    default:
                        return false;
                }
            }
        });
        //displaying the popup
        popup.show();
    }

    public void updateUI(MediaMetadataCompat mediaItem)
    {
        mAdapter.setSelectedIndex(mAdapter.getIndexOfItem(mediaItem));
        mSelectedMedia = mediaItem;
        saveLastPlayedSongProperties();
    }
    public  void addSongToList(Songs song)
    {
        // check for duplicates
        for(int i=0; i<mSongsList.size();i++)
        {
            if(mSongsList.get(i).getNameSong().equals(song.getNameSong()))
            {
                Toast.makeText(getContext(), "the song is already in the playlist ", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "addSongToList: the song is already in the playlist");
                return;
            }
        }
        File file = new File(song.getFileSong());

        MediaMetadataCompat media = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,song.getNameSong())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,song.getNameSong())
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,file.toURI().toString())
                .build();
        mMediaList.add(media);
        mSongsList.add(song);
        mAdapter.notifyItemInserted(mSongsList.size());
//        updateDataSet();
    }
    private   void deleteSongFromList(int position)
    {
        // need to be change just don't want to make crush
        if(mSongsList.size() == 1)
        {
          mIMainActivity.removePlaylistFragment(mPlaylistFragment);
        }
        else
        {
            MediaMetadataCompat theMediaToRemove = mMediaList.get(position);
            mSongsList.remove(position);
            mMediaList.remove(position);
            mIMainActivity.removeSongFromQueueList(theMediaToRemove);
            updateDataSet();
        }
    }

    private void addToMediaList(ArrayList<Songs> songsList)
    {
        for (int i=0;i<songsList.size();i++)
        {
            Log.d(TAG, "addToMediaList: pathSong:"+songsList.get(i).getFileSong());
            Log.d(TAG, "addToMediaList: the songName: "+songsList.get(i).getNameSong() +" position in list: "+i);
            if(songsList.get(i).getFileSong() != null)
            {
                File file = new File(songsList.get(i).getFileSong());
                MediaMetadataCompat media = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,songsList.get(i).getNameSong())
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE,songsList.get(i).getNameSong())
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,file.toURI().toString())
                        .build();
                mMediaList.add(media);
            }
        }
    }
    private void saveLastPlayedSongProperties()
    {
        // title is like the artist in mitch project
        mIMainActivity.getMyPreferenceManager().savePlaylistId(mPlaylistTitle);
        mIMainActivity.getMyPreferenceManager().saveLastPlayedArtist(mPlaylistTitle);
        mIMainActivity.getMyPreferenceManager().saveLastPlayedMedia(mSelectedMedia.getDescription().getMediaId());

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_index",mAdapter.getSelectedIndex());
    }
}
