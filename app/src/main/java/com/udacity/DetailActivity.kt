package com.udacity

import android.app.NotificationManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        val fileName = findViewById<TextView>(R.id.fileNameValueText)
        val fileStatus = findViewById<TextView>(R.id.statusValue)
        val button = findViewById<TextView>(R.id.button)

        fileName.text = intent.getStringExtra(getString(R.string.file_name_extra_text))
        fileStatus.text = intent.getStringExtra(getString(R.string.file_status_extra_text))
        val notificationId = intent.getIntExtra(getString(R.string.notification_id_extra_text),0)

        //Eliminar notificacion desde aqui
        notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java) as NotificationManager
        notificationManager.cancel(notificationId)

        button.setOnClickListener(){
            this.onBackPressed()
        }
    }
}
