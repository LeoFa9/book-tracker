package com.example.laba_9

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookAdapter(
    private val books: MutableList<Book>,
    private val onItemClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        val statusTextView: TextView = itemView.findViewById(R.id.textViewStatus)
        val yearTextView: TextView = itemView.findViewById(R.id.textViewYear)
        val authorTextView: TextView = itemView.findViewById(R.id.textViewAuthor)
        val noteTextView: TextView = itemView.findViewById(R.id.textViewNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.titleTextView.text = book.title
        holder.yearTextView.text = if (book.year > 0) book.year.toString() else "—"
        holder.authorTextView.text = book.author
        holder.noteTextView.text = book.note

        val statusText = when (book.status) {
            "want" -> "Хочу прочитать"
            "reading" -> "Читаю"
            "dropped" -> "Бросил"
            "read" -> "Прочитал"
            else -> book.status.ifEmpty { "Не указан" }
        }
        holder.statusTextView.text = statusText
        holder.statusTextView.visibility = View.VISIBLE

        holder.itemView.setOnClickListener {
            onItemClick(book)
        }
    }

    override fun getItemCount(): Int = books.size

    fun updateBooks(newBooks: List<Book>) {
        books.clear()
        books.addAll(newBooks)
        notifyDataSetChanged()
    }
}


