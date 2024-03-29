package com.elimak.chap12horizontaliconview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by elimak on 10/11/13.
 */
public class HorizontalIconView extends View {

    private static final String TAG = "HorizontalIconView";

    private static final int INVALID_POINTER = MotionEvent.INVALID_POINTER_ID;

    private int mActivePointerId = INVALID_POINTER;

    private List<Drawable> mDrawables;

    private EdgeEffectCompat mEdgeEffectLeft;

    private EdgeEffectCompat mEdgeEffectRight;

    private final List<Rect> mIconPositions = new ArrayList<Rect>();

    private int mIconSize;

    private int mIconSpacing;

    private boolean mIsBeingDragged;

    private int mMaximumVelocity;

    private int mMinimumVelocity;

    private int mOverflingDistance;

    private int mOverscrollDistance;

    private float mPreviousX = 0;

    private int mScrollRange;

    private int mTouchSlop;

    private VelocityTracker mVelocityTracker;

    private OverScroller mScroller;

    private int mSkippedIconCount = 0;


// ****************** //
// Constructor        //
// ****************** //

    public HorizontalIconView(Context context) {
        super(context);
        init(context);
    }


    public HorizontalIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HorizontalIconView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

// ****************** //
// Private methods    //
// ****************** //

// Initialization

    private void init(Context context) {

        final Resources res = context.getResources();
        mIconSize = res.getDimensionPixelSize(R.dimen.icon_size);
        mIconSpacing = res.getDimensionPixelSize(R.dimen.icon_spacing);

        final ViewConfiguration config = ViewConfiguration.get(context);
        mTouchSlop = config.getScaledTouchSlop();
        mMinimumVelocity = config.getScaledMinimumFlingVelocity();
        mMaximumVelocity = config.getScaledMaximumFlingVelocity();
        mOverflingDistance = config.getScaledOverflingDistance();
        mOverscrollDistance = config.getScaledOverscrollDistance();

        // verify this View will be drawn
        setWillNotDraw(false);

        mEdgeEffectLeft = new EdgeEffectCompat(context);
        mEdgeEffectRight = new EdgeEffectCompat(context);
        mScroller = new OverScroller(context);
        setFocusable(true);
    }

    // keep track of the right pointer if several touch are happening simultaneously
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);

        if(pointerId == mActivePointerId){
            final int newPointerIndex = pointerIndex == 0? 1 : 0;
            mPreviousX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if(mVelocityTracker != null){
                mVelocityTracker.clear();
            }
        }
    }

    /**
     * Flings the view horizontally with the specified velocity
     * @param velocity
     */
    private void fling(int velocity){
        if(mScrollRange == 0){
            return;
        }
        final int halfWidth = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2;
        mScroller.fling(getScrollX(), 0, velocity, 0, 0, mScrollRange, 0, 0, halfWidth, 0);
        invalidate();
    }

    /**
     * Measures height according to the passed measure spec
     * @param measureSpec
     * @return
     */
    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int result = 0;
        if(specMode == MeasureSpec.EXACTLY){
            result = specSize;
        }
        else{
            result = mIconSize + getPaddingTop() + getPaddingBottom();
            if(specMode == MeasureSpec.AT_MOST){
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * Measures width according to the passed measure spec
     * @param measureSpec
     * @return
     */
    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        // Calculate maximum size
        final int icons = (mDrawables == null) ? 0 : mDrawables.size();
        final int iconSpace = mIconSize * icons;
        final int dividerSpace;
        if (icons <= 1) {
            dividerSpace = 0;
        } else {
            dividerSpace = (icons - 1) * mIconSpacing;
        }
        final int maxSize = dividerSpace + iconSpace + getPaddingLeft() + getPaddingRight();

        // Calculate actual size
        int result = 0;
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(maxSize, specSize);
            } else {
                result = maxSize;
            }
        }

        if (maxSize > result) {
            mScrollRange = maxSize - result;
        } else {
            mScrollRange = 0;
        }

        return result;
    }

