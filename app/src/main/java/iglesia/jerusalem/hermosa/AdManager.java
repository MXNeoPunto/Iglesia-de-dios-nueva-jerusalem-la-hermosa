package iglesia.jerusalem.hermosa;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.Calendar;

public class AdManager {
    private static AdManager instance;
    private RewardedAd rewardedAd;
    private boolean isLoadingRewarded = false;
    // Test IDs
    private static final String TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111";
    private static final String TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917";

    private static final String PREFS_NAME = "ad_prefs";
    private static final long AD_FREE_DURATION_MS = 30 * 60 * 1000; // 30 mins
    private static final long MIN_AD_INTERVAL_MS = 15 * 1000; // 15 seconds
    private static final int MAX_ADS_PER_DAY = 5;

    private AdManager() {}

    public static synchronized AdManager getInstance() {
        if (instance == null) {
            instance = new AdManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        MobileAds.initialize(context, initializationStatus -> {});
        loadRewardedAd(context);
    }

    public void loadBanner(Activity activity, ViewGroup container) {
        if (isAdFree(activity)) {
            container.setVisibility(View.GONE);
            container.removeAllViews();
            return;
        }

        container.removeAllViews();
        AdView adView = new AdView(activity);
        adView.setAdUnitId(TEST_BANNER_ID);
        adView.setAdSize(AdSize.BANNER);
        container.addView(adView);
        container.setVisibility(View.VISIBLE);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void loadRewardedAd(Context context) {
        if (rewardedAd != null || isLoadingRewarded) return;
        isLoadingRewarded = true;

        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(context, TEST_REWARDED_ID, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                rewardedAd = null;
                isLoadingRewarded = false;
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
                isLoadingRewarded = false;
            }
        });
    }

    public void showRewardedAd(Activity activity, String featureToUnlock, Runnable onSuccess) {
        if (!canShowAd(activity)) {
            Toast.makeText(activity, activity.getString(R.string.ad_limit_reached), Toast.LENGTH_SHORT).show();
            return;
        }

        if (rewardedAd != null) {
            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    rewardedAd = null;
                    loadRewardedAd(activity); // Preload next
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    rewardedAd = null;
                }
            });

            rewardedAd.show(activity, rewardItem -> {
                unlockFeature(activity, featureToUnlock);
                activateAdFree(activity);
                incrementDailyAdCount(activity);
                recordAdShowTime(activity);
                if (onSuccess != null) onSuccess.run();
                Toast.makeText(activity, activity.getString(R.string.feature_unlocked), Toast.LENGTH_LONG).show();
            });
        } else {
            Toast.makeText(activity, activity.getString(R.string.ad_loading), Toast.LENGTH_SHORT).show();
            loadRewardedAd(activity);
        }
    }

    public boolean isFeatureUnlocked(Context context, String feature) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("feature_" + feature, false);
    }

    private void unlockFeature(Context context, String feature) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("feature_" + feature, true).apply();
    }

    public boolean isAdFree(Context context) {
        return getAdFreeRemainingTime(context) > 0;
    }

    public long getAdFreeRemainingTime(Context context) {
         SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
         long expiry = prefs.getLong("ad_free_until", 0);
         long diff = expiry - System.currentTimeMillis();
         return diff > 0 ? diff : 0;
    }

    private void activateAdFree(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long expiry = System.currentTimeMillis() + AD_FREE_DURATION_MS;
        prefs.edit().putLong("ad_free_until", expiry).apply();
    }

    private boolean canShowAd(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastTime = prefs.getLong("last_ad_time", 0);
        if (System.currentTimeMillis() - lastTime < MIN_AD_INTERVAL_MS) return false;

        String today = getDateString();
        String savedDate = prefs.getString("ad_date", "");
        int count = prefs.getInt("daily_ad_count", 0);

        if (!today.equals(savedDate)) {
            // New day, reset
            prefs.edit().putString("ad_date", today).putInt("daily_ad_count", 0).apply();
            return true;
        }

        return count < MAX_ADS_PER_DAY;
    }

    private void incrementDailyAdCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int count = prefs.getInt("daily_ad_count", 0);
        prefs.edit().putInt("daily_ad_count", count + 1).apply();
    }

    private void recordAdShowTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong("last_ad_time", System.currentTimeMillis()).apply();
    }

    private String getDateString() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH);
    }
}
