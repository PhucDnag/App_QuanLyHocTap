package com.example.androidlearn.ui.chat;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidlearn.R;
import com.example.androidlearn.data.DatabaseHelper;
import com.example.androidlearn.model.Message;
import java.util.ArrayList;
import java.util.List;
import com.example.androidlearn.adapter.ChatAdapter;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rcvChat;
    private EditText edtMessage;
    private ImageButton btnSend;
    private TextView tvChatTitle;

    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private DatabaseHelper dbHelper;

    private int myUserId = 0;
    private String currentChatName = "";
    private String currentRoomId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        dbHelper = new DatabaseHelper(this);

        rcvChat = findViewById(R.id.rcvChat);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        tvChatTitle = findViewById(R.id.tvChatTitle);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        myUserId = prefs.getInt("KEY_USER_ID", -1);

        // --- [SỬA ĐỔI QUAN TRỌNG] ---
        // 1. Ưu tiên lấy ROOM_ID trực tiếp từ Intent (Chính xác 100%)
        if (getIntent().hasExtra("ROOM_ID")) {
            currentRoomId = getIntent().getStringExtra("ROOM_ID");
        }

        // 2. Lấy tên hiển thị
        if (getIntent().hasExtra("CHAT_NAME") || getIntent().hasExtra("ROOM_NAME")) {
            currentChatName = getIntent().hasExtra("ROOM_NAME")
                    ? getIntent().getStringExtra("ROOM_NAME")
                    : getIntent().getStringExtra("CHAT_NAME");
            tvChatTitle.setText(currentChatName);
        }

        // 3. Nếu không có ROOM_ID (trường hợp cũ), mới tự tính toán
        if (currentRoomId.isEmpty()) {
            createSmartRoomId();
        }

        // -----------------------------

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rcvChat.setLayoutManager(layoutManager);
        rcvChat.setAdapter(chatAdapter);

        loadMessages();

        // Đánh dấu đã đọc
        if (!currentRoomId.isEmpty()) {
            dbHelper.markMessagesAsRead(currentRoomId);
        }

        btnSend.setOnClickListener(v -> {
            String content = edtMessage.getText().toString().trim();
            if (!content.isEmpty()) {
                dbHelper.sendGroupMessage(myUserId, content, currentRoomId);
                edtMessage.setText("");
                loadMessages();
                rcvChat.smoothScrollToPosition(messageList.size() - 1);
            }
        });
    }

    private void createSmartRoomId() {
        if (currentChatName.startsWith("Lớp ")) {
            currentRoomId = currentChatName;
        } else {
            int partnerId = dbHelper.getUserIdByName(currentChatName);
            if (partnerId != -1) {
                currentRoomId = (myUserId < partnerId) ? (myUserId + "_" + partnerId) : (partnerId + "_" + myUserId);
            } else {
                currentRoomId = currentChatName;
            }
        }
    }

    private void loadMessages() {
        messageList.clear();
        Cursor cursor = dbHelper.getMessagesByRoom(currentRoomId);
        if (cursor.moveToFirst()) {
            do {
                int senderId = cursor.getInt(cursor.getColumnIndexOrThrow("NguoiGui"));
                String content = cursor.getString(cursor.getColumnIndexOrThrow("NoiDung"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("ThoiGian"));

                String name = "Ẩn danh";
                int nameIndex = cursor.getColumnIndex("HoTen");
                if (nameIndex != -1 && cursor.getString(nameIndex) != null) name = cursor.getString(nameIndex);

                messageList.add(new Message(content, name, time, (senderId == myUserId)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (chatAdapter != null) chatAdapter.notifyDataSetChanged();
    }
}