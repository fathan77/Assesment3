package org.d3if0154.assesment3.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.d3if0154.assesment3.model.Film
import org.d3if0154.assesment3.model.OpStatus
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.DELETE

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

private const val BASE_URL = "https://34b6-2404-8000-1024-1a47-9426-6cdf-5e94-e2e4.ngrok-free.app/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()


private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface FilmApiService {
    @GET("/film")
    suspend fun getFilm(
        @Header("Authorization") userId: String
    ): List<Film>
    @Multipart
    @POST("/bikin-film")
    suspend fun postFilm(
        @Header("Authorization") userId: String,
//        @Part("id") id: RequestBody,
        @Part("namaFilm") namaFilm: RequestBody,
        @Part("namaPembuat") namaPembuat: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("mine") mine: RequestBody
    ): OpStatus
    @DELETE("/hapus-film")
    suspend fun deleteFilm(
        @Header("Authorization") userId: String,
        @Query ("id") id: String
    ): OpStatus
    abstract fun postFilm(userId: String, namaFilm: RequestBody, namaPembuat: RequestBody, image: MultipartBody.Part): OpStatus
//    abstract fun postFilm (userId: String, namaFilm: RequestBody, namaPembuat: RequestBody, image: MultipartBody.Part): OpStatus
//    abstract fun postFilm(userId: String, namaFilm: RequestBody, namaPembuat: RequestBody, image: MultipartBody.Part): OpStatus
}


object FilmApi {
    val service: FilmApiService by lazy {
        retrofit.create(FilmApiService::class.java)
    }

    fun getFilmUrl(imageId: String): String {
        return "$BASE_URL/$imageId"
    }

    fun postFilmUrl(): String {
        return "$BASE_URL/bikin-film"
    }

    enum class ApiStatus { LOADING, FAILED, SUCCESS }
}
