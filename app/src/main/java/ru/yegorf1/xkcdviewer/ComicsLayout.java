package ru.yegorf1.xkcdviewer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ComicsLayout extends LinearLayout {
    public static interface SwipeListener {
        void onLeftSlide();
        void onRightSlide();
    }

    private SwipeListener l;
    private final GestureDetector gestureDetector;
    private final Context context;

    public ComicsLayout(Context context) {
        super(context);
        this.context = context;
        this.gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public ComicsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void setSwipeListener(SwipeListener l) {
        this.l = l;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                    result = true;
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }


    public void onSwipeRight() {
        if (l != null) {
            l.onRightSlide();
        }
    }

    public void onSwipeLeft() {
        if (l != null) {
            l.onLeftSlide();
        }
    }
}
