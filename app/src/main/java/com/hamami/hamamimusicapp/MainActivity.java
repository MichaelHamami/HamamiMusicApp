package com.hamami.hamamimusicapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.hamami.hamamimusicapp.adapters.ViewPagerAdapter;
import com.hamami.hamamimusicapp.client.MediaBrowserHelperCallback;
import com.hamami.hamamimusicapp.Models.Playlist;
import com.hamami.hamamimusicapp.Models.Songs;
import com.hamami.hamamimusicapp.adapters.CustomExpandableListView;
import com.hamami.hamamimusicapp.client.MediaBrowserHelper;
import com.hamami.hamamimusicapp.persistence.PlaylistRepository;
import com.hamami.hamamimusicapp.services.MediaService;
import com.hamami.hamamimusicapp.ui.AboutFragment;
import com.hamami.hamamimusicapp.ui.InformationFromDeveloperFragment;
import com.hamami.hamamimusicapp.ui.MainFragment;
import com.hamami.hamamimusicapp.ui.MediaControllerFragment;
import com.hamami.hamamimusicapp.ui.PlaylistFragment;
import com.hamami.hamamimusicapp.ui.QueueFragment;
import com.hamami.hamamimusicapp.ui.StorageFragment;
import com.hamami.hamamimusicapp.util.DialogReportToDeveloper;
import com.hamami.hamamimusicapp.util.MyPreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.hamami.hamamimusicapp.util.Constants.MEDIA_QUEUE_POSITION;
import static com.hamami.hamamimusicapp.util.Constants.QUEUE_NEW_PLAYLIST;
import static com.hamami.hamamimusicapp.util.Constants.SEEK_BAR_MAX;
import static com.hamami.hamamimusicapp.util.Constants.SEEK_BAR_PROGRESS;

