package com.decomp.comp.decomp.features.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.SplashScreen
import com.decomp.comp.decomp.features.record_screen.RecordScreenActivity
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by lazy {
        ViewModelProvider(this).get(HomeViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        vp_tasks.adapter = TasksListAdapter(viewModel.getTasks(), this::onTaskSelect)
    }

    private fun onTaskSelect(task: Task) {
        when (task.taskType) {
            TaskType.COMPRESS_IMAGE -> startActivity(Intent(this, SplashScreen::class.java))
            TaskType.RECORD_SCREEN -> startActivity(Intent(this, RecordScreenActivity::class.java))
        }
    }
}