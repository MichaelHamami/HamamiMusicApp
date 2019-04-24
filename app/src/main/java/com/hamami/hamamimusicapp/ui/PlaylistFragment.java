package com.hamami.hamamimusicapp.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hamami.hamamimusicapp.IMainActivity;
import com.hamami.hamamimusicapp.Models.Playlist;
import com.hamami.hamamimusicapp.Models.Songs;
import com.hamami.hamamimusicapp.R;
import com.hamami.hamamimusicapp.adapters.PlaylistRecyclerAdapter;
import com.hamami.hamamimusicapp.persistence.PlaylistRepository;

import java.io.File;
import java.util.ArrayList;

public class PlaylistFragment extends Fragment implements PlaylistRecyclerAdapter.IMediaSelector
{
    private static final String TAG = "PlaylistFragment";

    // UI Components
    private RecyclerView mRecyclerView;
    private ImageView mShuffle;

    //Vars
    // the title we will get from bundle
    private String mPlaylistTitle;
    private PlaylistRecyclerAdapter mAdapter;
    private ArrayList<MediaMetadataCompat> mMediaList = new ArrayList<>();
    private ArrayList<Songs> songsList = new ArrayList<>();
    private IMainActivity mIMainActivity;
    private MediaMetadataCompat mSelectedMedia;
    private boolean mIsPlaylistInDatabase;
    private Playlist mPlaylistFragment;

