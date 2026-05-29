package com.hayatkoprusu.ui

import android.app.Application
import androidx.lifecycle.*
import com.hayatkoprusu.data.AppDatabase
import com.hayatkoprusu.data.MessageEntity
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.messageDao()

    val allMessages: LiveData<List<MessageEntity>> = dao.getAllMessages().asLiveData()

    fun sendMessage(message: MessageEntity) {
        viewModelScope.launch {
            dao.insertMessage(message)
        }
    }
}
