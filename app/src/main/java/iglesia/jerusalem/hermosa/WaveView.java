package iglesia.jerusalem.hermosa;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class WaveView extends View {

    private final Paint paint;
    private ValueAnimator animator;
    private float animationProgress = 0f;
    private boolean isAnimating = false;
    private int maxRadius = 0;
    private int waveColor = Color.parseColor("#80FFFFFF"); // Semi-transparent white

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(waveColor);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        maxRadius = Math.min(w, h) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isAnimating) return;

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        // Draw 3 waves
        for (int i = 0; i < 3; i++) {
            float offset = i * 0.33f;
            float progress = (animationProgress + offset) % 1.0f;

            float radius = progress * maxRadius;
            int alpha = (int) ((1.0f - progress) * 100); // Fade out as it expands

            paint.setAlpha(alpha);
            canvas.drawCircle(cx, cy, radius, paint);
        }
    }

    public void startAnimation() {
        if (isAnimating) return;
        isAnimating = true;

        if (animator == null) {
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(2000);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(animation -> {
                animationProgress = (float) animation.getAnimatedValue();
                invalidate();
            });
        }
        animator.start();
    }

    public void stopAnimation() {
        isAnimating = false;
        if (animator != null) {
            animator.cancel();
        }
        invalidate();
    }

    public void setColor(int color) {
        this.waveColor = color;
        paint.setColor(color);
        invalidate();
    }
}
