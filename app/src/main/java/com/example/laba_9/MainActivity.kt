package com.example.laba_9

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookAdapter
    private val books = mutableListOf<Book>()
    private val KEY_BOOKS = "books"
    private val CHANNEL_ID = "book_tracker_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        createNotificationChannel()
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerViewBooks)
        adapter = BookAdapter(books) { book ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("book", book)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val buttonAddBook = findViewById<Button>(R.id.buttonAddBook)
        buttonAddBook.setOnClickListener {
            val intent = Intent(this, AddBookActivity::class.java)
            startActivity(intent)
        }

        if (savedInstanceState != null) {
            val savedBooks = savedInstanceState.getParcelableArrayList<Book>(KEY_BOOKS)
            if (savedBooks != null) {
                books.clear()
                books.addAll(savedBooks)
                adapter.notifyDataSetChanged()
            }
        } else {
            loadBooks()
        }
    }

    override fun onResume() {
        super.onResume()
        loadBooks()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(KEY_BOOKS, ArrayList(books))
    }

    private fun loadBooks() {
        Log.d("MainActivity", "loadBooks request")
        ApiClient.bookApi.getBooks().enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                if (response.isSuccessful) {
                    val loadedBooks = response.body() ?: emptyList()
                    Log.d("MainActivity", "loadBooks onResponse code=${response.code()} bodySize=${loadedBooks.size}")
                    loadedBooks.forEach { book ->
                        Log.d("MainActivity", "Book: id=${book.id}, title=${book.title}, status=${book.status}")
                    }
                    val sortedBooks = sortBooks(loadedBooks)
                    books.clear()
                    books.addAll(sortedBooks)
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e("MainActivity", "loadBooks onResponse error code=${response.code()}")
                    Toast.makeText(this@MainActivity, "Ошибка загрузки: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Book>>, t: Throwable) {
                Log.e("MainActivity", "loadBooks onFailure", t)
                Toast.makeText(this@MainActivity, "Ошибка подключения: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sortBooks(list: List<Book>): List<Book> =
        list.sortedWith(
            compareByDescending<Book> { 
                try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.dateAdded)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }
            .thenByDescending { it.id ?: 0 }
        )

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Трекер книг",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления о действиях с книгами"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
