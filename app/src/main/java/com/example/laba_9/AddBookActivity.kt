package com.example.laba_9

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AddBookActivity : AppCompatActivity() {
    private lateinit var editTextTitle: EditText
    private lateinit var editTextYear: EditText
    private lateinit var editTextAuthor: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var editTextNote: EditText
    private lateinit var buttonSave: Button
    private val CHANNEL_ID = "book_tracker_channel"
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_book)
        createNotificationChannel()
        requestNotificationPermission()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editTextTitle = findViewById(R.id.editTextBookTitle)
        editTextYear = findViewById(R.id.editTextYear)
        editTextAuthor = findViewById(R.id.editTextAuthor)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        editTextNote = findViewById(R.id.editTextNote)
        buttonSave = findViewById(R.id.buttonSave)

        val statusOptions = arrayOf("Хочу прочитать", "Читаю", "Бросил", "Прочитал")
        val statusValues = arrayOf("want", "reading", "dropped", "read")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, statusOptions)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerStatus.adapter = adapter

        buttonSave.setOnClickListener {
            saveBook(statusValues[spinnerStatus.selectedItemPosition])
        }
    }

    private fun saveBook(status: String) {
        val title = editTextTitle.text.toString().trim()
        val yearStr = editTextYear.text.toString().trim()
        val author = editTextAuthor.text.toString().trim()
        val note = editTextNote.text.toString().trim()

        if (title.isEmpty() || author.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show()
            return
        }

        val year = if (yearStr.isEmpty()) {
            0
        } else {
            try {
                yearStr.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Некорректный год", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val dateAdded = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val book = Book(
            title = title,
            year = year,
            author = author,
            status = status,
            dateAdded = dateAdded,
            note = note
        )

        Log.d("AddBookActivity", "createBook request: $book")
        ApiClient.bookApi.createBook(book).enqueue(object : Callback<Book> {
            override fun onResponse(call: Call<Book>, response: Response<Book>) {
                if (response.isSuccessful) {
                    Log.d("AddBookActivity", "createBook success: ${response.body()}")
                    showNotification("Книга добавлена", "Книга \"${title}\" успешно добавлена в список")
                    Toast.makeText(this@AddBookActivity, "Книга добавлена", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e("AddBookActivity", "createBook error code=${response.code()}")
                    Toast.makeText(this@AddBookActivity, "Ошибка: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Book>, t: Throwable) {
                Log.e("AddBookActivity", "createBook onFailure", t)
                Toast.makeText(this@AddBookActivity, "Ошибка подключения: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Трекер книг",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о действиях с книгами"
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        // Проверяем разрешение для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                Log.w("AddBookActivity", "Notification permission not granted")
                return
            }
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        
        // Проверяем, что канал существует
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (channel == null) {
                Log.e("AddBookActivity", "Notification channel not found, recreating...")
                createNotificationChannel()
            }
        }

        val notificationId = System.currentTimeMillis().toInt()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()
        
        try {
            notificationManager.notify(notificationId, notification)
            Log.d("AddBookActivity", "Notification sent: $title - $message")
        } catch (e: Exception) {
            Log.e("AddBookActivity", "Failed to show notification", e)
        }
    }
}

