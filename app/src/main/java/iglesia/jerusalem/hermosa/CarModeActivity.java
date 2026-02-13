package iglesia.jerusalem.hermosa;

import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.media3.common.Player;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ExecutionException;

public class CarModeActivity extends AppCompatActivity {

    private MediaController mediaController;
    private ExtendedFloatingActionButton playPauseButton;
    private FloatingActionButton skipButton;
    private TextView statusText;
    private ListenableFuture<MediaController> controllerFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_car_mode);

        playPauseButton = findViewById(R.id.play_pause_button);
        skipButton = findViewById(R.id.skip_button);
        statusText = findViewById(R.id.status_text);

        findViewById(R.id.close_button).setOnClickListener(v -> finish());

        playPauseButton.setOnClickListener(v -> {
            animateButton(v);
            if (mediaController != null) {
                if (mediaController.isPlaying()) {
                    mediaController.pause();
                } else {
                    mediaController.play();
                }
            } else {
                initializeController();
            }
        });

        skipButton.setOnClickListener(v -> {
            animateButton(v);
            if (mediaController != null) {
                mediaController.stop();
                mediaController.prepare();
                mediaController.play();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeController();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (controllerFuture != null) {
            MediaController.releaseFuture(controllerFuture);
            controllerFuture = null;
        }
        mediaController = null;
    }

    private void initializeController() {
        SessionToken sessionToken = new SessionToken(this, new ComponentName(this, RadioService.class));
        controllerFuture = new MediaController.Builder(this, sessionToken).buildAsync();
        controllerFuture.addListener(() -> {
            try {
                mediaController = controllerFuture.get();
                updateUI();
                mediaController.addListener(new Player.Listener() {
                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        updateUI();
                    }

                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        updateUI();
                    }
                });
                if (mediaController.getPlaybackState() == Player.STATE_IDLE) {
                    mediaController.prepare();
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, MoreExecutors.directExecutor());
    }

    private void updateUI() {
        if (mediaController == null) return;

        if (mediaController.isPlaying()) {
            playPauseButton.setIconResource(R.drawable.ic_pause);
            playPauseButton.setText(R.string.pause);
            statusText.setText(R.string.now_playing);
        } else {
            playPauseButton.setIconResource(R.drawable.ic_play);
            playPauseButton.setText(R.string.play);
            int state = mediaController.getPlaybackState();
            if (state == Player.STATE_BUFFERING) {
                statusText.setText(R.string.loading);
            } else {
                statusText.setText(R.string.pause);
            }
        }
    }

    private void animateButton(View v) {
        ScaleAnimation anim = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(100);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);
        v.startAnimation(anim);
    }
}
