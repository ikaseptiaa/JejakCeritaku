package com.example.jejakceritaku.view.detail

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.jejakceritaku.R
import com.example.jejakceritaku.data.response.Story
import com.example.jejakceritaku.databinding.ActivityDetailStoryBinding
import com.example.jejakceritaku.view.ViewModelFactory

class DetailStoryActivity : AppCompatActivity() {
    private val viewModel: DetailStoryViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val id = intent.getStringExtra(EXTRA_ID).toString()

        showLoading(true)
        observeSession(id)
        observeDetailStory()
    }

    private fun observeDetailStory() {
        viewModel.detail.observe(this) { story ->
            if (story != null) {
                displayStoryDetails(story)
                showLoading(false)
            }
        }
    }

    private fun displayStoryDetails(story: Story) {
        Glide.with(this@DetailStoryActivity)
            .load(story.photoUrl)
            .into(binding.ivDetail)
        binding.cvName.text = story.name
        binding.cvDesc.text = story.description
    }

    private fun observeSession(id: String) {
        viewModel.getSession().observe(this) { user ->
            val token = user.token
            viewModel.getDetailStory(token, id)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val TAG = "DetailStoryActivity"
    }
}
