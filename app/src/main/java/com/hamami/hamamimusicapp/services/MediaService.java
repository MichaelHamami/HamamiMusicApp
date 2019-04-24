package com.hamami.hamamimusicapp.services;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;

import com.hamami.hamamimusicapp.MyApplication;
import com.hamami.hamamimusicapp.R;
import com.hamami.hamamimusicapp.notifications.MediaNotificationManager;
import com.hamami.hamamimusicapp.players.MediaPlayerAdapter;
import com.hamami.hamamimusicapp.players.PlaybackInfoListener;
import com.hamami.hamamimusicapp.players.PlayerAdapter;
import com.hamami.hamamimusicapp.util.MyPreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.hamami.hamamimusicapp.util.Constants.MEDIA_QUEUE_POSITION;
import static com.hamami.hamamimusicapp.util.Constants.QUEUE_NEW_PLAYLIST;
import static com.hamami.hamamimusicapp.util.Constants.SEEK_BAR_MAX;
import static com.hamami.hamamimusicapp.util.Constants.SEEK_BAR_PROGRESS;

public class MediaService extends MediaBrowserServiceCompat
{

    private static final String TAG = "MediaService";

    private MediaSessionCompat mSession;
    private PlayerAdapter mPlayback;
    private MyApplication mMyApplication;
    private MyPreferenceManager mMyPrefManager;
    private MediaNotificationManager mMediaNotificationManager;
    private boolean mIsServiceStarted;

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"onCreate: Called");
        mMyApplication = MyApplication.getInstance();
        mMyPrefManager = new MyPreferenceManager(this);
        //Build the MediaSession
        mSession = new MediaSessionCompat(this,TAG);
        mSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS
        );
        mSession.setCallback(new MediaSessionCallback());

        // A token that can be used to create a MediaController for this session
        setSessionToken(mSession.getSessionToken());