@SuppressWarnings({"FieldCanBeLocal", "SpellCheckingInspection"})
public class MainActivity extends AppCompatActivity implements
        IMainActivity,
        MediaBrowserHelperCallback {
    // Tag for debug
    private static final String TAG = "MainActivity";

    // for permission
    private static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    // UI Components
    public ViewPager mViewPagerActivity;
    public ViewPagerAdapter mViewPagerAdapterActivity;
    public TabLayout mTabLayoutActivity;
    public Toolbar mToolbar;

    // FOR NavigationView
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
//    private String mActivityTitle;

    private ExpandableListView mExpandListView;
    private ExpandableListAdapter mExpandAdapter;
    private List<String> lstTitle;
    private Map<String,List<String>> lstChild;

    // Songs Vars
    ArrayList<Songs> songList = new ArrayList<>();

//    ArrayList<File> mySongs = new ArrayList<>();
    private ArrayList<MediaMetadataCompat> mMediaList = new ArrayList<>();
    private Songs songToAdd;

      // vars
      private MediaBrowserHelper mMediaBrowserHelper;
      private MyApplication mMyApplication;
      private MyPreferenceManager mMyPrefManager;
      private boolean mIsPlaying;
      private SeekBarBroadcastReceiver mSeekBarBroadcastReceiver;
      private UpdateUIBroadcastReceiver mUpdateUIBroadcastReceiver;
      private boolean mOnAppOpen;
      private boolean mWasConfigurationChanged = false;
//      private boolean isNewPlaylist;
//      private boolean mOnStartCalled;

      // Repository object
      private PlaylistRepository mPlaylistRepository;
      private ArrayList<Playlist> mPlaylists;

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = findViewById(R.id.toolbar);
        mDrawerLayout =  findViewById(R.id.drawer_layout);
//        mActivityTitle = getTitle().toString();
        mExpandListView = findViewById(R.id.nav_expended);

        genData();
        addDrawerItem();
        setupDrawer();
        mPlaylists = new ArrayList<>();
        mPlaylistRepository = new PlaylistRepository(this);



        verifyPermissions();

        new GetDataTask().execute();

        final Observer<List<Playlist>> playlistObserver = playlists -> {
            Log.d(TAG, "onChanged: called LiveData Work : FromDataBase");
            mPlaylists.clear();
            mPlaylists.addAll(playlists);

            if(mPlaylists.size() != mViewPagerAdapterActivity.getCount() || mPlaylists.size() == 0 )
            {
                addTheFragmentsFromDataBase();
            }
        };
        mPlaylistRepository.retrievePlaylistsTask().observe(this,playlistObserver);
        mPlaylistRepository.retrievePlaylistsTask();


        try {
            Log.d(TAG, "onCreate: we start thread now:");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mMyApplication = MyApplication.getInstance();
        mMyPrefManager = new MyPreferenceManager(this);

        mMediaBrowserHelper = new MediaBrowserHelper(this, MediaService.class);
        mMediaBrowserHelper.setMediaBrowserHelperCallback(this);

        mViewPagerActivity = findViewById(R.id.viewpager_activity_main);
        mTabLayoutActivity = findViewById(R.id.tabLayout_main);
        mViewPagerAdapterActivity = new ViewPagerAdapter(getSupportFragmentManager());
        setupViewPager();



//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.main_container,MainFragment.newInstance(mPlaylists)).commit();
        MainFragment mainFragment = MainFragment.newInstance(mPlaylists);
        doFragmentTransaction(mainFragment, getString(R.string.fragment_main), false);

    }
    private void doFragmentTransaction(Fragment fragment, String tag, boolean addToBackStack){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // if Viewpager is Un visible and we change to Main Fragment that include that pager
        if(tag.equalsIgnoreCase(getString(R.string.fragment_main)) && mViewPagerActivity.getVisibility() != View.VISIBLE)
        {
            Log.d(TAG, "doFragmentTransaction: setVisible");
            mViewPagerActivity.setVisibility(View.VISIBLE);
        }
        else
        {
            mViewPagerActivity.setVisibility(View.GONE);
        }
        transaction.replace(R.id.main_container, fragment, tag);
        if(addToBackStack){
            transaction.addToBackStack(tag);
        }
        transaction.commit();
    }


    private void genData() {
        List<String> title = Arrays.asList("Features " , "Help","About");
        List<String> childsFeature = Arrays.asList("Settings","Change Music Folder");
        List<String> childsHelp = Arrays.asList("User Guide","Report to Developer","How to use the app");
        List<String> childsAbout = Arrays.asList("About","Info");

        lstChild = new TreeMap<>();
        lstChild.put(title.get(0),childsFeature);
        lstChild.put(title.get(1),childsHelp);
        lstChild.put(title.get(2),childsAbout);

        lstTitle = new ArrayList<>(title);
    }

    private void addDrawerItem() {
        mExpandAdapter = new CustomExpandableListView(this,lstTitle,lstChild);
        mExpandListView.setAdapter(mExpandAdapter);
        mExpandListView.setOnGroupClickListener((parent, v, groupPosition, id) -> false);
        // Listview Group expanded listener
        mExpandListView.setOnGroupExpandListener(groupPosition -> {
        });

        // Listview Group collasped listener
        mExpandListView.setOnGroupCollapseListener(groupPosition -> {
        });
        mExpandListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String selectedItem = lstChild.get(lstTitle.get(groupPosition)).get(childPosition);
            Toast.makeText(getApplicationContext(),
                    selectedItem + " clicked",
                    Toast.LENGTH_SHORT).show();
            if(selectedItem.equalsIgnoreCase("Report to Developer"))
            {
                Log.d(TAG, "onChildClick: we try to openDialog");
                DialogReportToDeveloper dialog = new DialogReportToDeveloper();
                dialog.show(getSupportFragmentManager(),"DialogReportToDeveloper");

            }
            else if(selectedItem.equalsIgnoreCase("about"))
            {
                // todo people can stack it how many they want.....
                Log.d(TAG, "onChildClick: click on about");
                doFragmentTransaction(new AboutFragment(),"about",true);
            }
            else if(selectedItem.equalsIgnoreCase("Change Music Folder"))
            {
                Log.d(TAG, "onChildClick: click on Change Music Folder");
                doFragmentTransaction(new StorageFragment(),"storage",true);
            }
            else if(selectedItem.equalsIgnoreCase("info"))
            {
                Log.d(TAG, "onChildClick: click on info");
                doFragmentTransaction(new InformationFromDeveloperFragment(),"info",true);
            }
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return false;
        });
    }
    private void setupDrawer()
    {
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    private void addTheFragmentsFromDataBase()
    {
        boolean foundTitle = false;
        Log.d(TAG, "addTheFragmentsFromDataBase: We add playlist's  from Database");
        for(int i = 0; i < mPlaylists.size();i++)
        {
            for(int j=0; j<mViewPagerAdapterActivity.getFragmentTitles().size(); j++)
            {
                if(mPlaylists.get(i).getTitle().equals(mViewPagerAdapterActivity.getFragmentTitles().get(j)))
                {
                    foundTitle = true;
                }
            }
            // foundTitle != true
            if(!foundTitle)
            {
                mViewPagerAdapterActivity.addFragment(PlaylistFragment.newInstance(mPlaylists.get(i),true),mPlaylists.get(i).getTitle());
            }
            foundTitle = false;
        }
        mViewPagerAdapterActivity.notifyDataSetChanged();
    }

    @Override
    public void addNewPlaylist(Songs song, String playlistTitle) {
        addNewPlaylistAndSong(playlistTitle,song);
    }

    @Override
    public void addSongToPlaylistFromSelectFragment(Songs song, String playlistTitle)
    {
        Log.d(TAG, "addSongToPlaylistFromSelectFragment: try transaction");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container,getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_main))).commit();
        Log.d(TAG, "addSongToPlaylistFromSelectFragment: try add song");
        addSongToPlaylist(song,playlistTitle);
        
    }

    @Override
    public void setRootFolder(File rootFolder) {
        Log.d(TAG, "setRootFolder: called new Root is: "+rootFolder);
        mMyPrefManager.saveLastRootMediaFolder(rootFolder);
        int positionPlaylist = 0;
        for (int i= 0;  i<mPlaylists.size(); i++)
        {
            if (mPlaylists.get(i).getTitle().equalsIgnoreCase("AllMusic"))
            {
                mPlaylists.get(i).getSongs().clear();
                positionPlaylist = i;
            }
        }
        Playlist playlistFromStorage = retrivePlaylistFromStorage();
        for (int i= 0;  i<playlistFromStorage.getSongs().size(); i++)
        {
            mPlaylists.get(positionPlaylist).getSongs().add(playlistFromStorage.getSongs().get(i));
        }
        Log.d(TAG, "setRootFolder: all adds finished");
        mPlaylistRepository.updatePlaylistTask(mPlaylists.get(positionPlaylist));
        mViewPagerAdapterActivity.notifyDataSetChanged();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container,getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_main))).commit();
        
    }

    private void setupViewPager() {

        if(mPlaylists.size() != 0)
        {
//            boolean foundTitle = false;
            Log.d(TAG, "setupViewPager: We get playlist's from Database Size is:"+mPlaylists.size());
            for(int i = 0; i < mPlaylists.size();i++)
            {
                mViewPagerAdapterActivity.addFragment(PlaylistFragment.newInstance(mPlaylists.get(i),true),mPlaylists.get(i).getTitle());
            }
            mViewPagerAdapterActivity.notifyDataSetChanged();
        }
        else
        {
            Log.d(TAG, "setupViewPager: We get playlist from Storage");
            Playlist playlistFromStorage = retrivePlaylistFromStorage();
            mViewPagerAdapterActivity.addFragment(PlaylistFragment.newInstance(playlistFromStorage,false),playlistFromStorage.getTitle());
            ArrayList<Songs> sonlistinu = new ArrayList<>();
            sonlistinu.add(playlistFromStorage.getSongs().get(0));
            mViewPagerAdapterActivity.addFragment(PlaylistFragment.newInstance(new Playlist("Favorite",sonlistinu),false),"Favorite");
            mViewPagerAdapterActivity.notifyDataSetChanged();
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mWasConfigurationChanged = true;
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onMediaControllerConnected(MediaControllerCompat mediaController) {

        getMediaControllerFragment().getMediaSeekBar().setMediaController(mediaController);

    }
    @Override
    protected void onResume() {
        super.onResume();
        initSeekBarBroadcastReceiver();
        initUpdateUiBroadcastReceiver();
    }
    private void initSeekBarBroadcastReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.broadcast_seekbar_update));
        mSeekBarBroadcastReceiver = new SeekBarBroadcastReceiver();
        registerReceiver(mSeekBarBroadcastReceiver,intentFilter);

    }
    @Override
    protected void onPause() {
        super.onPause();
        if(mSeekBarBroadcastReceiver != null)
        {
            unregisterReceiver(mSeekBarBroadcastReceiver);
        }
        if(mUpdateUIBroadcastReceiver != null)
        {
            unregisterReceiver(mUpdateUIBroadcastReceiver);
        }
    }

    public void  removeSongFromQueueList(MediaMetadataCompat mediaId)
    {
        Log.d(TAG, "removeSongFromQueueList: called");
        mMyApplication.removeSongFromListMedia(mediaId);
        mMediaBrowserHelper.removeQueueItemFromPlaylist(mediaId);

    }

    @Override
    public void removePlaylistFragment(Playlist playlist)
    {
        Log.d(TAG, "removePlaylistFragment: we trying to remove");
        MainFragment mainFragment = (MainFragment)getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_main));
        ViewPager viewPagerMain = mainFragment.getViewPager();
        ViewPagerAdapter viewPagerAdapterMain = (ViewPagerAdapter)viewPagerMain.getAdapter();
        PlaylistFragment playlistFragment = (PlaylistFragment) viewPagerAdapterMain.getFragments().get(viewPagerAdapterMain.getItemPositionByTitle(playlist.getTitle()));
        Log.d(TAG, "removePlaylistFragment: the fragment is? :"+playlistFragment);
        viewPagerAdapterMain.removeFragment(playlistFragment,playlist.getTitle());
        viewPagerAdapterMain.notifyDataSetChanged();
        viewPagerMain.setCurrentItem(0);
    }

    @Override
    public void onFinishedDragInQueueFragment(ArrayList<MediaMetadataCompat> mediaList)
    {
        Log.d(TAG, "onFinishedDragInQueueFragment: called : songs size: "+mediaList.size());
        mMyApplication.setMediaItems(mediaList);

        String lastNameSong = mMyPrefManager.getLastPlayedMedia();
        int position = 0;
        for(int i= 0; i<mediaList.size();i++)
        {
            if(lastNameSong.equals(mediaList.get(i).getDescription().getMediaId()))
            {
                position = i;
            }
        }
        Log.d(TAG, "onFinishedDragInQueueFragment: the new position is : "+position);
        mMediaBrowserHelper.setQueueItemsFromPlaylist(mediaList);
        mMyPrefManager.saveQueuePosition(position);
        mMediaBrowserHelper.setQueueIndex(position);
    }

    @Override
    public void shufflePlayingPlaylist(boolean isShuffle)
    {
        Log.d(TAG, "shufflePlayingPlaylist: called we shuffle");
        // isShuffle == true
        if (isShuffle)
        {
            mMediaBrowserHelper.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
        }
        else
        {
            mMediaBrowserHelper.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
        }
    }

