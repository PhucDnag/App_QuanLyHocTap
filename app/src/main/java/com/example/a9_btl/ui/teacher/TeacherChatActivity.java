package com.example.a9_btl.ui.teacher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.ui.chat.ChatActivity;
import com.example.a9_btl.ui.main.MainActivity;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class TeacherChatActivity extends AppCompatActivity {

    private RecyclerView rcv;
    private TeacherInternalAdapter adapter; // Dùng Adapter nội bộ
    private DatabaseHelper db;
    private int teacherId;
    private List<String> allRooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_chat); // Đảm bảo bạn đã có layout này

        db = new DatabaseHelper(this);
        rcv = findViewById(R.id.rcvTeacherChat);
        EditText edtSearch = findViewById(R.id.edtSearchChat);

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        teacherId = prefs.getInt("KEY_USER_ID", -1);

        rcv.setLayoutManager(new LinearLayoutManager(this));

        // Tìm kiếm
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filter(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        updateUnreadBadge();
    }

    private void loadData() {
        allRooms = db.getTeacherConversations(teacherId);
        // Khởi tạo Adapter nội bộ
        if (adapter == null) {
            adapter = new TeacherInternalAdapter(this, allRooms, db, teacherId);
            rcv.setAdapter(adapter);
        } else {
            adapter.updateList(allRooms);
        }
    }

    private void filter(String text) {
        if (allRooms == null) return;
        List<String> filteredList = new ArrayList<>();
        for (String roomId : allRooms) {
            String displayName = db.getRoomDisplayName(roomId, teacherId);
            if (displayName.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(roomId);
            }
        }
        if (adapter != null) adapter.updateList(filteredList);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavTeacher);
        if (bottomNav == null) return;
        bottomNav.setSelectedItemId(R.id.nav_chat);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, TeacherMainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("NAVIGATE_TO", "PROFILE");
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    private void updateUnreadBadge() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavTeacher);
        if (bottomNav == null) return;
        int unreadCount = db.getTotalUnreadCountForTeacher(teacherId);
        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_chat);
        badge.setVisible(unreadCount > 0);
        if (unreadCount > 0) badge.setNumber(unreadCount);
    }

    // =========================================================
    // ADAPTER NỘI BỘ (Để khỏi phải tạo file mới nếu bạn lười)
    // =========================================================
    public class TeacherInternalAdapter extends RecyclerView.Adapter<TeacherInternalAdapter.ViewHolder> {
        private Context context;
        private List<String> list;
        private DatabaseHelper db;
        private int myId;

        public TeacherInternalAdapter(Context context, List<String> list, DatabaseHelper db, int myId) {
            this.context = context;
            this.list = list;
            this.db = db;
            this.myId = myId;
        }

        public void updateList(List<String> newList) {
            this.list = newList;
            notifyDataSetChanged();
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Dùng lại layout item_conversation.xml của sinh viên cho tiện
            // Hoặc dùng item_teacher_chat_room.xml nếu bạn đã tạo
            View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String roomId = list.get(position);
            String displayName = db.getRoomDisplayName(roomId, myId);

            holder.tvName.setText(displayName);

            String lastMsg = db.getLastMessage(roomId, myId);
            boolean hasUnread = db.hasUnreadMessage(roomId, myId);

            if (lastMsg.isEmpty()) {
                holder.tvLastMessage.setText("Bắt đầu trò chuyện...");
                holder.tvLastMessage.setTypeface(null, Typeface.ITALIC);
            } else {
                holder.tvLastMessage.setText(lastMsg);
                if (hasUnread) {
                    holder.tvLastMessage.setTypeface(null, Typeface.BOLD);
                    holder.tvLastMessage.setTextColor(Color.BLACK);
                    holder.tvName.setTypeface(null, Typeface.BOLD);
                } else {
                    holder.tvLastMessage.setTypeface(null, Typeface.NORMAL);
                    holder.tvLastMessage.setTextColor(Color.GRAY);
                    holder.tvName.setTypeface(null, Typeface.NORMAL);
                }
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatActivity.class);
                // QUAN TRỌNG: Truyền ID phòng chuẩn xác
                intent.putExtra("ROOM_ID", roomId);
                intent.putExtra("ROOM_NAME", displayName);
                context.startActivity(intent);
            });
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvLastMessage;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                // Đảm bảo item_conversation.xml có các ID này
                tvName = itemView.findViewById(R.id.tvName);
                tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            }
        }
    }
}