package com.hayatkoprusu.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hayatkoprusu.R
import com.hayatkoprusu.core.SqueezeCodec
import com.hayatkoprusu.data.MessageEntity

class ChatActivity : AppCompatActivity() {
    private lateinit var etMessage: EditText
    private lateinit var tvBitCounter: TextView
    private lateinit var btnSend: ImageButton
    private lateinit var rvChat: RecyclerView
    private val chatAdapter = ChatAdapter()
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        etMessage = findViewById(R.id.et_message)
        tvBitCounter = findViewById(R.id.tv_bit_counter)
        btnSend = findViewById(R.id.btn_send)
        rvChat = findViewById(R.id.rv_chat)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        rvChat.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupListeners() {
        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateBitCounter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun observeViewModel() {
        viewModel.allMessages.observe(this) { messages ->
            chatAdapter.submitList(messages)
            if (messages.isNotEmpty()) {
                rvChat.smoothScrollToPosition(0)
            }
        }
    }

    private fun updateBitCounter(text: String) {
        val encoded = SqueezeCodec.encode(text)
        val bitCount = encoded.size * 8
        tvBitCounter.text = getString(R.string.bit_counter, bitCount)
    }

    private fun sendMessage() {
        val text = etMessage.text.toString()
        if (text.isBlank()) return

        val message = MessageEntity(
            msgId = System.currentTimeMillis(),
            senderId = "ME",
            timestamp = System.currentTimeMillis(),
            content = text,
            originalLength = text.length,
            statusMask = 0,
            isComplete = true,
            expiration = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
        )

        viewModel.sendMessage(message)
        etMessage.text.clear()
    }
}
