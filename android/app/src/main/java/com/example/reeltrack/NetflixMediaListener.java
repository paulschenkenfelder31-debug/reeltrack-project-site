package com.example.reeltrack;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.NotificationListenerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Opt-in local companion for the Netflix Android media session. This service never reads
 * notification content or account data. It only saves metadata that Android exposes through
 * Netflix's active MediaSession after the user enables Notification access in Settings.
 */
public class NetflixMediaListener extends NotificationListenerService {
    private static final String NETFLIX_PACKAGE = "com.netflix.mediaclient";
    private MediaSessionManager sessionManager;
    private final Map<MediaController, MediaController.Callback> callbacks = new HashMap<MediaController, MediaController.Callback>();
    private final MediaSessionManager.OnActiveSessionsChangedListener sessionListener = new MediaSessionManager.OnActiveSessionsChangedListener() {
        @Override public void onActiveSessionsChanged(List<MediaController> controllers) {
            bindNetflixSessions(controllers);
        }
    };

    @Override public void onListenerConnected() {
        super.onListenerConnected();
        sessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        if (sessionManager == null) return;
        try {
            ComponentName component = new ComponentName(this, NetflixMediaListener.class);
            sessionManager.addOnActiveSessionsChangedListener(sessionListener, component, new Handler(Looper.getMainLooper()));
            bindNetflixSessions(sessionManager.getActiveSessions(component));
        } catch (SecurityException ignored) {
            saveActive(false);
        }
    }

    @Override public void onListenerDisconnected() {
        if (sessionManager != null) {
            sessionManager.removeOnActiveSessionsChangedListener(sessionListener);
        }
        for (Map.Entry<MediaController, MediaController.Callback> entry : callbacks.entrySet()) {
            entry.getKey().unregisterCallback(entry.getValue());
        }
        callbacks.clear();
        saveActive(false);
        super.onListenerDisconnected();
    }

    private void bindNetflixSessions(List<MediaController> controllers) {
        boolean found = false;
        if (controllers != null) for (final MediaController controller : controllers) {
            if (!NETFLIX_PACKAGE.equals(controller.getPackageName())) continue;
            found = true;
            if (!callbacks.containsKey(controller)) {
                MediaController.Callback callback = new MediaController.Callback() {
                    @Override public void onMetadataChanged(MediaMetadata metadata) { save(controller, metadata, controller.getPlaybackState()); }
                    @Override public void onPlaybackStateChanged(PlaybackState state) { save(controller, controller.getMetadata(), state); }
                    @Override public void onSessionDestroyed() { saveActive(false); }
                };
                callbacks.put(controller, callback);
                controller.registerCallback(callback, new Handler(Looper.getMainLooper()));
            }
            save(controller, controller.getMetadata(), controller.getPlaybackState());
        }
        if (!found) saveActive(false);
    }

    private void save(MediaController controller, MediaMetadata metadata, PlaybackState playbackState) {
        SharedPreferences prefs = getSharedPreferences("reeltrack", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit().putBoolean("netflix.media.active", true).putLong("netflix.media.updated", System.currentTimeMillis());
        if (metadata != null) {
            CharSequence title = metadata.getDescription() == null ? null : metadata.getDescription().getTitle();
            CharSequence subtitle = metadata.getDescription() == null ? null : metadata.getDescription().getSubtitle();
            if (title != null && title.length() > 0) editor.putString("netflix.media.title", title.toString());
            if (subtitle != null && subtitle.length() > 0) editor.putString("netflix.media.subtitle", subtitle.toString());
            long duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
            if (duration > 0) editor.putLong("netflix.media.duration", duration);
        }
        if (playbackState != null) {
            editor.putLong("netflix.media.position", Math.max(0, playbackState.getPosition()));
            editor.putString("netflix.media.state", stateName(playbackState.getState()));
        }
        editor.apply();
    }

    private void saveActive(boolean active) {
        getSharedPreferences("reeltrack", MODE_PRIVATE).edit().putBoolean("netflix.media.active", active).apply();
    }

    private String stateName(int state) {
        switch (state) {
            case PlaybackState.STATE_PLAYING: return "Playing";
            case PlaybackState.STATE_PAUSED: return "Paused";
            case PlaybackState.STATE_BUFFERING: return "Buffering";
            case PlaybackState.STATE_CONNECTING: return "Connecting";
            case PlaybackState.STATE_STOPPED: return "Stopped";
            case PlaybackState.STATE_ERROR: return "Playback error";
            default: return "Playback status unavailable";
        }
    }
}
