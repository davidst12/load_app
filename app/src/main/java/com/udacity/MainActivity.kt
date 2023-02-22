package com.udacity

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var urlSelected: String? = null
    private var fileNameSelected: String = ""
    private var fileStatusSelected: String = ""

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    private lateinit var downloadManager: DownloadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java) as NotificationManager
        createChannel(CHANNEL_ID, getString(R.string.channel_name))

        custom_button.setOnClickListener {
            if(urlSelected!=null){
                Timber.i("Button clicked")
                custom_button.buttonState = ButtonState.Clicked
                download()
            }else{
                Toast.makeText(this,"Please select the file to download",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createChannel(channelId: String, channelName: String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            notificationChannel.description = getString(R.string.notification_channel_description)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun NotificationManager.sendNotification(applicationContext: Context){

        val contentIntent = Intent(applicationContext, DetailActivity::class.java)
        contentIntent.putExtra(getString(R.string.file_name_extra_text), fileNameSelected)
        contentIntent.putExtra(getString(R.string.file_status_extra_text), fileStatusSelected)
        contentIntent.putExtra(getString(R.string.notification_id_extra_text), notificationID)
        pendingIntent = PendingIntent.getActivity(applicationContext, notificationID, contentIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        action = NotificationCompat.Action.Builder(R.drawable.ic_assistant_black_24dp, getString(R.string.notification_button_text), pendingIntent).build()

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_description))
            .addAction(action)
            .setAutoCancel(true)

        notify(notificationID, builder.build())
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            id.let {
                downloadManager.query(DownloadManager.Query().setFilterById(id!!)).use {
                    if(it.moveToFirst()){
                        custom_button.buttonState = ButtonState.Completed
                        when (it.getInt(it.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                            DownloadManager.STATUS_SUCCESSFUL ->{
                                fileStatusSelected = getString(R.string.text_status_succed)
                            }
                            DownloadManager.STATUS_FAILED -> {
                                fileStatusSelected = getString(R.string.text_status_failed)
                            }
                        }
                        notificationManager.sendNotification(context!!)
                    }
                }
            }
        }
    }

    private fun download() {
        custom_button.buttonState = ButtonState.Loading
        val request =
            DownloadManager.Request(Uri.parse(urlSelected))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        // Check internet connection before start downloading
        if (networkInfo != null && networkInfo.isAvailable) {
            downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        }
        else {
            Toast.makeText(this, "Can not connect to the internet", Toast.LENGTH_LONG).show()
            custom_button.buttonState = ButtonState.Completed
        }
    }

    fun onRadioButtonClicked(view: View) {
        when (view.id) {
            R.id.urlGlideButton ->{
                Timber.i("glideURL selected")
                urlSelected = glideURL
                fileNameSelected = getString(R.string.text_glide)
            }
            R.id.urlUdacityButton ->{
                Timber.i("udacityURL selected")
                urlSelected = udacityURL
                fileNameSelected = getString(R.string.text_load_app)
            }
            R.id.urlRetrofitButton ->{
                Timber.i("retrofitURL selected")
                urlSelected = retrofitURL
                fileNameSelected = getString(R.string.text_retrofit)
            }
        }
    }

    companion object {
        private const val udacityURL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val glideURL =
            "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
        private const val retrofitURL =
            "https://github.com/square/retrofit/archive/refs/heads/master.zip"
        private const val CHANNEL_ID = "channelId"
        private const val notificationID = 1
    }
}
