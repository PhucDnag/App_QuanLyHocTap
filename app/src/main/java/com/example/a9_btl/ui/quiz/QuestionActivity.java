package com.example.a9_btl.ui.quiz;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a9_btl.R;
import com.example.a9_btl.data.DatabaseHelper;
import com.example.a9_btl.model.Question;

import java.util.List;
import java.util.Locale;

public class QuestionActivity extends AppCompatActivity {

    // Khai báo biến
    private TextView tvTimer, tvQuestionContent;
    private RadioGroup radioGroup;
    private RadioButton radioA, radioB, radioC, radioD;
    private Button btnNext, btnPrev;
    private View btnSubmit, btnBack;

    private DatabaseHelper dbHelper;
    private List<Question> questionList;
    private int currentQuestionIndex = 0; // Đang làm câu số mấy
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        // 1. Ánh xạ View
        tvTimer = findViewById(R.id.tvTimer);
        tvQuestionContent = findViewById(R.id.tvQuestionContent);
        radioGroup = findViewById(R.id.radioGroup);
        radioA = findViewById(R.id.radioA);
        radioB = findViewById(R.id.radioB);
        radioC = findViewById(R.id.radioC);
        radioD = findViewById(R.id.radioD);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);

        // 2. Lấy dữ liệu câu hỏi từ DB
        dbHelper = new DatabaseHelper(this);
        // Giả sử lấy câu hỏi chương 1 (Sau này bạn lấy ID từ Intent gửi sang)
        int chapterId = getIntent().getIntExtra("CHAPTER_ID", 1);
        questionList = dbHelper.getQuestionsByChapter(chapterId);

        if (questionList.isEmpty()) {
            Toast.makeText(this, "Chương này chưa có câu hỏi!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3. Hiển thị câu hỏi đầu tiên
        showQuestion(0);

        // 4. Bắt đầu đếm ngược 15 phút (15 * 60 * 1000 ms)
        startTimer(15 * 60 * 1000);

        // 5. Xử lý sự kiện bấm nút

        // Nút Next
        btnNext.setOnClickListener(v -> {
            saveUserAnswer(); // Lưu đáp án câu hiện tại trước khi chuyển
            if (currentQuestionIndex < questionList.size() - 1) {
                currentQuestionIndex++;
                showQuestion(currentQuestionIndex);
            }
        });

        // Nút Prev
        btnPrev.setOnClickListener(v -> {
            saveUserAnswer();
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                showQuestion(currentQuestionIndex);
            }
        });

        // Nút Submit (Nộp bài)
        btnSubmit.setOnClickListener(v -> {
            saveUserAnswer();
            confirmSubmit();
        });

        // Nút Back (Góc trái trên)
        btnBack.setOnClickListener(v -> finish());
    }

    // Hàm hiển thị câu hỏi lên màn hình
    private void showQuestion(int index) {
        Question q = questionList.get(index);

        // Cập nhật nội dung
        tvQuestionContent.setText("Câu " + (index + 1) + ": " + q.getContent());
        radioA.setText("A. " + q.getAnswerA());
        radioB.setText("B. " + q.getAnswerB());
        radioC.setText("C. " + q.getAnswerC());
        radioD.setText("D. " + q.getAnswerD());

        // Kiểm tra xem câu này người dùng đã chọn đáp án chưa để tích lại
        radioGroup.clearCheck();
        if (q.getUserAnswer().equals("A")) radioA.setChecked(true);
        if (q.getUserAnswer().equals("B")) radioB.setChecked(true);
        if (q.getUserAnswer().equals("C")) radioC.setChecked(true);
        if (q.getUserAnswer().equals("D")) radioD.setChecked(true);

        // Ẩn/Hiện nút Next/Prev
        btnPrev.setEnabled(index > 0);
        btnNext.setEnabled(index < questionList.size() - 1);

        // Nếu là câu cuối, nút Next đổi thành "Kết thúc" (tuỳ chọn)
        if (index == questionList.size() - 1) {
            btnNext.setText("Hết");
        } else {
            btnNext.setText("Câu tiếp");
        }
    }

    // Hàm lưu đáp án người dùng chọn vào biến tạm
    private void saveUserAnswer() {
        Question q = questionList.get(currentQuestionIndex);
        int selectedId = radioGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.radioA) q.setUserAnswer("A");
        else if (selectedId == R.id.radioB) q.setUserAnswer("B");
        else if (selectedId == R.id.radioC) q.setUserAnswer("C");
        else if (selectedId == R.id.radioD) q.setUserAnswer("D");
        // Nếu không chọn thì giữ nguyên
    }

    // Hàm đếm ngược thời gian
    private void startTimer(long durationInMillis) {
        timer = new CountDownTimer(durationInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Cập nhật TextView 15:00 -> 14:59...
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                tvTimer.setText(timeFormatted);
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                Toast.makeText(QuestionActivity.this, "Hết giờ!", Toast.LENGTH_LONG).show();
                calculateScore(); // Tự động nộp bài khi hết giờ
            }
        }.start();
    }

    // Hộp thoại xác nhận nộp bài
    private void confirmSubmit() {
        new AlertDialog.Builder(this)
                .setTitle("Nộp bài")
                .setMessage("Bạn có chắc muốn nộp bài và xem điểm không?")
                .setPositiveButton("Nộp luôn", (dialog, which) -> calculateScore())
                .setNegativeButton("Làm tiếp", null)
                .show();
    }

    // Tính điểm
    private void calculateScore() {
        if (timer != null) timer.cancel();

        int score = 0; // Số câu đúng
        int total = questionList.size();

        for (Question q : questionList) {
            if (q.getUserAnswer().equals(q.getCorrectAnswer())) {
                score++;
            }
        }

        // --- MỚI: LƯU ĐIỂM VÀO DATABASE ---
        // Giả sử User ID là 1 (sau này bạn lấy từ Login)
        // Giả sử currentChapterId được lấy từ Intent ở onCreate
        int chapterId = getIntent().getIntExtra("CHAPTER_ID", 1);
        dbHelper.saveQuizScore(1, chapterId, score);
        // ----------------------------------

        // Hiển thị thông báo kết quả
        new AlertDialog.Builder(this)
                .setTitle("Kết quả")
                .setMessage("Bạn làm đúng: " + score + "/" + total + " câu.")
                .setPositiveButton("Hoàn thành", (dialog, which) -> finish()) // Bấm xong thì thoát ra
                .setCancelable(false)
                .show();
    }
}