package com.hayatkoprusu.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hayatkoprusu.R
import com.hayatkoprusu.data.MessageEntity
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter : ListAdapter<MessageEntity, ChatAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvContent: TextView = itemView.findViewById(R.id.tv_message_content)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_message_time)
        private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(message: MessageEntity) {
            tvContent.text = message.content ?: "[Şifreli Veri Paketi]"
            tvTime.text = sdf.format(Date(message.timestamp))
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<MessageEntity>() {
        override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean = oldItem.msgId == newItem.msgId
        override fun areContentsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean = oldItem == newItem
    }
}
