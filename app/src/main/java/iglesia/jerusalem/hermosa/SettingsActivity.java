package iglesia.jerusalem.hermosa;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_THEME = "theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_settings);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setup Gradient Background (Only for Dark Mode)
        View gradientBackground = findViewById(R.id.gradient_background);
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            gradientBackground.setVisibility(View.VISIBLE);
            GradientAnimationHelper.animateGradient(gradientBackground);
        } else {
            gradientBackground.setVisibility(View.GONE);
        }

        // Apply Window Insets (Edge-to-Edge)
        // AppBarLayout handles top insets via fitsSystemWindows="true" in XML.
        // We manually handle bottom insets for the content scroll view.
        View scrollView = findViewById(R.id.settings_content_scroll);
        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        setupThemeToggle();
        setupLanguageToggle();
    }

    private void setupThemeToggle() {
        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.theme_toggle_group);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int savedTheme = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // Initialize state without triggering listener immediately if possible,
        // but adding listener after check is safer to avoid redundant calls during init.
        if (savedTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            toggleGroup.check(R.id.btn_light);
        } else if (savedTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            toggleGroup.check(R.id.btn_dark);
        } else {
            toggleGroup.check(R.id.btn_system);
        }

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                int mode;
                if (checkedId == R.id.btn_light) {
                    mode = AppCompatDelegate.MODE_NIGHT_NO;
                } else if (checkedId == R.id.btn_dark) {
                    mode = AppCompatDelegate.MODE_NIGHT_YES;
                } else {
                    mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                }

                prefs.edit().putInt(KEY_THEME, mode).apply();

                // Only apply if it changes the current mode, but setDefaultNightMode handles that check internally mostly.
                // However, forcing it ensures the app recreates if needed.
                AppCompatDelegate.setDefaultNightMode(mode);
            }
        });
    }

    private void setupLanguageToggle() {
        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.language_toggle_group);

        // Get current locale
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        String currentLang;
        if (!locales.isEmpty()) {
            currentLang = locales.get(0).getLanguage();
        } else {
            currentLang = java.util.Locale.getDefault().getLanguage();
        }

        if (currentLang.equals("en")) {
            toggleGroup.check(R.id.btn_en);
        } else {
            toggleGroup.check(R.id.btn_es);
        }

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String selectedLang = (checkedId == R.id.btn_en) ? "en" : "es";
                LocaleListCompat currentAppLocales = AppCompatDelegate.getApplicationLocales();
                String appLang = currentAppLocales.isEmpty() ? java.util.Locale.getDefault().getLanguage() : currentAppLocales.get(0).getLanguage();

                if (!selectedLang.equals(appLang)) {
                    LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(selectedLang);
                    AppCompatDelegate.setApplicationLocales(appLocale);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
