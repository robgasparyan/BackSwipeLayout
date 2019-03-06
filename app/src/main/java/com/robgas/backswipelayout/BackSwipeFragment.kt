package com.robgas.backswipelayout

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.back_swipe_fragment_layout.*

class BackSwipeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.back_swipe_fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backSwipeLayout.dragDirection = BackSwipeLayout.DragDirections.START
        backSwipeLayout.parentScreenState = BackSwipeLayout.ParentScreenState.FRAGMENT
    }

    companion object {

        fun newInstance(): BackSwipeFragment {
            return BackSwipeFragment()
        }
    }

}