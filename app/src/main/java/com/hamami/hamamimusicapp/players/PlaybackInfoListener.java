package com.hamami.hamamimusicapp.players;

import android.support.v4.media.session.PlaybackStateCompat;

public interface PlaybackInfoListener {

    void onPlaybackStateChange(PlaybackStateCompat state);

    void seekTo(long progress, long max);

    void onPlaybackComplete();

    void updateUI(String mediaId);

}
