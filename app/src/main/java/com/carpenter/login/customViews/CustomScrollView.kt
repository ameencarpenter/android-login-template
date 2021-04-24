package com.carpenter.login.customViews

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView

//extended to add disabling scrolling functionality
class CustomScrollView : NestedScrollView {
    var isScrollable = true

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (isScrollable) super.onTouchEvent(ev)
        else false
    }

    override fun onInterceptHoverEvent(event: MotionEvent): Boolean {
        return if (isScrollable) super.onInterceptHoverEvent(event)
        else false
    }
}