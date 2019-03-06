package com.robgas.backswipelayout

import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.ViewDragHelper
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.AbsListView
import android.widget.ScrollView

class BackSwipeLayout : ViewGroup {

    enum class DragMode {
        EDGE,
        VERTICAL,
    }

    enum class ParentScreenState {
        ACTIVITY,
        FRAGMENT,
    }

    constructor(context: Context) : super(context) {
        viewDragHelper = ViewDragHelper.create(this, 1.0f, ViewDragHelperCallBack())
        checkDragable()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        viewDragHelper = ViewDragHelper.create(this, 1.0f, ViewDragHelperCallBack())
        checkDragable()
    }

    enum class DragDirections {
        START,

        TOP,

        END,

        BOTTOM
    }

    private var dragDirectMode = DragMode.EDGE
        set(value) {
            if (value == DragMode.VERTICAL) {
                this.dragDirection = DragDirections.TOP
            }
            field = value
        }

    var parentScreenState = ParentScreenState.ACTIVITY

    var dragDirection = DragDirections.TOP

    private val FINISHED_SPEED_MAX = 2000.0

    private var viewDragHelper: ViewDragHelper

    private var targetView: View? = null

    private var scrollChild: View? = null

    private var verticalDragRange = 0

    private var horizontalDragRange = 0

    private var draggingState = 0

    private var draggingOffset: Int = 0

    private val FACTOR = 0.5f

    private var finishAnchor = 0f

    fun setFinishAnchor(offset: Float) {
        finishAnchor = offset
    }

    private var swipeBackListener: SwipeBackListener? = null

    fun setOnSwipeBackListener(listener: SwipeBackListener) {
        swipeBackListener = listener
    }

    var lastY = 0f
    var newY = 0f
    var offsetY = 0f

    var lastX = 0f
    var newX = 0f
    var offsetX = 0f

    private fun checkDragable() {
        setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                lastY = motionEvent.rawY
                lastX = motionEvent.rawX
            } else if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                newY = motionEvent.rawY
                lastX = motionEvent.rawX

                offsetY = Math.abs(newY - lastY)
                lastY = newY

