package iglesia.jerusalem.hermosa;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

public class GradientAnimationHelper {

    public static void animateGradient(View gradientBackground) {
        if (gradientBackground == null) return;

        int colorStart = Color.parseColor("#DAA520"); // Goldenrod
        int colorEnd = Color.parseColor("#B8860B"); // Dark Goldenrod
        int colorBottom = Color.parseColor("#000000"); // Black

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {colorStart, colorBottom});

        gradientBackground.setBackground(gd);

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorStart, colorEnd);
        colorAnimation.setDuration(5000); // 5 seconds
        colorAnimation.setRepeatCount(ValueAnimator.INFINITE);
        colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
        colorAnimation.addUpdateListener(animator -> {
            if (gradientBackground != null) {
                gd.setColors(new int[] {(int) animator.getAnimatedValue(), colorBottom});
            }
        });
        colorAnimation.start();
    }
}