//        mSession.getController().get
        mPlayback = new MediaPlayerAdapter(this,new MediaPlayerListener());
        mMediaNotificationManager = new MediaNotificationManager(this);
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG,"onTaskRemoved: stopped");
        super.onTaskRemoved(rootIntent);
        mPlayback.stop();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSession.release();
        Log.d(TAG,"onDestroy: MediaSession released");
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints)
    {
        Log.d(TAG,"onGetRoot: called");
        if(clientPackageName.equals(getApplicationContext().getPackageName())){
            //allowed to browser media
            Log.d(TAG, "onGetRoot: get media from some real playlist");
            return new BrowserRoot("some_real_playlist",null);
        }
        Log.d(TAG, "onGetRoot: no media");
        return new BrowserRoot("empty_media",null);

    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result)
    {
        Log.d(TAG,"onLoadChildren: called parentId: " +parentId +", result list:  "+ result);
        if(TextUtils.equals("empty_media",parentId)){
            result.sendResult(null);
            return;
        }
        // get the media items and sent it to MediaBrowserSubscriptionCallback ... then goes to onChildrenLoaded to add the items. to the list.
        Log.d(TAG, "onLoadChildren: list can be loaded ? size: "+ mMyApplication.getMediaItems().size());
        result.sendResult(mMyApplication.getMediaItems());
    }

    public class MediaSessionCallback extends MediaSessionCompat.Callback
    {
//        private final HashSet<MediaSessionCompat.QueueItem> mSetPlaylist = new HashSet<>();
        private final List<MediaSessionCompat.QueueItem> mPlaylist = new ArrayList<>();
        private int mQueueIndex = -1;
        private MediaMetadataCompat mPreparedMedia;
        private boolean mIsShuffle = false;

        private void resetPlaylist(){
//            mSetPlaylist.clear();
            mPlaylist.clear();
            mQueueIndex = -1;
        }
        public void setQueueIndex(int queueIndex)
        {
            mQueueIndex = queueIndex;
        }

        @Override
        public void onRewind() {
            Log.d(TAG, "onRewind: but we not doing this we set the QueueIndex");
            mQueueIndex = mMyPrefManager.getQueuePosition();
            Log.d(TAG, "onRewind: queueu index set to: "+mQueueIndex);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.d(TAG,"onPlayFromMediaId: Called");

            if(extras.getBoolean(QUEUE_NEW_PLAYLIST,false))
            {
                Log.d(TAG, "onPlayFromMediaId: reset playlist");
                resetPlaylist();
            }
            mPreparedMedia = mMyApplication.getMediaItem(mediaId);
            mSession.setMetadata(mPreparedMedia);
            if (!mSession.isActive())
            {
                mSession.setActive(true);
            }

            mPlayback.playFromMedia(mPreparedMedia);
            int newQueuePosition = extras.getInt(MEDIA_QUEUE_POSITION,-1);
            if(newQueuePosition == -1)
            {
                mQueueIndex++;
            }
            else
            {
                // this was something before last check Mediaservice 29.3
                mQueueIndex = newQueuePosition;
            }
            mMyPrefManager.saveQueuePosition(mQueueIndex);
            mMyPrefManager.saveLastPlayedMedia(mPreparedMedia.getDescription().getMediaId());


        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            if(shuffleMode == 1)
            {
                mIsShuffle = true;
                Log.d(TAG, "onSetShuffleMode: isShuffle? : "+mIsShuffle);
                return;
            }
            mIsShuffle = false;
            Log.d(TAG, "onSetShuffleMode: isShuffle? : "+mIsShuffle);
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            Log.d(TAG,"onAddQueueItem: called: Size list: "+mPlaylist.size());
//            mSetPlaylist.add(new MediaSessionCompat.QueueItem(description,description.hashCode()));
            for(int i=0;i<mPlaylist.size();i++)
            {
                Log.d(TAG, "onAddQueueItem: we compare" +mPlaylist.get(i).getDescription().getMediaId() + " || "+ description.getMediaId());
                if(mPlaylist.get(i).getDescription().getMediaId().equals(description.getMediaId()))
                {
                    Log.d(TAG, "onAddQueueItem: "+description.getMediaId() + " allredy in?");
                    return;
                }

            }
            mPlaylist.add(new MediaSessionCompat.QueueItem(description,description.hashCode()));
            mQueueIndex = (mQueueIndex == -1) ? 0 : mQueueIndex;
            mSession.setQueue(mPlaylist);
        }


        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            Log.d(TAG,"onRemoveQueueItem: called: Size list: "+mPlaylist.size());
            Log.d(TAG, "onRemoveQueueItem: we try to remove item: "+description.getMediaId());
            for(int i= 0; i<mPlaylist.size();i++)
            {
                if(mPlaylist.get(i).getDescription().getMediaId().equals(description.getMediaId()))
                {
                    Log.d(TAG, "onRemoveQueueItem: we removed: "+mPlaylist.get(i).getDescription().getMediaId());
                    mPlaylist.remove(i);
                    break;
                }
            }
            Log.d(TAG, "onRemoveQueueItem:  Size list after remove: "+mPlaylist.size());
            mQueueIndex = (mPlaylist.isEmpty()) ? -1 : mQueueIndex;
            mSession.setQueue(mPlaylist);
        }
        @Override
        public void onPrepare() {
            Log.d(TAG, "onPrepare: called");
            if(mQueueIndex < 0 && mPlaylist.isEmpty())
            {
                Log.d(TAG, "onPrepare:  return nothing from onPrepare");
                return;
            }
            String mediaId = mPlaylist.get(mQueueIndex).getDescription().getMediaId();
            mPreparedMedia = mMyApplication.getTreeMap().get(mediaId);
            mSession.setMetadata(mPreparedMedia);

            if (!mSession.isActive())
            {
                mSession.setActive(true);
            }
        }


        @Override
        public void onPlay() {
            Log.d(TAG, "onPlay: called");
            if(!isReadyToPlay())
            {
                return;
            }
            if (mPreparedMedia == null)
            {
                onPrepare();
            }
            mPlayback.playFromMedia(mPreparedMedia);
            mMyPrefManager.saveQueuePosition(mQueueIndex);
            mMyPrefManager.saveLastPlayedMedia(mPreparedMedia.getDescription().getMediaId());
        }

        @Override
        public void onPause() {
            Log.d(TAG, "onPause: called");
            mPlayback.pause();
        }


        @Override
        public void onStop() {
            Log.d(TAG, "onStop: called");
            mPlayback.stop();
            mSession.setActive(false);
        }


        @Override
        public void onSkipToNext() {
            Log.d(TAG,"onSkipToNext: SKIP TO NEXT");
            Log.d(TAG, "onSkipToNext:  the queueIndex before is: "+mQueueIndex +" playlistSize is: "+mPlaylist.size());
            if(mPlaylist.size() == 0)
            {
                Log.d(TAG, "onSkipToNext: size is 0 ");


            }
            if(mIsShuffle == false)
            {
                mQueueIndex = (++mQueueIndex % mPlaylist.size());
            }
            else
            {
                Log.d(TAG, "onSkipToNext: but shuffle is true so :");
                mQueueIndex = new Random().nextInt(mPlaylist.size());
            }
            Log.d(TAG,"onSkipToNext: queue index: "+mQueueIndex);
            mPreparedMedia = null;
            onPlay();

        }

        @Override
        public void onSkipToPrevious() {
            Log.d(TAG,"onSkipToPrevious: SKIP TO PREVIOUS");
            mQueueIndex = mQueueIndex > 0 ? mQueueIndex-1 : mPlaylist.size() - 1;
            mPreparedMedia = null;
            onPlay();

        }
        @Override
        public void onSeekTo(long pos) {
            mPlayback.seekTo(pos);
        }

        private boolean isReadyToPlay()
        {
            if(mPlaylist.isEmpty()) Log.d(TAG, "isReadyToPlay: playlist is empty ... size: " +mPlaylist.size());
            return (!mPlaylist.isEmpty());
        }
    }

    private class MediaPlayerListener implements PlaybackInfoListener
    {
        private final ServiceManager mServiceManager;

        public MediaPlayerListener() {
            mServiceManager = new ServiceManager();
        }


        @Override
        public void updateUI(String mediaId) {
            Log.d(TAG, "updateUI: called" + mediaId);
            Intent intent = new Intent();
            intent.setAction(getString(R.string.broadcast_update_ui));
            intent.putExtra(getString(R.string.broadcast_new_media_id),mediaId);
            sendBroadcast(intent);
        }

        @Override
        public void onPlaybackStateChange(PlaybackStateCompat state) {
            //Report the state to the MediaSession
            mSession.setPlaybackState(state);

            // manage the started state of this service.
            switch (state.getState())
            {
                case PlaybackStateCompat.STATE_PLAYING:
                {
                    mServiceManager.displayNotification(state);
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED:
                {
                    mServiceManager.displayNotification(state);
                    break;
                }
                case PlaybackStateCompat.STATE_STOPPED:
                {
                    mServiceManager.moveServiceOutOfStartedState();
                    break;
                }
            }
        }

        @Override
        public void seekTo(long progress, long max) {
            Intent intent = new Intent();
            intent.setAction(getString(R.string.broadcast_seekbar_update));
            intent.putExtra(SEEK_BAR_PROGRESS,progress);
            intent.putExtra(SEEK_BAR_MAX,max);
            sendBroadcast(intent);
        }

        @Override
        public void onPlaybackComplete() {
            Log.d(TAG, "onPlaybackComplete: SKIPPING TO NEXT");
            mSession.getController().getTransportControls().skipToNext();
        }

        class ServiceManager{
            private PlaybackStateCompat mState;

            public ServiceManager()
            { }

            public void displayNotification(PlaybackStateCompat state)
            {
                mState = state;
                Notification notification = null;
                switch (state.getState())
                {
                    case PlaybackStateCompat.STATE_PLAYING:
                    {
                        notification = mMediaNotificationManager.buildNotification(
                                state,getSessionToken(),mPlayback.getCurrentMedia().getDescription(),null);
                        if(!mIsServiceStarted)
                        {
                            ContextCompat.startForegroundService(MediaService.this,
                                    new Intent(MediaService.this,MediaService.class));
                            mIsServiceStarted = true;
                        }

                        startForeground(MediaNotificationManager.NOTIFICATION_ID,notification);
                        break;
                    }

                    case PlaybackStateCompat.STATE_PAUSED:
                    {
                        //Make us can swap the notification to remove it
                        stopForeground(false);

                        notification = mMediaNotificationManager.buildNotification(
                                state,getSessionToken(),mPlayback.getCurrentMedia().getDescription(),null);

                        mMediaNotificationManager.getNotificationManager().notify(MediaNotificationManager.NOTIFICATION_ID,notification);
                        break;
                    }
                }
            }
            private void moveServiceOutOfStartedState()
            {
                stopForeground(true);
                stopSelf();
                mIsServiceStarted = false;
            }
        }


    }
}