                offsetX = Math.abs(newX - lastX)
                lastX = newX
            }

            false
        }
    }

    fun setScrollChild(view: View) {
        scrollChild = view
    }


    private fun ensureTarget() {
        if (targetView == null) {
            if (childCount > 1) {
                throw IllegalStateException("BackSwipeLayout should contains only one child")
            }
            targetView = getChildAt(0)

            if (scrollChild == null && targetView != null) {
                if (targetView is ViewGroup) {
                    findScrollView(targetView as ViewGroup)
                } else {
                    scrollChild = targetView
                }

            }
        }
    }


    private fun findScrollView(viewGroup: ViewGroup) {
        scrollChild = viewGroup
        if (viewGroup.childCount > 0) {
            val count = viewGroup.childCount
            var child: View
            for (i in 0 until count) {
                child = viewGroup.getChildAt(i)
                if (child is AbsListView || child is ScrollView || child is ViewPager || child is WebView) {
                    scrollChild = child
                    return
                }
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = measuredWidth
        val height = measuredHeight
        if (childCount == 0) return

        val child = getChildAt(0)

        val childWidth = width - paddingLeft - paddingRight
        val childHeight = height - paddingTop - paddingBottom
        val childLeft = paddingLeft
        val childTop = paddingTop
        val childRight = childLeft + childWidth
        val childBottom = childTop + childHeight
        child.layout(childLeft, childTop, childRight, childBottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (childCount > 1) {
            throw IllegalStateException("BackSwipeLayout must contains only one direct child.")
        }

        if (childCount > 0) {
            val measureWidth =
                View.MeasureSpec.makeMeasureSpec(measuredWidth - paddingLeft - paddingRight, View.MeasureSpec.EXACTLY)
            val measureHeight =
                View.MeasureSpec.makeMeasureSpec(measuredHeight - paddingTop - paddingBottom, View.MeasureSpec.EXACTLY)
            getChildAt(0).measure(measureWidth, measureHeight)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        verticalDragRange = h
        horizontalDragRange = w

        finishAnchor = when (dragDirection) {
            DragDirections.TOP, DragDirections.BOTTOM -> if (finishAnchor > 0) finishAnchor else verticalDragRange * FACTOR
            DragDirections.START, DragDirections.END -> if (finishAnchor > 0) finishAnchor else horizontalDragRange * FACTOR
        }
    }

    private fun getDragRange(): Int {
        return when (dragDirection) {
            DragDirections.TOP, DragDirections.BOTTOM -> verticalDragRange
            DragDirections.START, DragDirections.END -> horizontalDragRange
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        var handled = false
        ensureTarget()
        if (isEnabled) {
            handled = viewDragHelper.shouldInterceptTouchEvent(ev)
        } else {
            viewDragHelper.cancel()
        }
        return if (!handled) super.onInterceptTouchEvent(ev) else handled
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        viewDragHelper.processTouchEvent(event)
        return true
    }

    override fun computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    fun canChildScrollUp(): Boolean {
        return ViewCompat.canScrollVertically(scrollChild, -1)
    }

    fun canChildScrollDown(): Boolean {
        return ViewCompat.canScrollVertically(scrollChild, 1)
    }

    private fun canChildScrollRight(): Boolean {
        return ViewCompat.canScrollHorizontally(scrollChild, -1)
    }

    private fun canChildScrollLeft(): Boolean {
        return ViewCompat.canScrollHorizontally(scrollChild, 1)
    }

    private fun finish() {
        if (parentScreenState == ParentScreenState.ACTIVITY) {
            val act = context as AppCompatActivity
            act.finish()
        } else {
            val act = context as AppCompatActivity
            act.supportFragmentManager.beginTransaction()
                .remove(act.supportFragmentManager.fragments[act.supportFragmentManager.fragments.size - 1])
                .commitAllowingStateLoss()
            act.overridePendingTransition(0, android.R.anim.fade_out)
        }

    }

    private inner class ViewDragHelperCallBack : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child === targetView
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return verticalDragRange
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return horizontalDragRange
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {

            var result = 0

            if (dragDirectMode == DragMode.VERTICAL) {
                if (!canChildScrollUp() && top > 0) {
                    dragDirection = DragDirections.TOP
                } else if (!canChildScrollDown() && top < 0) {
                    dragDirection = DragDirections.BOTTOM
                }
            }

            if (dragDirection == DragDirections.TOP && !canChildScrollUp() && top > 0) {
                val topBound = paddingTop
                val bottomBound = verticalDragRange
                result = Math.min(Math.max(top, topBound), bottomBound)
            } else if (dragDirection == DragDirections.BOTTOM && !canChildScrollDown() && top < 0) {
                val topBound = -verticalDragRange
                val bottomBound = paddingTop
                result = Math.min(Math.max(top, topBound), bottomBound)
            }

            return result
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {

            var result = 0

            if (dragDirection == DragDirections.START && !canChildScrollRight() && left > 0) {
                val leftBound = paddingLeft
                val rightBound = horizontalDragRange
                result = Math.min(Math.max(left, leftBound), rightBound)
            } else if (dragDirection == DragDirections.END && !canChildScrollLeft() && left < 0) {
                val leftBound = -horizontalDragRange
                val rightBound = paddingLeft
                result = Math.min(Math.max(left, leftBound), rightBound)
            }

            return result
        }

        override fun onViewDragStateChanged(state: Int) {
            if (state == draggingState) return

            if ((draggingState == ViewDragHelper.STATE_DRAGGING || draggingState == ViewDragHelper.STATE_SETTLING) && state == ViewDragHelper.STATE_IDLE) {
                if (draggingOffset == getDragRange()) {
                    finish()
                }
            }

            draggingState = state
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            draggingOffset = when (dragDirection) {
                DragDirections.TOP, DragDirections.BOTTOM -> Math.abs(top)
                DragDirections.START, DragDirections.END -> Math.abs(left)
            }

            var fAnchor = draggingOffset.toFloat() / finishAnchor
            if (fAnchor >= 1) fAnchor = 1f

            var fScreen = draggingOffset.toFloat() / getDragRange().toFloat()
            if (fScreen >= 1) fScreen = 1f

            if (swipeBackListener != null) {
                swipeBackListener?.onViewPositionChanged(fAnchor, fScreen)
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            if (draggingOffset == 0) return

            if (draggingOffset == getDragRange()) return

            var isBack = false

            when {
                backBySpeed(xvel, yvel) -> isBack = !canChildScrollUp()
                draggingOffset >= finishAnchor -> isBack = true
                draggingOffset < finishAnchor -> isBack = false
            }

            val finalLeft: Int
            val finalTop: Int
            when (dragDirection) {
                DragDirections.START -> {
                    finalLeft = if (isBack) horizontalDragRange else 0
                    smoothScrollToX(finalLeft)
                }
                DragDirections.END -> {
                    finalLeft = if (isBack) -horizontalDragRange else 0
                    smoothScrollToX(finalLeft)
                }
                DragDirections.TOP -> {
                    finalTop = if (isBack) verticalDragRange else 0
                    smoothScrollToY(finalTop)
                }
                DragDirections.BOTTOM -> {
                    finalTop = if (isBack) -verticalDragRange else 0
                    smoothScrollToY(finalTop)
                }
            }

        }
    }

    private fun backBySpeed(xVelocity: Float, yVelocity: Float): Boolean {
        when (dragDirection) {
            DragDirections.TOP, DragDirections.BOTTOM -> if (Math.abs(yVelocity) > Math.abs(xVelocity) && Math.abs(
                    yVelocity
                ) > FINISHED_SPEED_MAX
            ) {
                return if (dragDirection == DragDirections.TOP) !canChildScrollUp() else !canChildScrollDown()
            }
            DragDirections.START, DragDirections.END -> if (Math.abs(xVelocity) > Math.abs(yVelocity) && Math.abs(
                    xVelocity
                ) > FINISHED_SPEED_MAX
            ) {
                return if (dragDirection == DragDirections.START) !canChildScrollLeft() else !canChildScrollRight()
            }
        }
        return false
    }

    private fun smoothScrollToX(finalLeft: Int) {
        if (viewDragHelper.settleCapturedViewAt(finalLeft, 0)) {
            ViewCompat.postInvalidateOnAnimation(this@BackSwipeLayout)
        }
    }

    private fun smoothScrollToY(finalTop: Int) {
        if (viewDragHelper.settleCapturedViewAt(0, finalTop)) {
            ViewCompat.postInvalidateOnAnimation(this@BackSwipeLayout)
        }
    }

    interface SwipeBackListener {

        fun onViewPositionChanged(fractionAnchorView: Float, fractionScreen: Float)

    }

}