// ********************* //
// Protected methods     //
// ********************* //

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawables == null || mDrawables.isEmpty()) {
            return;
        }

        final int width = getWidth();
        final int paddingBottom = getPaddingBottom();
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();

        // Determine edges of visible content
        final int leftEdge = getScrollX();
        final int rightEdge = leftEdge + width;

        int left = paddingLeft;
        int top = paddingTop;
        mSkippedIconCount = 0;

        final int iconCount = mDrawables.size();
        for (int i = 0; i < iconCount; i++) {
            if (left + mIconSize < leftEdge) {
                // Icon is too far left to be seen
                left = left + mIconSize + mIconSpacing;
                mSkippedIconCount++;
                continue;
            }

            if (left > rightEdge) {
                // All remaining icons are right of the view
                break;
            }

            // Get a reference to the icon to be drawn
            final Drawable icon = mDrawables.get(i);
            icon.setBounds(left, top, left + mIconSize, top + mIconSize);
            icon.draw(canvas);

            // Icon was drawn, so track position
            final int drawnPosition = i - mSkippedIconCount;
            if (drawnPosition + 1 > mIconPositions.size()) {
                final Rect rect = icon.copyBounds();
                mIconPositions.add(rect);
            } else {
                final Rect rect = mIconPositions.get(drawnPosition);
                icon.copyBounds(rect);
            }

            // Update left position
            left = left + mIconSize + mIconSpacing;
        }

        if (mEdgeEffectLeft != null) {
            if (!mEdgeEffectLeft.isFinished()) {
                final int restoreCount = canvas.save();
                final int height = getHeight() - paddingTop - paddingBottom;

                canvas.rotate(270);
                canvas.translate(-height + paddingTop, Math.min(0, leftEdge));
                mEdgeEffectLeft.setSize(height, getWidth());
                if (mEdgeEffectLeft.draw(canvas)) {
                    postInvalidateOnAnimation();
                }
                canvas.restoreToCount(restoreCount);
            }
            if (!mEdgeEffectRight.isFinished()) {
                final int restoreCount = canvas.save();
                final int height = getHeight() - paddingTop - paddingBottom;

                canvas.rotate(90);
                canvas.translate(-paddingTop, -(Math.max(mScrollRange, leftEdge) + width));
                mEdgeEffectRight.setSize(height, width);
                if (mEdgeEffectRight.draw(canvas)) {
                    postInvalidateOnAnimation();
                }
                canvas.restoreToCount(restoreCount);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if(mScroller.isFinished()){
            super.scrollTo(scrollX, scrollY);
        }
        else{
            setScrollX(scrollX);
            if(clampedX){
                mScroller.springBack(scrollX, 0, 0, mScrollRange, 0, 0);
            }
        }
    }

// ****************** //
// Public methods     //
// ****************** //

    public void setDrawables(List<Drawable> drawables) {
        if(mDrawables == null){
            if(drawables == null){
                return;
            }
        }
        else if(drawables == null){
            requestLayout();
            mDrawables = null;
            return;
        }
        else if(mDrawables.size() == drawables .size()){
            invalidate();
        }
        else{
            requestLayout();
        }
        mDrawables = new ArrayList<Drawable>(drawables);
        mIconPositions.clear();
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            int oldX = getScrollX();
            int x = mScroller.getCurrX();

            if(oldX != x){
                // int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent
                overScrollBy(x - oldX, 0, oldX, 0, mScrollRange, 0, mOverflingDistance, 0, false);
                onScrollChanged(x, 0, oldX, 0);

                if(x < 0 && oldX >= 0){
                    mEdgeEffectLeft.onAbsorb((int) mScroller.getCurrVelocity());
                }
                else if (x > mScrollRange && oldX <= mScrollRange){
                    mEdgeEffectRight.onAbsorb((int) mScroller.getCurrVelocity());
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mVelocityTracker == null){
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action){
            case MotionEvent.ACTION_DOWN:{
                if(!mScroller.isFinished()){
                    mScroller.abortAnimation();
                }

                // Remember where the motion event started
                mPreviousX = (int) MotionEventCompat.getX(ev, 0);
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                break;
            }

            case MotionEvent.ACTION_MOVE:{
                final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if(activePointerIndex == INVALID_POINTER){
                    Log.e(TAG, "Invalid pointerId= " + mActivePointerId+" in TouchEvent");
                    break;
                }

                final int x = (int) MotionEventCompat.getX(ev, 0);
                int deltaX = (int) (mPreviousX - x);
                if (!mIsBeingDragged && Math.abs(deltaX) > mTouchSlop){
                    mIsBeingDragged = true;
                    if(deltaX > 0){
                        deltaX -= mTouchSlop;
                    }
                    else{
                        deltaX += mTouchSlop;
                    }
                }

                if(mIsBeingDragged){
                    // Scroll to follow the motion event
                    mPreviousX = x;

                    final int oldX = getScrollX();
                    final int range = mScrollRange;

                    if(overScrollBy(deltaX, 0, oldX, 0, range, 0, mOverflingDistance, 0, true)){
                        // Break our velocity if we hit a scroll barrier.
                        mVelocityTracker.clear();
                    }

                    if(mEdgeEffectLeft != null){
                        final int pulledToX = oldX + deltaX;
                        if(pulledToX < 0){
                            mEdgeEffectLeft.onPull((float) deltaX / getWidth());
                            if(!mEdgeEffectRight.isFinished()){
                                mEdgeEffectRight.onRelease();
                            }
                        }
                        else if (pulledToX > range) {
                            mEdgeEffectRight.onPull((float) deltaX / getWidth());
                            if (!mEdgeEffectLeft.isFinished()) {
                                mEdgeEffectLeft.onRelease();
                            }
                        }

                        if(!mEdgeEffectLeft.isFinished() || !mEdgeEffectRight.isFinished()){
                            postInvalidateOnAnimation();
                        }
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP:{
                if(mIsBeingDragged){
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) mVelocityTracker.getXVelocity(mActivePointerId);

                    if( (Math.abs(initialVelocity)> mMinimumVelocity)){
                        fling(-initialVelocity);
                    }
                    else{
                        if(mScroller.springBack(getScrollX(), 0,0,mScrollRange, 0,0)){
                            postInvalidateOnAnimation();
                        }
                    }

                    mActivePointerId = INVALID_POINTER;
                    mIsBeingDragged = false;
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;

                    if(mEdgeEffectLeft != null){
                        mEdgeEffectLeft.onRelease();
                        mEdgeEffectRight.onRelease();
                    }
                }
                else{
                    final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                    if(activePointerIndex == INVALID_POINTER){
                        return false;
                    }
                    final int x = (int) ev.getX(activePointerIndex) + getScrollX();
                    final int y = (int) ev.getY(activePointerIndex);

                    int i =0;

                    for(Rect rect: mIconPositions){
                        if(rect.contains(x, y)){
                            final int position = i + mSkippedIconCount;
                            Toast.makeText(getContext(), "Pressed icon "+position+ "; rect count: "+mIconPositions.size(), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        i++;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:{
                if(mIsBeingDragged){
                    if(mScroller.springBack(getScrollX(), 0, 0, mScrollRange, 0, 0)){
                        postInvalidateOnAnimation();
                    }
                    mActivePointerId = INVALID_POINTER;
                    mIsBeingDragged = false;
                    if(mVelocityTracker != null){
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }

                    if(mEdgeEffectLeft != null){
                        mEdgeEffectLeft.onRelease();
                        mEdgeEffectRight.onRelease();
                    }
                }
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:{
                onSecondaryPointerUp(ev);
                break;
            }
        }
        return true;
    }
}
