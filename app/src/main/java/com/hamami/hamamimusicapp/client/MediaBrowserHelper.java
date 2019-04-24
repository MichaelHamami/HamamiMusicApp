package com.hamami.hamamimusicapp.client;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.media.MediaBrowserServiceCompat;
import com.hamami.hamamimusicapp.util.MyPreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class MediaBrowserHelper {

    private static final String TAG = "MediaBrowserHelper";

    private final Context mContext;
    private final Class<? extends MediaBrowserServiceCompat> mMediaBrowserServiceClass;

    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;
    private MyPreferenceManager mMyPrefManager;

    private MediaBrowserConnectionCallback mMediaBrowserConnectionCallback;
    private MediaBrowserSubscriptionCallBack mMediaBrowserSubscriptionCallBack;
    private MediaControllerCallback mMediaControllerCallback;
    private MediaBrowserHelperCallback mMediaBrowserCallback;
    private boolean mWasConfigurationChanged;



    public MediaBrowserHelper(Context context, Class<? extends MediaBrowserServiceCompat> mediaBrowserServiceClass) {
        mContext = context;
        mMediaBrowserServiceClass = mediaBrowserServiceClass;

        mMediaBrowserConnectionCallback = new MediaBrowserConnectionCallback();
        mMediaBrowserSubscriptionCallBack = new MediaBrowserSubscriptionCallBack();
        mMediaControllerCallback = new MediaControllerCallback();
        mMyPrefManager = new MyPreferenceManager(context);
    }

    public void setMediaBrowserHelperCallback(MediaBrowserHelperCallback mediaBrowserHelperCallback)
    {
        mMediaBrowserCallback = mediaBrowserHelperCallback;
    }
    private class MediaControllerCallback extends MediaControllerCompat.Callback
    {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata)
        {
            Log.d(TAG, "onMetadataChanged: called.");

            if(mMediaBrowserCallback != null)
            {
                mMediaBrowserCallback.onMetadataChanged(metadata);
            }
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.d(TAG, "onPlaybackStateChanged: called.");

            if(mMediaBrowserCallback != null)
            {
                mMediaBrowserCallback.onPlayBackStateChanged(state);
            }

        }
        // This might happen if the MusicService is killed while the Activity is in the
        // foreground and onStart() has been called (but not onStop()).
        @Override
        public void onSessionDestroyed() {
            onPlaybackStateChanged(null);
        }
    }

    public void subscribeToNewPlaylist(String currentPlaylistId, String newPlaylistId){
        if(!currentPlaylistId.equals("")){
            Log.d(TAG, "subscribeToNewPlaylist:  unsubscribed ...");
            mMediaBrowser.unsubscribe(currentPlaylistId);
        }
        Log.d(TAG, "subscribeToNewPlaylist: playlistId is: "+newPlaylistId);
        mMediaBrowser.subscribe(newPlaylistId,mMediaBrowserSubscriptionCallBack);
    }
    public void addQueueItemFromPlaylist(MediaMetadataCompat mediaId)
    {
        Log.d(TAG, "addQueueItemFromPlaylist: Called we call controller to add: "+mediaId.getDescription().getMediaId());
        mMediaController.addQueueItem(mediaId.getDescription());

    }
    public void removeQueueItemFromPlaylist(MediaMetadataCompat mediaId)
    {
        Log.d(TAG, "removeQueueItemFromPlaylist: Called we call controller to remove");
        mMediaController.removeQueueItem(mediaId.getDescription());
    }

    public void setQueueIndex(int index) {
        Log.d(TAG, "setQueueIndex: called we actully called rewind");
        mMediaController.getTransportControls().rewind();
//        @SuppressLint("RestrictedApi") final IMediaSession extraBinder = mMediaController.getSessionToken().getExtraBinder();
//        extraBinder.rewind();
    }
    public void  setQueueItemsFromPlaylist(ArrayList<MediaMetadataCompat> mediaList)
    {
        for(int i=0;i<mMediaController.getQueue().size();i++)
        {
            mMediaController.removeQueueItem(mMediaController.getQueue().get(i).getDescription());
        }
        for(int j=0;j<mediaList.size();j++)
        {
            mMediaController.addQueueItem(mediaList.get(j).getDescription());
        }
    }

    public void onStart(boolean wasConfigurationChanged)
    {
        mWasConfigurationChanged = wasConfigurationChanged;
        if(mMediaBrowser == null)
        {
            mMediaBrowser = new MediaBrowserCompat(
                    mContext,
                    new ComponentName(mContext,mMediaBrowserServiceClass),
                    mMediaBrowserConnectionCallback,
                    null);

            mMediaBrowser.connect();
        }
        Log.d(TAG, "onStart: CALLED: Creating MediaBrowser, and connecting");
    }
    public void onStop()
    {
        if (mMediaController != null)
        {
            mMediaController.unregisterCallback(mMediaControllerCallback);
            mMediaController = null;
        }
        if(mMediaBrowser != null && mMediaBrowser.isConnected())
        {
            mMediaBrowser.disconnect();
            mMediaBrowser = null;
        }
        Log.d(TAG, "onStop: CALLED: Releasing MediaController, Disconnecting from MediaBrowser");
    }

    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback{
        // Happens as a result of onStart().
        @Override
        public void onConnected() {
            Log.d(TAG,"onConnected: called");

            try
            {
                // Get a MediaController for the MediaSession.
                mMediaController = new MediaControllerCompat(mContext,mMediaBrowser.getSessionToken());
                mMediaController.registerCallback(mMediaControllerCallback);


            }catch (RemoteException e)
            {
                Log.d(TAG,"onConnected: connection problem: "+e.toString());
                throw new RuntimeException(e);
            }
            mMediaBrowser.subscribe(mMediaBrowser.getRoot(),mMediaBrowserSubscriptionCallBack);
            Log.d(TAG,"onConnected: called: subscribing to: "+mMediaBrowser.getRoot());

            mMediaBrowserCallback.onMediaControllerConnected(mMediaController);

        }
    }

    public class MediaBrowserSubscriptionCallBack extends MediaBrowserCompat.SubscriptionCallback
    {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            Log.d(TAG,"onChildrenLoaded: called: "+parentId + ", "+  children.toString()+", size:"+children.size());


                for(final MediaBrowserCompat.MediaItem mediaItem : children)
                {
                    Log.d(TAG,"onChildrenLoaded: CALLED: queue item:" + mediaItem.getMediaId());
                    if(mMediaController.getMetadata() != null)
                    {
                        if(!mMediaController.getMetadata().containsKey(mediaItem.getDescription().getMediaId()))
                        {
                            mMediaController.addQueueItem(mediaItem.getDescription());
                        }
                    }
                    else
                    {
                        mMediaController.addQueueItem(mediaItem.getDescription());

                    }

//                    mMediaController.addQueueItem(mediaItem.getDescription());
                }
                mMediaController.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
        }
    }

    public MediaControllerCompat.TransportControls getTransportControls()
    {
        if(mMediaController == null)
        {
            Log.d(TAG,"getTransportControls: MediaController is null !");
            throw new IllegalStateException("Media Controller is null");
        }
        return mMediaController.getTransportControls();
    }

}