//    @Override
//    public void setFirstShuffle()
//    {
//        mMediaBrowserHelper.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
//    }

    @Override
    public void playPause()
    {
        Log.d(TAG, "playPause: called");
        if(mOnAppOpen)
        {
            if(mIsPlaying)
            {
                Log.d(TAG, "playPause: we try to pause");
                mMediaBrowserHelper.getTransportControls().pause();
            }
            else
            {
                // play song
                Log.d(TAG, "playPause: we call play song");
                mMediaBrowserHelper.getTransportControls().play();
            }
        }
        else
        {
            if(!getMyPreferenceManager().getPlaylistId().equals(""))
            {
                Log.d(TAG, "playPause: playlist is not null");
                onMediaSelected(
                getMyPreferenceManager().getPlaylistId(),
                        mMyApplication.getMediaItem(getMyPreferenceManager().getLastPlayedMedia()),
                        getMyPreferenceManager().getQueuePosition()
                );
            }
            else
            {
                Log.d(TAG, "playPause: selected something to play");
                Toast.makeText(this,"select something to play",Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void playNext()
    {
        Log.d(TAG, "playNext: called");
        if(mOnAppOpen)
        {
                Log.d(TAG, "playNext: we try to skip to next");
                mMediaBrowserHelper.getTransportControls().skipToNext();

        }
        else
        {
            if(!getMyPreferenceManager().getPlaylistId().equals(""))
            {
                Log.d(TAG, "playNext: playlist is not null");
                onMediaSelected(
                        getMyPreferenceManager().getPlaylistId(),
                        mMyApplication.getMediaItem(getMyPreferenceManager().getLastPlayedMedia()),
                        getMyPreferenceManager().getQueuePosition()
                );
            }
            else
            {
                Log.d(TAG, "playNext: selected something to play");
                Toast.makeText(this,"select something to play",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void playPrev()
    {
        Log.d(TAG, "playPrev: called");
        if(mOnAppOpen)
        {
            Log.d(TAG, "playPrev: we try to skip to previous");
            mMediaBrowserHelper.getTransportControls().skipToPrevious();

        }
        else
        {
            if(!getMyPreferenceManager().getPlaylistId().equals(""))
            {
                Log.d(TAG, "playPrev: playlist is not null");
                onMediaSelected(
                        getMyPreferenceManager().getPlaylistId(),
                        mMyApplication.getMediaItem(getMyPreferenceManager().getLastPlayedMedia()),
                        getMyPreferenceManager().getQueuePosition()
                );
            }
            else
            {
                Log.d(TAG, "playPrev: selected something to play");
                Toast.makeText(this,"select something to play",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public MyApplication getMyApplicationInstance() {
        return mMyApplication;
    }

    @Override
    public void onMediaSelected(String playlistId,MediaMetadataCompat mediaItem,int queuePosition)
    {
        if (mediaItem != null)
        {
            Log.d(TAG,"onMediaSelected: Called: "+mediaItem.getDescription().getMediaId());

            String currentPlaylistId = getMyPreferenceManager().getPlaylistId();
            Log.d(TAG, "onMediaSelected: currentPlaylistId is: "+currentPlaylistId +"||| compare with playlistId: "+playlistId);

            Bundle bundle = new Bundle();
            bundle.putInt(MEDIA_QUEUE_POSITION,queuePosition);

            if(playlistId.equals(currentPlaylistId))
            {
                if(mMyApplication.getMediaItems().isEmpty())
                {
                    Log.d(TAG, "onMediaSelected:  the list in myApplication is empty so we subscribe again.");
                    mMediaBrowserHelper.subscribeToNewPlaylist(currentPlaylistId,playlistId);
                }

                Log.d(TAG,"onMediaSelected: its same playlist and not empty: "+playlistId);
                mMediaBrowserHelper.getTransportControls().playFromMediaId(mediaItem.getDescription().getMediaId(),bundle);
            }
            else
            {
                Log.d(TAG,"onMediaSelected: its new playlist: "+playlistId);
                bundle.putBoolean(QUEUE_NEW_PLAYLIST,true);
                mMediaBrowserHelper.subscribeToNewPlaylist(currentPlaylistId,playlistId);
                mMediaBrowserHelper.getTransportControls().playFromMediaId(mediaItem.getDescription().getMediaId(),bundle);

            }
            mOnAppOpen = true;
        }
        else
        {
            Log.d(TAG, "onMediaSelected: select something to play");
            Toast.makeText(this,"select something to play",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAddPlaylistMenuSelected(Songs songSelected)
    {
        Log.d(TAG, "onAddPlaylistMenuSelected: Called with Song: SongName: "+songSelected.getNameSong()+" | Song FilePath: "+songSelected.getFileSong());
        MainFragment mainFragment = (MainFragment)getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_main));
        ViewPager viewPagerMain = mainFragment.getViewPager();
        ViewPagerAdapter viewPagerAdapterMain = (ViewPagerAdapter)viewPagerMain.getAdapter();

        songToAdd = songSelected;
        Log.d(TAG, "onAddPlaylistMenuSelected: Song: SongName: "+songToAdd.getNameSong()+" | Song FilePath: "+songToAdd.getFileSong());

        Bundle bundle = new Bundle();
        ArrayList<String> fragmentTitles = viewPagerAdapterMain.getFragmentTitles();
        bundle.putStringArrayList("playlistTitles", fragmentTitles );
        bundle.putParcelable("songSelected",songSelected);
        SelectPlayList selectPlayListFragment = new SelectPlayList();
        selectPlayListFragment.setArguments(bundle);

        // make transaction
        mViewPagerActivity.setVisibility(View.GONE);
        doFragmentTransaction(selectPlayListFragment,"select_fragment",true);
    }


    @Override
    public void addSongToPlaylist(Songs song, String playlistTitle) {
        MainFragment mainFragment = (MainFragment)getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_main));
        ViewPager viewPagerLoc = mainFragment.getViewPager();
//        TabLayout tabLayout = mainFragment.getTabLayout();
        ViewPagerAdapter viewPagerAdapterMain = (ViewPagerAdapter)viewPagerLoc.getAdapter();

//        int position = viewPagerAdapterMain.getItemPositionByTitle(playlistTitle);
        Log.d(TAG, "addSongToPlaylist: Called");
        if(playlistTitle.equals("Queue"))
        {
            Log.d(TAG, "addSongToPlaylist: we try add to queue list");
            if(viewPagerAdapterMain.getItemPositionByTitle(playlistTitle) != -1)
            {
//                viewPagerLoc.setCurrentItem(position);
                ((QueueFragment)(viewPagerAdapterMain.getItemByTitle(playlistTitle))).addSongToList(song);
            }
            else
            {
                // need to code here or test it.
                ArrayList<Songs> newSongList = new ArrayList<>();
                newSongList.add(song);
                viewPagerAdapterMain.addFragment(QueueFragment.newInstance(new Playlist(playlistTitle,newSongList)),playlistTitle);
                viewPagerAdapterMain.notifyDataSetChanged();

            }

        }
        else
        {
            Log.d(TAG, "addSongToPlaylist: add song to not new playlist");
//            viewPagerLoc.setCurrentItem(position);
            ((PlaylistFragment)(viewPagerAdapterMain.getItemByTitle(playlistTitle))).addSongToList(song);
        }
        Log.d(TAG, "addSongToPlaylist: The Song: "+song);
        File file = new File(song.getFileSong());

        MediaMetadataCompat media = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,song.getNameSong())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,song.getNameSong())
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,file.toURI().toString())
                .build();
//        mMediaBrowserHelper.onStart(mWasConfigurationChanged);
        mMediaBrowserHelper.addQueueItemFromPlaylist(media);
        int position = viewPagerAdapterMain.getItemPositionByTitle(playlistTitle);
        viewPagerLoc.setCurrentItem(position);
    }


    public void addNewPlaylistAndSong(String newPlaylist,Songs song)
    {
        Log.d(TAG, "addNewPlaylist: called");
        MainFragment mainFragment = (MainFragment)getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_main));
        ViewPager viewPagerMain = mainFragment.getViewPager();
        ViewPagerAdapter viewPagerAdapterMain = (ViewPagerAdapter)viewPagerMain.getAdapter();

        ArrayList<Songs> songNewList = new ArrayList<>();
        songNewList.add(song);
//        viewPagerAdapter.addFragment(PlaylistFragment.newInstance(songNewList,newPlaylist),newPlaylist);
        viewPagerAdapterMain.addFragment(PlaylistFragment.newInstance(new Playlist(newPlaylist,songNewList),false),newPlaylist);
        viewPagerAdapterMain.notifyDataSetChanged();
        int position = viewPagerAdapterMain.getItemPositionByTitle(newPlaylist);
        viewPagerMain.setCurrentItem(position);
    }


    @Override

    public MyPreferenceManager getMyPreferenceManager() {
        return mMyPrefManager;
    }

    @Override
    public void insertToDatabase(Playlist playlist)
    {
//        ArrayList<String> playlistTitles = new ArrayList<>();
//        playlistTitles.addAll(mPlaylistRepository.getPlaylistTitles());
        Log.d(TAG, "savePlaylistToDatabase: we try to save the playlist to the database");
        Log.d(TAG, "savePlaylistToDatabase: Title: "+playlist.getTitle()+" Songs size: "+playlist.getSongs().size());
        // isThisNewPlaylist(playlist,playlistTitles) == true
        if(isThisNewPlaylist(playlist))
        {
            Log.d(TAG, "insertToDatabase: we insert new Playlist");
            mPlaylistRepository.insertPlaylistTask(playlist);
        }
        else
        {
            Log.d(TAG, "insertToDatabase: this playlist :"+playlist.getTitle() +" are already in database");
        }

    }

    @Override
    public void updateToDatabase(Playlist playlist)
    {
        mPlaylistRepository.updatePlaylistTask(playlist);
    }

    @Override
    public void removePlaylistFromDatabase(Playlist playlist) 
    {
        Log.d(TAG, "removePlaylistFromDatabase: we trying to remove");
        MainFragment mainFragment = (MainFragment)getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_main));
        ViewPager viewPagerMain = mainFragment.getViewPager();
        ViewPagerAdapter viewPagerAdapterMain = (ViewPagerAdapter)viewPagerMain.getAdapter();

        PlaylistFragment playlistFragment = (PlaylistFragment) viewPagerAdapterMain.getFragments().get(viewPagerAdapterMain.getItemPositionByTitle(playlist.getTitle()));
        Log.d(TAG, "removePlaylistFromDatabase: the fragment is? :"+playlistFragment);
        mPlaylistRepository.deletePlaylist(playlist);
        viewPagerAdapterMain.removeFragment(playlistFragment,playlist.getTitle());
        viewPagerAdapterMain.notifyDataSetChanged();
        viewPagerMain.setCurrentItem(0);
    }


    @Override
    protected void onStart() {
        // when the app started
        Log.d(TAG, "onStart: Called");
        super.onStart();
        Log.d(TAG, "onStart: Called after super");
        if (!getMyPreferenceManager().getPlaylistId().equals(""))
        {
            preparedLastPlayedMedia();
        }
        else
        {
            Log.d(TAG, "onStart: else called will do MediaBrowser onStart");
            mMediaBrowserHelper.onStart(mWasConfigurationChanged);
        }
    }

    private void preparedLastPlayedMedia()
    {
        Log.d(TAG, "preparedLastPlayedMedia: Called");

        String lastPlaylist = getMyPreferenceManager().getPlaylistId();
        Log.d(TAG, "preparedLastPlayedMedia: lastPlayed is: "+lastPlaylist);
        int position = -1;
        for ( int i =0; i<mPlaylists.size(); i++)
        {
            if(lastPlaylist.equals(mPlaylists.get(i).getTitle()))
            {
               position = i;
                break;
            }
        }
        Log.d(TAG, "preparedLastPlayedMedia: the positin is: "+position);

        if(position != -1)
        {
            songList = mPlaylists.get(position).getSongs();
            Log.d(TAG, "preparedLastPlayedMedia: songList size: "+songList.size());
            addToMediaList(songList);
            onFinishedGettingPreviousSessionData(mMediaList);
        }
        else
        {
            Log.d(TAG, "preparedLastPlayedMedia: else called will do MediaBrowser onStart");
            mMediaBrowserHelper.onStart(mWasConfigurationChanged);
        }

//        Log.d(TAG, "preparedLastPlayedMedia: Size of songlist:  " +songList.size() + " MediaList size: "+mMediaList.size());

//        for(int i = 0; i<songList.size(); i++)
//        {
//            if(mMediaList.get(i).getDescription().getMediaId().equals(getMyPreferenceManager().getLastPlayedMedia())){
//                getMediaControllerFragment().setMediaTitle(mMediaList.get(i));
//            }
//        }
//        onFinishedGettingPreviousSessionData(mMediaList);

    }
    private void onFinishedGettingPreviousSessionData(List<MediaMetadataCompat> mediaItems){
        mMyApplication.setMediaItems(mediaItems);
        mMediaBrowserHelper.onStart(mWasConfigurationChanged);

    }

    @Override
    protected void onStop() {
        super.onStop();
        getMediaControllerFragment().getMediaSeekBar().disconnectController();
        mMediaBrowserHelper.onStop();
    }

    private Playlist retrivePlaylistFromStorage()
    {
        ArrayList<File> songsFiles;
//        songsFiles =  findSongs(Environment.getExternalStorageDirectory());
        if(mMyPrefManager.getLastRootMediaFolder().equals(""))
        {
            songsFiles =  findSongs(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        }
        else 
        {
            songsFiles = findSongs(new File(mMyPrefManager.getLastRootMediaFolder()));
        }
        
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
        ArrayList<File> al = new ArrayList<>();
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

    private void verifyPermissions(){
        Log.d(TAG, "verifyPermissions: Checking Permissions.");


        int permissionExternalMemory = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionExternalMemory != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    STORAGE_PERMISSIONS,
                    1
            );
        }
    }
    private void initUpdateUiBroadcastReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.broadcast_update_ui));
        mUpdateUIBroadcastReceiver = new UpdateUIBroadcastReceiver();
        registerReceiver(mUpdateUIBroadcastReceiver,intentFilter);

    }
    private class UpdateUIBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            MainFragment mainFragment = (MainFragment)getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_main));
            TabLayout tabLayout = mainFragment.getTabLayout();
            ViewPager viewPager = mainFragment.getViewPager();
            ViewPagerAdapter viewPagerAdapterMain = (ViewPagerAdapter)viewPager.getAdapter();

            String mediaID = intent.getStringExtra(getString(R.string.broadcast_new_media_id));
            Log.d(TAG, "onReceive:  media id: "+mediaID);
           int fragmentPosition = tabLayout.getSelectedTabPosition();
            Log.d(TAG, "onReceive: position: "+fragmentPosition);
            String title = viewPagerAdapterMain.getPageTitle(fragmentPosition).toString();
            Log.d(TAG, "onReceive: title? :"+title);
            if(title.equalsIgnoreCase("queue"))
            {
                ((QueueFragment) (viewPagerAdapterMain.getItem(fragmentPosition))).updateUI(mMyApplication.getMediaItem(mediaID));

            }
           else
           {
               ((PlaylistFragment) (viewPagerAdapterMain.getItem(fragmentPosition))).updateUI(mMyApplication.getMediaItem(mediaID));
           }
        }
    }

    private class SeekBarBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            long seekProgress = intent.getLongExtra(SEEK_BAR_PROGRESS,0);
            long maxProgress = intent.getLongExtra(SEEK_BAR_MAX,0);
            if (!getMediaControllerFragment().getMediaSeekBar().isTracking())
            {
                getMediaControllerFragment().getMediaSeekBar().setProgress((int)seekProgress);
                getMediaControllerFragment().getMediaSeekBar().setMax((int)maxProgress);
            }
        }
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        Log.d(TAG, "onMetadataChanged: called");
        // Do stuff with new metaData

        if(metadata == null)
        {
            return;
        }
        if (getMediaControllerFragment() != null)
        {
            getMediaControllerFragment().setMediaTitle(metadata);
        }

    }

    @Override
    public void onPlayBackStateChanged(PlaybackStateCompat state)
    {
        Log.d(TAG, "onPlayBackStateChanged: called");
        mIsPlaying = state != null && state.getState() == PlaybackStateCompat.STATE_PLAYING;

        // Update UI
        if (getMediaControllerFragment() != null)
        {
            getMediaControllerFragment().setIsPlaying(mIsPlaying);
        }
    }


    private MediaControllerFragment getMediaControllerFragment()
    {
        MediaControllerFragment mediaControllerFragment = (MediaControllerFragment)getSupportFragmentManager()
                .findFragmentById(R.id.bottom_media_controller);
        if (mediaControllerFragment != null)
        {
            return mediaControllerFragment;
        }
        return null;

    }

    private void addToMediaList(ArrayList<Songs> songsList)
    {
        mMediaList.clear();
        for (int i=0;i<songsList.size();i++)
        {
            // for the new Songs Type
            File file = new File(songsList.get(i).getFileSong());
            MediaMetadataCompat media = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,songsList.get(i).getNameSong())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE,songsList.get(i).getNameSong())
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,file.toURI().toString())
                    .build();
            mMediaList.add(media);
        }
    }

    public boolean isThisNewPlaylist(Playlist playlist)
    {
        Log.d(TAG, "isThisNewPlaylist: for checks size:" +mPlaylists.size());
        for(int i = 0; i<mPlaylists.size(); i++)
        {
            if(mPlaylists.get(i).getTitle().equals(playlist.getTitle()))
            {
                return false;
            }
        }
        return true;
    }


    /**
     * Creating Get Data Task for Getting Data From Web
     */
    @SuppressLint("StaticFieldLeak")
    public  class  GetDataTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;
        int jIndex;
        int x;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            x=mPlaylists.size();

            if(x==0)
                jIndex=0;
            else
                jIndex=x;

            dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle("Wait plaease");
            dialog.setMessage("we getting data from data base");
            dialog.show();
        }

        @Nullable
        @Override
        protected Void doInBackground(Void... params) {

            //Getting data from database
            Log.d(TAG, "doInBackground: trying to get playlist from database in GetDataTasak");
            mPlaylists.addAll(mPlaylistRepository.getPlaylistAsArrayList());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            if(mPlaylists.size() == 0)
            {
                Snackbar.make(findViewById(R.id.main_layout),"no Data from database", Snackbar.LENGTH_LONG).show();

            }
        }
    }
}
