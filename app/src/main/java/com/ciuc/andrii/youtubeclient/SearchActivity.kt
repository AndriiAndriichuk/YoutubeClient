package com.ciuc.andrii.youtubeclient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.search_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchActivity : AppCompatActivity() {

    private var searchResults: ArrayList<VideoItem>? = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_main)




        search_input.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchOnYoutube(v.text.toString())
                return@OnEditorActionListener false
            }
            true
        })

        videos_found.onItemClickListener = OnItemClickListener { av, v, pos, id ->
            val intent = Intent(applicationContext, PlayerActivity::class.java)

            intent.putExtra("VIDEO_ID", searchResults?.get(pos)?.id)
            startActivity(intent)

        }
    }


    private fun searchOnYoutube(keywords: String) {
        CoroutineScope(IO).launch {
            val youtubeConnector = YoutubeConnector(this@SearchActivity)
            searchResults = youtubeConnector.getMostPopular(/*keywords*/) as ArrayList<VideoItem>
            withContext(Main) {
                updateVideosFound()
            }
        }
    }


    private fun updateVideosFound() {
        Log.d("search_dbdfbd", searchResults?.size.toString())
        val adapter = object :
            ArrayAdapter<VideoItem?>(
                applicationContext, R.layout.video_item,
                searchResults as List<VideoItem>
            ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var convertView: View? = convertView
                if (convertView == null) {
                    convertView = layoutInflater.inflate(R.layout.video_item, parent, false)
                }
                val thumbnail: ImageView =
                    convertView?.findViewById(R.id.video_thumbnail) as ImageView
                val title = convertView.findViewById(R.id.video_title) as TextView
                val description =
                    convertView.findViewById(R.id.video_description) as TextView
                val searchResult = searchResults?.get(position)
                // thumbnail.setImageURI(Uri.parse(searchResult.thumbnailURL))
                Glide.with(this@SearchActivity)
                    .load(searchResult?.thumbnailURL)
                    .into(thumbnail)
                title.text = searchResult?.title
                description.text = searchResult?.description
                return convertView
            }
        }
        videos_found.adapter = adapter
    }


}