    // Repository
    private PlaylistRepository mPlaylistRepository;

//    public static PlaylistFragment newInstance(ArrayList<Songs> songsArray,String title){
public static PlaylistFragment newInstance(Playlist playlist,boolean isPlaylistInDatabase){
    Log.d(TAG, "playListFragment new Instance called!");
        PlaylistFragment playlistFragment = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("songLists",playlist.getSongs());
        args.putString("title",playlist.getTitle());
        args.putBoolean("isPlaylistInDatabase",isPlaylistInDatabase);
        playlistFragment.setArguments(args);
        return playlistFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called , sizeList:" +songsList.size());
        if (getArguments() != null){
            Log.d(TAG, "playListFragment, OnCreate: try getArguments! ");
            if (songsList.size() ==0)
            {
                Toast.makeText(getContext(),"we get arguments",Toast.LENGTH_LONG).show();
                songsList = getArguments().getParcelableArrayList("songLists");
                addToMediaList(songsList);
                mPlaylistTitle = getArguments().getString("title");
                mPlaylistFragment = new Playlist(mPlaylistTitle,songsList);
                mIsPlaylistInDatabase = getArguments().getBoolean("isPlaylistInDatabase");

                // mIsPlaylistInDatabase == false
                if(!mIsPlaylistInDatabase)
                {
                    savePlaylistToDatabase();

                }
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
        mShuffle = view.findViewById(R.id.shuffle);
//        mIMainActivity.setFirstShuffle();
        mShuffle.setOnClickListener(v -> {
            mIMainActivity.shufflePlayingPlaylist(mShuffle.isActivated());
            mShuffle.setActivated(!mShuffle.isActivated());
        });


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
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mAdapter = new PlaylistRecyclerAdapter(getActivity(),songsList,mMediaList,this);
            Log.d(TAG, "initRecyclerView: called , Song list size is:"+songsList.size()+" Title: " +mPlaylistTitle);
            mRecyclerView.setAdapter(mAdapter);

            updateDataSet();

    }

    private void updateDataSet() {
        mAdapter.notifyDataSetChanged();
        if(mIMainActivity.getMyPreferenceManager().getLastPlayedArtist().equals(mPlaylistTitle)){
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
    public void showPopup(final int postion, View view){
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.inflate(R.menu.options_menu);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.playMenu:
                    Log.d(TAG, "onMenuItemClick: play menu clicked ");
                    onMediaSelected(postion);
                    return true;
                case R.id.deleteMenu:
                    Log.d(TAG, "onMenuItemClick: delete menu  clicked ");
                    deleteSongFromList(postion);
                    return true;
                case R.id.addToPlaylistMenu:
                    Log.d(TAG, "onMenuItemClick: add to playlist menu clicked song:"+songsList.get(postion).getNameSong());
                    mIMainActivity.onAddPlaylistMenuSelected(songsList.get(postion));
                    return true;
                case R.id.addAsFavorite:
                    Log.d(TAG, "onMenuItemClick: add to Favorite menu  clicked ");
                    mIMainActivity.addSongToPlaylist(songsList.get(postion),"Favorite");
                    return true;
                case R.id.addToQueue:
                    Log.d(TAG, "onMenuItemClick: Add to queue menu  clicked ");
                    mIMainActivity.addSongToPlaylist(songsList.get(postion),"Queue");
                    return true;

                default:
                    return false;
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
    public   void addSongToList(Songs song)
    {
        // check for duplicates
        for(int i=0; i<songsList.size();i++)
        {
            if(songsList.get(i).getNameSong().equals(song.getNameSong()))
            {
                Toast.makeText(getContext(), "the song is already in the playlist ", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "addSongToList: the song is already in the playlist");
                return;
            }
        }
        Log.d(TAG, "addSongToList: we add the song");
        File file = new File(song.getFileSong());

        MediaMetadataCompat media = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,song.getNameSong())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,song.getNameSong())
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,file.toURI().toString())
                .build();
        mMediaList.add(media);
        songsList.add(song);
        Log.d(TAG, "addSongToList: songlist size after add is:"+songsList.size());
        // need to update database
        mIMainActivity.updateToDatabase(mPlaylistFragment);
        updateDataSet();
    }
    private   void deleteSongFromList(int position)
    {
        // need to be change just don't want to make crush
        if(songsList.size() == 1)
        {
          mIMainActivity.removePlaylistFromDatabase(mPlaylistFragment);
        }

        else
        {
            MediaMetadataCompat theMediaToRemove = mMediaList.get(position);
            songsList.remove(position);
            mMediaList.remove(position);
            mIMainActivity.removeSongFromQueueList(theMediaToRemove);
            mIMainActivity.updateToDatabase(mPlaylistFragment);
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
                        // title = songName , artist=songTime
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,songsList.get(i).getNameSong())
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE,songsList.get(i).getNameSong())
//                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,songsList.get(i).getSongLength())
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,file.toURI().toString())
//                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,songsList.get(i).getFileSong().toURI().toString())
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

    private  void savePlaylistToDatabase()
    {
        Log.d(TAG, "savePlaylistToDatabase: we try to save the playlist to the database");
        Log.d(TAG, "savePlaylistToDatabase: Title: "+mPlaylistTitle+" Songs size: "+songsList.size());
//        mPlaylistRepository.insertPlaylistTask(mPlaylistFragment);
        mIMainActivity.insertToDatabase(mPlaylistFragment);

//        ArrayList<String> playlistTitles = new ArrayList<>();
////        playlistTitles.addAll(mPlaylistRepository.getPlaylistTitles());
//        if( isThisNewPlaylist(mPlaylistFragment,playlistTitles) == true)
//        {
//            Log.d(TAG, "savePlaylistToDatabase: we insert new Playlist");
//            mPlaylistRepository.insertPlaylistTask(new Playlist(mPlaylistTitle,songsList));
//        }
//        else
//        {
//            Log.d(TAG, "savePlaylistToDatabase: this playlist :"+mPlaylistFragment.getTitle() +" are already in database");
//        }
    }
    public boolean isThisNewPlaylist(Playlist playlist,ArrayList<String> titles)
    {
        for(int i = 0; i<titles.size(); i++)
        {
            if(titles.get(i).equals(playlist.getTitle()))
            {
                return false;
            }
        }
        return true;
    }

}
