package com.example.androidlearn.ui.teacher;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidlearn.R;
import com.example.androidlearn.data.DatabaseHelper;
import com.example.androidlearn.model.Question;
import java.util.List;

public class TeacherQuizManagerActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int chapterId;
    private RecyclerView rcv;
    private TextView btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_quiz_manager);

        db = new DatabaseHelper(this);
        chapterId = getIntent().getIntExtra("CHAPTER_ID", -1);
        if (chapterId == -1) {
            Toast.makeText(this, "Không tìm thấy chương học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rcv = findViewById(R.id.rcvQuestions);
        btnAdd = findViewById(R.id.btnAddQuestion);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rcv.setLayoutManager(new LinearLayoutManager(this));

        loadQuestions();

        // Bấm nút Thêm -> Truyền vào null (tức là không sửa ai cả)
        btnAdd.setOnClickListener(v -> showQuestionDialog(null));
    }

    private void loadQuestions() {
        List<Question> list = db.getQuestionsByChapter(chapterId);
        rcv.setAdapter(new QuestionAdapter(list));
    }

    // --- HÀM HIỆN DIALOG (DÙNG CHUNG CHO THÊM VÀ SỬA) ---
    private void showQuestionDialog(Question questionToEdit) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_question);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle); // Nếu layout bạn chưa có ID này thì thêm vào hoặc bỏ qua dòng này
        EditText edtContent = dialog.findViewById(R.id.edtQuestionContent);
        EditText edtA = dialog.findViewById(R.id.edtOpA);
        EditText edtB = dialog.findViewById(R.id.edtOpB);
        EditText edtC = dialog.findViewById(R.id.edtOpC);
        EditText edtD = dialog.findViewById(R.id.edtOpD);
        RadioGroup rg = dialog.findViewById(R.id.rgCorrectAnswer);
        Button btnSave = dialog.findViewById(R.id.btnSaveQuestion);

        // --- KIỂM TRA: NẾU LÀ SỬA (questionToEdit != null) ---
        if (questionToEdit != null) {
            // Điền dữ liệu cũ vào
            if(tvTitle != null) tvTitle.setText("Cập nhật câu hỏi");
            btnSave.setText("Cập nhật");

            edtContent.setText(questionToEdit.getNoiDung());
            edtA.setText(questionToEdit.getAnswerA()); // Dùng getter cũ (getAnswerA) hoặc mới (getDapAnA) tùy Model
            edtB.setText(questionToEdit.getAnswerB());
            edtC.setText(questionToEdit.getAnswerC());
            edtD.setText(questionToEdit.getAnswerD());

            // Chọn lại đáp án đúng cũ
            String correct = questionToEdit.getCorrectAnswer();
            if (correct.equals("A")) rg.check(R.id.rbA);
            else if (correct.equals("B")) rg.check(R.id.rbB);
            else if (correct.equals("C")) rg.check(R.id.rbC);
            else if (correct.equals("D")) rg.check(R.id.rbD);
        }

        btnSave.setOnClickListener(v -> {
            String content = edtContent.getText().toString().trim();
            String a = edtA.getText().toString().trim();
            String b = edtB.getText().toString().trim();
            String c = edtC.getText().toString().trim();
            String d = edtD.getText().toString().trim();

            String correctAns = "";
            int selectedId = rg.getCheckedRadioButtonId();
            if (selectedId == R.id.rbA) correctAns = "A";
            else if (selectedId == R.id.rbB) correctAns = "B";
            else if (selectedId == R.id.rbC) correctAns = "C";
            else if (selectedId == R.id.rbD) correctAns = "D";

                if (content.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty() || correctAns.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ nội dung, 4 đáp án và chọn đáp án đúng!", Toast.LENGTH_SHORT).show();
            } else {
                final String finalCorrectAns = correctAns;
                btnSave.setEnabled(false);
                AsyncTask.execute(() -> {
                    if (questionToEdit == null) {
                        // --- THÊM MỚI ---
                        db.addQuestion(chapterId, content, a, b, c, d, finalCorrectAns);
                    } else {
                        // --- CẬP NHẬT (SỬA) ---
                        db.updateQuestion(questionToEdit.getMaCauHoi(), content, a, b, c, d, finalCorrectAns);
                    }

                    runOnUiThread(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            loadQuestions(); // Load lại danh sách
                            dialog.dismiss();
                            Toast.makeText(this, questionToEdit == null ? "Đã thêm!" : "Đã cập nhật!", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });

        dialog.show();
    }

    class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {
        List<Question> list;
        public QuestionAdapter(List<Question> list) { this.list = list; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question_row, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Question q = list.get(position);
            holder.tvContent.setText("Câu " + (position + 1) + ": " + q.getNoiDung());

            // Xử lý Xóa
            holder.btnDelete.setOnClickListener(v -> {
                db.deleteQuestion(q.getMaCauHoi());
                loadQuestions();
            });

            // Xử lý Sửa (Bấm vào dòng câu hỏi -> Hiện dialog sửa)
            holder.itemView.setOnClickListener(v -> {
                showQuestionDialog(q); // Truyền câu hỏi cần sửa vào
            });
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvContent;
            ImageView btnDelete;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvContent = v.findViewById(R.id.tvQuestionContent);
                btnDelete = v.findViewById(R.id.btnDeleteQuestion);
            }
        }
    }
}