package com.ciuc.andrii.youtubeclient


import android.content.Context
import android.util.Log
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.security.GeneralSecurityException
import java.util.*


class YoutubeConnector(context: Context) {

    private val youtube: YouTube = YouTube.Builder(NetHttpTransport(),
        JacksonFactory(), HttpRequestInitializer { })
        .setApplicationName(context.getString(R.string.app_name))
        .build()

    private lateinit var query: YouTube.Search.List

    companion object {
        // Your developer key goes here
        const val KEY = "AIzaSyA0nDfjU1SP11AZD1LjUdlRHsB3ozRdqIE"
    }


    private val CLIENT_SECRETS = "client_secret.json"
    private val SCOPES=
        mutableListOf<String>("https://www.googleapis.com/auth/youtube.readonly")

    private val APPLICATION_NAME = "YoutubeAPI"
    private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()


    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun getService(): YouTube? {
        val httpTransport = NetHttpTransport()
        val credential: Credential = authorize(httpTransport)
        return YouTube.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }


    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun authorize(httpTransport: NetHttpTransport?): Credential { // Load client secrets.
        val `in`: InputStream = ByteArrayInputStream(CLIENT_SECRETS.toByteArray(Charsets.UTF_8))
        val clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))
        // Build flow and trigger user authorization request.
        val flow =
            GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                SCOPES as MutableCollection<String>
            )
                .build()
        return AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user")
    }


    fun search(keywords: String?): List<VideoItem>? {
        query = youtube.search().list("id,snippet")
        query.key = KEY
        query.type = "video"
        query.fields =
            "items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)"
        query.maxResults = 50L

        query.q = keywords
        return try {
            val response = query.execute()
            val results =
                response.items
            Log.d("search_dbdfbd", results.toString())
            val items: MutableList<VideoItem> = ArrayList()
            for (result in results) {
                val item = VideoItem(
                    result.snippet.title,
                    result.snippet.description,
                    result.snippet.thumbnails.default.url,
                    result.id.videoId
                )
                items.add(item)
                Log.d("search_dbdfbd", item.title)
            }
            items
        } catch (e: IOException) {
            Log.d("YC", "Could not search: $e")
            null
        }
    }

    fun getMostPopular(): List<VideoItem>? {

        val youtubeService = getService()
        // Define and execute the API request
        // Define and execute the API request
        val request = youtubeService!!.videos()
            .list("snippet,contentDetails,statistics")
        val response = request.setChart("mostPopular")
            .setRegionCode("US")
            .execute()
        println(response)
        return try {
            val results =
                response.items
            Log.d("search_dbdfbd", results.toString())
            val items: MutableList<VideoItem> = ArrayList()
         /*   for (result in results) {
                val item = VideoItem(
                    result.snippet.title,
                    result.snippet.description,
                    result.snippet.thumbnails.default.url,
                    result.id
                )
                items.add(item)
                Log.d("search_dbdfbd", item.title)
            }*/
            items
        } catch (e: IOException) {
            Log.d("YC", "Could not search: $e")
            null
        }

        /* query = youtube.search().list("mostPopular")
         query.key = KEY
         query.type = "video"
         query.fields =
             "items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)"
         query.maxResults = 50L
         query.q = ""
         return try {
             val response = query.execute()
             val results =
                 response.items
             Log.d("search_dbdfbd", results.toString())
             val items: MutableList<VideoItem> = ArrayList()
             for (result in results) {
                 val item = VideoItem(
                     result.snippet.title,
                     result.snippet.description,
                     result.snippet.thumbnails.default.url,
                     result.id.videoId
                 )
                 items.add(item)
                 Log.d("search_dbdfbd", item.title)
             }
             items
         } catch (e: IOException) {
             Log.d("YC", "Could not search: $e")
             null
         }*/
    }


}