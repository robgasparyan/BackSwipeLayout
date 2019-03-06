package com.robgas.backswipelayout

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.back_swipe_activity_layout.*

class BackSwipeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.back_swipe_activity_layout)
        backSwipeLayout.dragDirection = BackSwipeLayout.DragDirections.START
    }
}