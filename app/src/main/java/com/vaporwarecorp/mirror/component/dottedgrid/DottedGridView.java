package com.vaporwarecorp.mirror.component.dottedgrid;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import com.vaporwarecorp.mirror.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class DottedGridView extends PercentRelativeLayout {
// ------------------------------ FIELDS ------------------------------

    private static final int INVALID_POINTER = -1;
    private static final float SENSITIVITY = 1f;

    private int activePointerId = INVALID_POINTER;
    private float lastTouchActionDownXPosition;
    private Drawable mBorder;
    private FrameLayout mContainer;
    private FrameLayout mFragmentContainer;
    private ViewDragHelper mViewDragHelper;

// --------------------------- CONSTRUCTORS ---------------------------

    public DottedGridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeLayout(context);
        initializeViewDragHelper();
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * To ensure the animation is going to work this method has been override to call
     * postInvalidateOnAnimation if the view is not settled yet.
     */
    @Override
    public void computeScroll() {
        if (!isInEditMode() && mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public int getDraggedViewHeightPlusMarginTop() {
        return mFragmentContainer.getHeight();
    }

    /**
     * Checks if the top view is closed at the right or left place.
     *
     * @return true if the view is closed.
     */
    public boolean isClosed() {
        return isClosedAtLeft() || isClosedAtRight();
    }

    /**
     * Checks if the top view is closed at the left place.
     *
     * @return true if the view is closed at left.
     */
    public boolean isClosedAtLeft() {
        return mFragmentContainer.getRight() <= 0;
    }

    /**
     * Checks if the top view closed at the right place.
     *
     * @return true if the view is closed at right.
     */
    public boolean isClosedAtRight() {
        return mFragmentContainer.getLeft() >= getWidth();
    }

    /**
     * Override method to intercept only touch events over the drag view and to cancel the drag when
     * the action associated to the MotionEvent is equals to ACTION_CANCEL or ACTION_UP.
     *
     * @param ev captured.
     * @return true if the view is going to process the touch event or false if not.
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }
        switch (MotionEventCompat.getActionMasked(ev) & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mViewDragHelper.cancel();
                return false;
            case MotionEvent.ACTION_DOWN:
                int index = MotionEventCompat.getActionIndex(ev);
                activePointerId = MotionEventCompat.getPointerId(ev, index);
                if (activePointerId == INVALID_POINTER) {
                    return false;
                }
                break;
            default:
                break;
        }
        boolean interceptTap = mViewDragHelper.isViewUnder(mFragmentContainer, (int) ev.getX(), (int) ev.getY());
        return mViewDragHelper.shouldInterceptTouchEvent(ev) || interceptTap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int actionMasked = MotionEventCompat.getActionMasked(event);
        switch (actionMasked & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_CANCEL:
                return false;
            case MotionEvent.ACTION_UP:
                mContainer.setVisibility(INVISIBLE);
                break;
            case MotionEvent.ACTION_DOWN:
                activePointerId = MotionEventCompat.getPointerId(event, actionMasked);
                mContainer.setVisibility(VISIBLE);
                break;
        }
        if (activePointerId == INVALID_POINTER) {
            return false;
        }
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    private void initializeLayout(Context context) {
        mBorder = ContextCompat.getDrawable(context, R.drawable.bg_solid_border);

        FrameLayout gridLayout = new FrameLayout(context);
        gridLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dotted_grid));
        gridLayout.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));

        mContainer = new FrameLayout(context);
        mContainer.addView(gridLayout);
        mContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_solid_border));
        mContainer.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        mContainer.setVisibility(INVISIBLE);
        addView(mContainer);

        LayoutParams params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.addRule(PercentRelativeLayout.CENTER_IN_PARENT);
        params.getPercentLayoutInfo().widthPercent = 0.3f;

        mFragmentContainer = new FrameLayout(context);
        mFragmentContainer.setId(R.id.fragment_container);
        mFragmentContainer.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        mFragmentContainer.setLayoutParams(params);
        addView(mFragmentContainer);
    }

    /**
     * Initialize the viewDragHelper.
     */
    private void initializeViewDragHelper() {
        mViewDragHelper = ViewDragHelper.create(this, SENSITIVITY, new DraggableViewCallback(this, mFragmentContainer));
    }
}