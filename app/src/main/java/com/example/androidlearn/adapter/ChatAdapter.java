package com.example.androidlearn.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidlearn.R;
import com.example.androidlearn.model.Message;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Message> messageList;

    public ChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message msg = messageList.get(position);

        if (msg.isMe()) {
            // Nếu là tôi: Hiện phải, Ẩn trái
            holder.layoutRight.setVisibility(View.VISIBLE);
            holder.layoutLeft.setVisibility(View.GONE);

            holder.tvContentRight.setText(msg.getContent());
            holder.tvTimeRight.setText(msg.getTime());
        } else {
            // Nếu là người khác: Hiện trái, Ẩn phải
            holder.layoutRight.setVisibility(View.GONE);
            holder.layoutLeft.setVisibility(View.VISIBLE);

            holder.tvSenderName.setText(msg.getSenderName());
            holder.tvContentLeft.setText(msg.getContent());
            holder.tvTimeLeft.setText(msg.getTime());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutLeft, layoutRight;
        TextView tvSenderName, tvContentLeft, tvTimeLeft, tvContentRight, tvTimeRight;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutLeft = itemView.findViewById(R.id.layoutLeft);
            layoutRight = itemView.findViewById(R.id.layoutRight);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvContentLeft = itemView.findViewById(R.id.tvContentLeft);
            tvTimeLeft = itemView.findViewById(R.id.tvTimeLeft);
            tvContentRight = itemView.findViewById(R.id.tvContentRight);
            tvTimeRight = itemView.findViewById(R.id.tvTimeRight);
        }
    }
}