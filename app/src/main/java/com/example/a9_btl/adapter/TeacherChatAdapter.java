package com.example.a9_btl.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.ui.chat.ChatActivity;
import java.util.List;

public class TeacherChatAdapter extends RecyclerView.Adapter<TeacherChatAdapter.ViewHolder> {

    private Context context;
    private List<String> roomList; // Danh sách mã phòng (Room ID)
    private DatabaseHelper db;
    private int teacherId;

    public TeacherChatAdapter(Context context, List<String> roomList, DatabaseHelper db, int teacherId) {
        this.context = context;
        this.roomList = roomList;
        this.db = db;
        this.teacherId = teacherId;
    }

    public void updateList(List<String> newList) {
        this.roomList = newList;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_teacher_chat_room, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String roomId = roomList.get(position);

        // 1. Lấy tên hiển thị (Tên lớp hoặc Tên SV)
        String displayName = db.getRoomDisplayName(roomId, teacherId);
        holder.tvName.setText(displayName);

        // 2. Avatar (Lấy chữ cái đầu)
        String firstChar = displayName.isEmpty() ? "?" : String.valueOf(displayName.charAt(0));
        holder.tvAvatar.setText(firstChar);

        // 3. Tin nhắn cuối
        String lastMsg = db.getLastMessage(roomId, teacherId);
        holder.tvMsg.setText(lastMsg);

        // 4. Kiểm tra tin nhắn mới (Unread)
        boolean hasUnread = db.hasUnreadMessage(roomId, teacherId);
        if (hasUnread) {
            holder.tvMsg.setTypeface(null, Typeface.BOLD);
            holder.tvMsg.setTextColor(Color.BLACK);
            holder.imgDot.setVisibility(View.VISIBLE);
        } else {
            holder.tvMsg.setTypeface(null, Typeface.NORMAL);
            holder.tvMsg.setTextColor(Color.GRAY);
            holder.imgDot.setVisibility(View.GONE);
        }

        // Click mở Chat
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("ROOM_ID", roomId);
            intent.putExtra("ROOM_NAME", displayName); // Truyền tên hiển thị sang
            context.startActivity(intent);
        });
    }

    @Override public int getItemCount() { return roomList.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMsg, tvAvatar;
        ImageView imgDot;
        public ViewHolder(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvRoomName);
            tvMsg = v.findViewById(R.id.tvLastMessage);
            tvAvatar = v.findViewById(R.id.tvAvatarChar);
            imgDot = v.findViewById(R.id.imgUnreadDot);
        }
    }
}