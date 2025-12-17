package com.example.laba_9

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailActivity : AppCompatActivity() {
    private lateinit var book: Book
    private lateinit var editTextTitle: EditText
    private lateinit var editTextYear: EditText
    private lateinit var editTextAuthor: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var editTextNote: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonDelete: Button
    private var isEditMode = false
    private val CHANNEL_ID = "book_tracker_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)
        createNotificationChannel()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        book = intent.getParcelableExtra("book") ?: return

        editTextTitle = findViewById(R.id.editTextBookTitle)
        editTextYear = findViewById(R.id.editTextYear)
        editTextAuthor = findViewById(R.id.editTextAuthor)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        editTextNote = findViewById(R.id.editTextNote)
        buttonSave = findViewById(R.id.buttonSave)
        buttonDelete = findViewById(R.id.buttonDelete)

        val statusOptions = arrayOf("Хочу прочитать", "Читаю", "Бросил", "Прочитал")
        val statusValues = arrayOf("want", "reading", "dropped", "read")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, statusOptions)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerStatus.adapter = adapter

        loadBookDetails()
        setEditMode(false)

        buttonSave.setOnClickListener {
            if (isEditMode) {
                updateBook(statusValues[spinnerStatus.selectedItemPosition])
            } else {
                setEditMode(true)
            }
        }

        buttonDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadBookDetails() {
        editTextTitle.setText(book.title)
        editTextYear.setText(if (book.year > 0) book.year.toString() else "")
        editTextAuthor.setText(book.author)
        editTextNote.setText(book.note)

        val statusIndex = when (book.status) {
            "want" -> 0
            "reading" -> 1
            "dropped" -> 2
            "read" -> 3
            else -> {
                Log.w("DetailActivity", "Unknown status: ${book.status}, defaulting to 0")
                0
            }
        }
        spinnerStatus.setSelection(statusIndex, false)
        Log.d("DetailActivity", "Loaded book status: ${book.status}, index: $statusIndex")
    }

    private fun setEditMode(enabled: Boolean) {
        isEditMode = enabled
        editTextTitle.isEnabled = enabled
        editTextYear.isEnabled = enabled
        editTextAuthor.isEnabled = enabled
        spinnerStatus.isEnabled = enabled
        editTextNote.isEnabled = enabled
        buttonSave.text = if (enabled) "Сохранить" else "Редактировать"
    }

    private fun updateBook(status: String) {
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

        val updatedBook = Book(
            id = book.id,
            title = title,
            year = year,
            author = author,
            status = status,
            dateAdded = book.dateAdded,
            note = note
        )

        val bookId = book.id ?: return
        Log.d("DetailActivity", "updateBook request: id=$bookId, book=$updatedBook")
        ApiClient.bookApi.updateBook(bookId, updatedBook).enqueue(object : Callback<Book> {
            override fun onResponse(call: Call<Book>, response: Response<Book>) {
                if (response.isSuccessful) {
                    Log.d("DetailActivity", "updateBook success: ${response.body()}")
                    book = response.body() ?: book
                    loadBookDetails() // Обновляем отображение, включая статус
                    showNotification("Книга обновлена", "Книга \"${book.title}\" успешно обновлена")
                    Toast.makeText(this@DetailActivity, "Книга обновлена", Toast.LENGTH_SHORT).show()
                    setEditMode(false)
                } else {
                    Log.e("DetailActivity", "updateBook error code=${response.code()}")
                    Toast.makeText(this@DetailActivity, "Ошибка: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Book>, t: Throwable) {
                Log.e("DetailActivity", "updateBook onFailure", t)
                Toast.makeText(this@DetailActivity, "Ошибка подключения: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Удаление книги")
            .setMessage("Вы уверены, что хотите удалить эту книгу?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteBook()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteBook() {
        val bookId = book.id ?: return
        Log.d("DetailActivity", "deleteBook request: id=$bookId")
        ApiClient.bookApi.deleteBook(bookId).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    Log.d("DetailActivity", "deleteBook success")
                    showNotification("Книга удалена", "Книга \"${book.title}\" удалена из списка")
                    Toast.makeText(this@DetailActivity, "Книга удалена", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e("DetailActivity", "deleteBook error code=${response.code()}")
                    Toast.makeText(this@DetailActivity, "Ошибка: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e("DetailActivity", "deleteBook onFailure", t)
                Toast.makeText(this@DetailActivity, "Ошибка подключения: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

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

