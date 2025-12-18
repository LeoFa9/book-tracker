package com.example.laba_9

import retrofit2.Call
import retrofit2.http.*

interface BookApi {
    @GET("books")
    fun getBooks(): Call<List<Book>>

    @POST("books")
    fun createBook(@Body book: Book): Call<Book>

    @GET("books/{id}")
    fun getBook(@Path("id") id: Int): Call<Book>

    @PUT("books/{id}")
    fun updateBook(@Path("id") id: Int, @Body book: Book): Call<Book>

    @DELETE("books/{id}")
    fun deleteBook(@Path("id") id: Int): Call<Unit>
}



