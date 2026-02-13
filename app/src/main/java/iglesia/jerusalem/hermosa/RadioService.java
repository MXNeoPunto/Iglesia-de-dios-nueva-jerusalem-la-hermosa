package iglesia.jerusalem.hermosa;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.LibraryResult;
import androidx.media3.session.MediaLibraryService;
import androidx.media3.session.MediaSession;
import androidx.media3.session.SessionCommand;
import androidx.media3.session.SessionResult;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class RadioService extends MediaLibraryService {

    private MediaLibrarySession mediaLibrarySession;
    private ExoPlayer player;
    private android.media.audiofx.Equalizer equalizer;
    private android.content.SharedPreferences.OnSharedPreferenceChangeListener prefsListener;
    private static final String PREF_EQ_MODE = "equalizer_mode";

    @Override
    public void onCreate() {
        super.onCreate();
        player = new ExoPlayer.Builder(this).build();

        // Prepare the stream immediately
        String streamUrl = CryptoUtils.getStreamUrl();
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(streamUrl)
                .setMediaId(streamUrl)
                .setMediaMetadata(new MediaMetadata.Builder()
                        .setTitle("Nueva Jerusalem")
                        .setArtist("Iglesia Jerusalem Hermosa")
                        .setIsBrowsable(false)
                        .setIsPlayable(true)
                        .build())
                .build();

        player.setMediaItem(mediaItem);
        player.prepare();

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        mediaLibrarySession = new MediaLibrarySession.Builder(this, player, new CustomMediaLibrarySessionCallback())
                .setSessionActivity(pendingIntent)
                .build();

        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE);
        prefsListener = (sharedPreferences, key) -> {
            if (PREF_EQ_MODE.equals(key)) {
                int mode = sharedPreferences.getInt(PREF_EQ_MODE, 0);
                applyEqualizerMode(mode);
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(prefsListener);

        player.addListener(new Player.Listener() {
            @Override
            public void onAudioSessionIdChanged(int audioSessionId) {
                if (audioSessionId != androidx.media3.common.C.AUDIO_SESSION_ID_UNSET) {
                    initializeEqualizer(audioSessionId);
                }
            }
        });
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (!player.getPlayWhenReady() || player.getMediaItemCount() == 0) {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        if (prefsListener != null) {
            android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE);
            prefs.unregisterOnSharedPreferenceChangeListener(prefsListener);
        }
        if (equalizer != null) {
            equalizer.release();
            equalizer = null;
        }
        if (mediaLibrarySession != null) {
            mediaLibrarySession.release();
            mediaLibrarySession = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public MediaLibrarySession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaLibrarySession;
    }

    private void initializeEqualizer(int audioSessionId) {
        if (equalizer != null) {
            equalizer.release();
        }
        try {
            equalizer = new android.media.audiofx.Equalizer(0, audioSessionId);
            equalizer.setEnabled(true);

            android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE);
            int mode = prefs.getInt(PREF_EQ_MODE, 0);
            applyEqualizerMode(mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyEqualizerMode(int mode) {
        if (equalizer == null) return;

        try {
            short bands = equalizer.getNumberOfBands();
            short[] range = equalizer.getBandLevelRange();
            short minEQ = range[0];
            short maxEQ = range[1];

            for (short i = 0; i < bands; i++) {
                int centerFreq = equalizer.getCenterFreq(i) / 1000; // Hz
                short level = 0;

                int bandFreq = equalizer.getCenterFreq(i) / 1000;

                switch (mode) {
                    case 0: // Normal
                        level = 0;
                        break;
                    case 1: // Voz (Voice) - Boost Mids (400Hz - 3kHz)
                        if (bandFreq >= 400 && bandFreq <= 3000) {
                            level = (short) (maxEQ * 0.5);
                        }
                        break;
                    case 2: // Alabanzas (Praise) - Boost Bass (< 200Hz)
                        if (bandFreq < 200) {
                            level = (short) (maxEQ * 0.6);
                        }
                        break;
                    case 3: // Adoración (Worship) - V-Shape
                        if (bandFreq < 100) {
                            level = (short) (maxEQ * 0.4);
                        } else if (bandFreq > 4000) {
                            level = (short) (maxEQ * 0.4);
                        } else if (bandFreq >= 500 && bandFreq <= 2000) {
                            level = (short) (minEQ * 0.2); // Slight cut
                        }
                        break;
                }
                equalizer.setBandLevel(i, level);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class CustomMediaLibrarySessionCallback implements MediaLibrarySession.Callback {
        @Override
        public ListenableFuture<LibraryResult<MediaItem>> onGetLibraryRoot(
                MediaLibrarySession session, MediaSession.ControllerInfo browser, @Nullable LibraryParams params) {
            MediaItem rootItem = new MediaItem.Builder()
                    .setMediaId("root")
                    .setMediaMetadata(new MediaMetadata.Builder()
                            .setTitle("Root")
                            .setIsBrowsable(true)
                            .setIsPlayable(false)
                            .build())
                    .build();
            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params));
        }

        @Override
        public ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> onGetChildren(
                MediaLibrarySession session,
                MediaSession.ControllerInfo browser,
                String parentId,
                int page,
                int pageSize,
                @Nullable LibraryParams params) {

            if ("root".equals(parentId)) {
                 String streamUrl = CryptoUtils.getStreamUrl();
                 MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(streamUrl)
                    .setMediaId(streamUrl)
                    .setMediaMetadata(new MediaMetadata.Builder()
                            .setTitle("Nueva Jerusalem")
                            .setArtist("Iglesia Jerusalem Hermosa")
                            .setIsBrowsable(false)
                            .setIsPlayable(true)
                            .build())
                    .build();

                 return Futures.immediateFuture(LibraryResult.ofItemList(
                         ImmutableList.of(mediaItem),
                         params));
            }

            return Futures.immediateFuture(LibraryResult.ofItemList(
                    ImmutableList.of(),
                    params));
        }

        @Override
        public ListenableFuture<LibraryResult<Void>> onSubscribe(
            MediaLibrarySession session,
            MediaSession.ControllerInfo browser,
            String parentId,
            @Nullable LibraryParams params) {
            return Futures.immediateFuture(LibraryResult.ofVoid());
        }

        // Handle custom commands if needed, or just default behavior
    }
}
