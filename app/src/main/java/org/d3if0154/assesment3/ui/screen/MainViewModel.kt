package org.d3if0154.assesment3.ui.screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.d3if0154.assesment3.model.Film
import org.d3if0154.assesment3.network.FilmApi
import java.io.ByteArrayOutputStream

class MainViewModel:ViewModel() {
    var data = mutableStateOf(emptyList<Film>())
        private set

    var status = MutableStateFlow(FilmApi.ApiStatus.LOADING)
        private set
    var errorMessage = mutableStateOf<String?>(null)
        private set
    fun retrieveData(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = FilmApi.ApiStatus.LOADING
            try {
                data.value = FilmApi.service.getFilm(userId)
                status.value = FilmApi.ApiStatus.SUCCESS
            }
            catch (e: Exception) {
                Log.e("MainViewModel", "Failure: ${e.message}")
                status.value = FilmApi.ApiStatus.FAILED
            }
        }
    }
    fun saveData(userId: String, namaFilm: String, namaPembuat: String, bitmap: Bitmap, mine: Int = 1){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = FilmApi.service.postFilm(
                    userId = userId,
                    namaFilm = namaFilm.toRequestBody("text/plain".toMediaTypeOrNull()),
                    namaPembuat = namaPembuat.toRequestBody("text/plain".toMediaTypeOrNull()),
                    image = bitmap.toMultipartBody(),
                    mine = mine.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                )
                if (result.status == "success")
                    retrieveData(userId = userId)
                else
                    throw Exception(result.message)
            }catch (e: Exception){
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun deletingData(userId: String,id: Long){
        viewModelScope.launch(Dispatchers.IO){
            try {
                val result = FilmApi.service.deleteFilm(userId = userId, id = id.toString())
                if (result.status == "success")
                    retrieveData(userId = userId)
                else
                    throw Exception(result.message)
            }catch (e: Exception){
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    private fun Bitmap.toMultipartBody(): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody(
            "image/jpg".toMediaTypeOrNull(), 0, byteArray.size)
        return MultipartBody.Part.createFormData(
            "image", "image.jpg", requestBody)
    }

    fun clearMessage() { errorMessage.value = null }
}