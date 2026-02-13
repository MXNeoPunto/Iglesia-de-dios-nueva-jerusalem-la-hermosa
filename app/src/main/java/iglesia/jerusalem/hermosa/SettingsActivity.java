package iglesia.jerusalem.hermosa;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_THEME = "theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setupThemeToggle();
        setupLanguageSpinner();
    }

    private void setupThemeToggle() {
        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.theme_toggle_group);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int savedTheme = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

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
                AppCompatDelegate.setDefaultNightMode(mode);
                prefs.edit().putInt(KEY_THEME, mode).apply();
            }
        });
    }

    private void setupLanguageSpinner() {
        Spinner spinner = findViewById(R.id.language_spinner);
        String[] languages = {"Español", "English"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, languages);
        spinner.setAdapter(adapter);

        // Get current locale
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (!locales.isEmpty()) {
            String lang = locales.get(0).getLanguage();
            if (lang.equals("en")) {
                spinner.setSelection(1);
            } else {
                spinner.setSelection(0);
            }
        } else {
            spinner.setSelection(0);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLang = position == 0 ? "es" : "en";
                LocaleListCompat current = AppCompatDelegate.getApplicationLocales();
                String currentLang = current.isEmpty() ? "es" : current.get(0).getLanguage();

                if (!selectedLang.equals(currentLang)) {
                    LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(selectedLang);
                    AppCompatDelegate.setApplicationLocales(appLocale);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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
