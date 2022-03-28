package com.root.nativeCamera.util;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    // new code
    private int min_distance = 100;
    private float downX, downY, upX, upY;
    View v;

    public OnSwipeTouchListener(Context ctx) {
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getPointerCount() == 1) {
            this.v = v;
            switch (event.getAction()) { // Check vertical and horizontal touches
                case MotionEvent.ACTION_DOWN: {
                    downX = event.getX();
                    downY = event.getY();
                    return false;
                }
                case MotionEvent.ACTION_UP: {
                    upX = event.getX();
                    upY = event.getY();

                    float deltaX = downX - upX;
                    float deltaY = downY - upY;

                    //HORIZONTAL SCROLL
                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        if (Math.abs(deltaX) > min_distance) {
                            // left or right
                            if (deltaX < 0) {
                                onSwipeRight();
                                return true;
                            }
                            if (deltaX > 0) {
                                onSwipeLeft();
                                return true;
                            }
                        } else {
                            //not long enough swipe...
                            return false;
                        }
                    }
                }
            }
        }
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
                        result = true;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onZoom(float zoomLevel) {
    }


}
