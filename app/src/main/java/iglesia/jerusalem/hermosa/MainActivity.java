package iglesia.jerusalem.hermosa;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.splashscreen.SplashScreen;
import androidx.media3.common.Player;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private MediaController mediaController;
    private ExtendedFloatingActionButton playPauseButton;
    private TextView statusText;
    private ImageView logoImage;
    private ListenableFuture<MediaController> controllerFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        restoreTheme();

        setContentView(R.layout.activity_main);

        playPauseButton = findViewById(R.id.play_pause_button);
        statusText = findViewById(R.id.status_text);
        logoImage = findViewById(R.id.logo_image);

        findViewById(R.id.settings_button).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        playPauseButton.setOnClickListener(v -> {
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
    }

    private void restoreTheme() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        int savedTheme = prefs.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedTheme);
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
            startAnim();
        } else {
            playPauseButton.setIconResource(R.drawable.ic_play);
            playPauseButton.setText(R.string.play);
            int state = mediaController.getPlaybackState();
            if (state == Player.STATE_BUFFERING) {
                statusText.setText(R.string.loading);
                startAnim();
            } else if (state == Player.STATE_READY) {
                statusText.setText(R.string.pause);
                stopAnim();
            } else {
                statusText.setText(R.string.pause); // Or idle
                stopAnim();
            }
        }
    }

    private void startAnim() {
        if (logoImage.getAnimation() == null) {
            ScaleAnimation anim = new ScaleAnimation(1.0f, 1.1f, 1.0f, 1.1f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(1000);
            anim.setRepeatCount(Animation.INFINITE);
            anim.setRepeatMode(Animation.REVERSE);
            logoImage.startAnimation(anim);
        }
    }

    private void stopAnim() {
        logoImage.clearAnimation();
    }
}
