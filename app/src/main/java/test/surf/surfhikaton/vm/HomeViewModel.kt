package test.surf.surfhikaton.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.beust.klaxon.Klaxon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import test.surf.surfhikaton.model.CatHolder
import test.surf.surfhikaton.model.dto.CatRandomResponse
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _catState: MutableSharedFlow<CatState> = MutableSharedFlow(replay = 1, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val catState: SharedFlow<CatState> = _catState.asSharedFlow()

    private val _counterFlow = MutableSharedFlow<Int>(replay = 1, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val counterFlow: SharedFlow<Int> = _counterFlow.asSharedFlow()

    fun updateData(){
        viewModelScope.launch(Dispatchers.IO) {
            _catState.emit(CatState.Loading)

            _catState.emit(CatState.Success(
                combineCats(getFakeCats(3), getRealCats(1))
            ))
        }
    }

    fun onClickCat(isFake: Boolean){
        if (isFake){
            changeCounter(-5)
        } else {
            changeCounter(1)
            updateData()
        }
    }

    private fun changeCounter(value: Int){
        viewModelScope.launch(Dispatchers.IO) {
            val score = _counterFlow.replayCache.getOrNull(0) ?: 0
            _counterFlow.emit(score + value)
        }
    }

    private fun combineCats(fakeCats: List<CatHolder>, realCats: List<CatHolder>) : List<CatHolder>{
        val list = mutableListOf<CatHolder>()

        list.apply {
            addAll(fakeCats)
            addAll(realCats)
            shuffle()
        }

        return list
    }

    private fun getRealCats(num: Int = 1) : List<CatHolder>{
        val list = mutableListOf<CatHolder>()
        for(i in 1..num){
            list.add(CatHolder(getRealBitmap()!!, isFake = false))//TODO Обработать ошибки
        }
        return list
    }

    private suspend fun getFakeCats(num: Int = 3) : List<CatHolder>{
        val list = mutableListOf<CatHolder>()
        for(i in 1..num){
            list.add(CatHolder(getFakeBitmap()!!, isFake = true))//TODO Обработать ошибки
            delay(1000) // Необходимое время для генерации нового котика
        }
        return list
    }

    private fun getFakeBitmap(): Bitmap? =
        try {
            BitmapFactory.decodeStream(urlConnection("https://thiscatdoesnotexist.com/").inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

    private fun getRealBitmap(): Bitmap? =
        try {
            val apiConnection = urlConnection("https://api.thecatapi.com/v1/images/search")
            val resultUrl = Klaxon().parseArray<CatRandomResponse>(apiConnection.inputStream)!![0].url
            apiConnection.disconnect()

            val imgConnection = urlConnection(resultUrl)
            BitmapFactory.decodeStream(imgConnection.inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

    private fun urlConnection(urlStr: String) : HttpURLConnection{
        val url = URL(urlStr)
        val connection: HttpURLConnection = url
            .openConnection() as HttpURLConnection

        connection.apply {
            doInput = true
            connect()
        }

        return connection
    }

}

sealed class CatState{
    class Error(val msg: String = "Случилось что-то очень плохое, но я не знаю что :(") : CatState()
    class Success(val dataList: List<CatHolder>) : CatState()
    object Loading : CatState()

}