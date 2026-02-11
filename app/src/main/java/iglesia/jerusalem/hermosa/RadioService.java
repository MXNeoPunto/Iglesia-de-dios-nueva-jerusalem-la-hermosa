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
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (!player.getPlayWhenReady() || player.getMediaItemCount() == 0) {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
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
