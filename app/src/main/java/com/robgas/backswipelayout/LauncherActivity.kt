package com.robgas.backswipelayout

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityButton.setOnClickListener {
            startActivity(Intent(this, BackSwipeActivity::class.java))
        }
        fragmentButton.setOnClickListener {
            supportFragmentManager.beginTransaction().add(
                R.id.fragmentContainer,
                BackSwipeFragment.newInstance()
            ).commit()
        }
    }
}
