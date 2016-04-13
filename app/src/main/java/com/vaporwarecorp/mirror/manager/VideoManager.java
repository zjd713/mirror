package com.vaporwarecorp.mirror.manager;

import com.vaporwarecorp.mirror.event.VideoCompletedEvent;
import com.vaporwarecorp.mirror.vendor.ijk.IjkVideoView;
import org.greenrobot.eventbus.EventBus;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoManager implements IMediaPlayer.OnCompletionListener {
// ------------------------------ FIELDS ------------------------------

    private IjkVideoView mView;

// --------------------------- CONSTRUCTORS ---------------------------

    public VideoManager() {
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface OnCompletionListener ---------------------

    @Override
    public void onCompletion(IMediaPlayer mp) {
        EventBus.getDefault().post(new VideoCompletedEvent());
    }

// -------------------------- OTHER METHODS --------------------------

    public void onDestroy() {
        if (mView != null) {
            mView.setOnCompletionListener(null);
            mView.release(true);
            mView = null;
        }
    }

    public void setIjkVideoView(IjkVideoView view) {
        mView = view;
        mView.setOnCompletionListener(this);
    }

    public void start(String videoUri) {
        mView.setVideoPath(videoUri);
        mView.start();
    }

    public void stop() {
        if (mView != null) {
            mView.stopPlayback();
        }
    }

    public void togglePlay() {
        if (mView.isPlaying()) {
            mView.pause();
        } else {
            mView.start();
        }
    }
}