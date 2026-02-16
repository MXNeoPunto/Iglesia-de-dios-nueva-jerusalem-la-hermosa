package iglesia.jerusalem.hermosa;

import android.content.res.Configuration;
import android.content.ComponentName;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.media3.common.Player;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private MediaController mediaController;
    private ExtendedFloatingActionButton playPauseButton;
    private MaterialButton timerButton;
    private MaterialButton carModeButton;
    private TextView statusText;
    private TextView timerText;
    private ImageView logoImage;
    private View gradientBackground;
    private ListenableFuture<MediaController> controllerFuture;
    private CountDownTimer sleepTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        restoreTheme();

        setContentView(R.layout.activity_main);

        gradientBackground = findViewById(R.id.gradient_background);
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            gradientBackground.setVisibility(View.VISIBLE);
            GradientAnimationHelper.animateGradient(gradientBackground);
        } else {
            gradientBackground.setVisibility(View.GONE);
        }

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.content_container), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(v -> showMenuBottomSheet());

        playPauseButton = findViewById(R.id.play_pause_button);
        timerButton = findViewById(R.id.timer_button);
        carModeButton = findViewById(R.id.car_mode_button);
        statusText = findViewById(R.id.status_text);
        timerText = findViewById(R.id.timer_text);
        logoImage = findViewById(R.id.logo_image);

        timerButton.setOnClickListener(v -> showSleepTimerDialog());
        carModeButton.setOnClickListener(v -> startActivity(new Intent(this, CarModeActivity.class)));

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
                statusText.setText(R.string.pause);
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

    private void animateButton(View v) {
        ScaleAnimation anim = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(100);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);
        v.startAnimation(anim);
    }

    private void showMenuBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_menu, null);
        bottomSheetDialog.setContentView(view);

        view.findViewById(R.id.btn_menu_equalizer).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            openEqualizer();
        });
        view.findViewById(R.id.btn_menu_settings).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            startActivity(new Intent(this, SettingsActivity.class));
        });
        view.findViewById(R.id.btn_menu_about).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showAboutDialog();
        });

        bottomSheetDialog.show();
    }

    private void openEqualizer() {
        String[] options = {
            getString(R.string.eq_normal),
            getString(R.string.eq_voice),
            getString(R.string.eq_praise),
            getString(R.string.eq_worship)
        };

        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        int currentMode = prefs.getInt("equalizer_mode", 0);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle(R.string.equalizer_mode)
            .setSingleChoiceItems(options, currentMode, (dialog, which) -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("equalizer_mode", which);
                editor.apply();
                dialog.dismiss();
                Toast.makeText(this, options[which], Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showSleepTimerDialog() {
        String[] options = {
            getString(R.string.minutes_15),
            getString(R.string.minutes_30),
            getString(R.string.minutes_60),
            getString(R.string.cancel)
        };

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sleep_timer)
            .setItems(options, (dialog, which) -> {
                if (which == 0) startTimer(15);
                else if (which == 1) startTimer(30);
                else if (which == 2) startTimer(60);
                else cancelTimer();
            })
            .show();
    }

    private void startTimer(int minutes) {
        if (sleepTimer != null) sleepTimer.cancel();
        long millis = minutes * 60 * 1000L;
        timerText.setVisibility(View.VISIBLE);

        sleepTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                timerText.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                if (mediaController != null) mediaController.pause();
                sleepTimer = null;
                timerText.setVisibility(View.GONE);
            }
        }.start();
        Toast.makeText(this, String.format(getString(R.string.sleep_timer_set), minutes), Toast.LENGTH_SHORT).show();
    }

    private void cancelTimer() {
        if (sleepTimer != null) {
            sleepTimer.cancel();
            sleepTimer = null;
            timerText.setVisibility(View.GONE);
            Toast.makeText(this, R.string.sleep_timer_cancelled, Toast.LENGTH_SHORT).show();
        }
    }

    private void showAboutDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle(R.string.about)
            .setMessage(R.string.about_text)
            .setPositiveButton(android.R.string.ok, null)
            .setNeutralButton(R.string.contact, (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(android.net.Uri.parse("mailto:ventas@neopunto.com"));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
                }
            })
            .show();
    }
}
