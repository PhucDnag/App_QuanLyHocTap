package com.example.a9_btl.ui.flashcard;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.a9_btl.R;
import com.example.a9_btl.model.FlashCard;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình xem Flashcard với animation lật 3D.
 * Nhận danh sách card qua Intent extras.
 */
public class FlashCardActivity extends AppCompatActivity {

    public static final String EXTRA_FRONTS       = "extra_fronts";
    public static final String EXTRA_BACKS        = "extra_backs";
    public static final String EXTRA_CHAPTER_NAME = "extra_chapter_name";

    private TextView    tvCounter, tvChapterName, tvFront, tvBack, tvTapHint;
    private CardView    cardFront, cardBack;
    private FrameLayout frameCard;
    private ProgressBar pbFlash;
    private MaterialButton btnPrev, btnNext;

    private List<FlashCard> cards = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isFlipped = false;

    // Camera distance để animation 3D không clip
    private static final float CAMERA_DISTANCE = 12000f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        bindViews();
        parseCards();
        setupClickListeners();
        showCard(0);
    }

    private void bindViews() {
        tvCounter     = findViewById(R.id.tvFlashCounter);
        tvChapterName = findViewById(R.id.tvFlashChapterName);
        tvFront       = findViewById(R.id.tvCardFront);
        tvBack        = findViewById(R.id.tvCardBack);
        tvTapHint     = findViewById(R.id.tvTapHint);
        cardFront     = findViewById(R.id.cardFront);
        cardBack      = findViewById(R.id.cardBack);
        frameCard     = findViewById(R.id.frameCard);
        pbFlash       = findViewById(R.id.pbFlash);
        btnPrev       = findViewById(R.id.btnFlashPrev);
        btnNext       = findViewById(R.id.btnFlashNext);

        // Set camera distance for smooth 3D flip
        float scale = getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(CAMERA_DISTANCE * scale);
        cardBack.setCameraDistance(CAMERA_DISTANCE * scale);

        View btnBack = findViewById(R.id.btnFlashBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        String chapterName = getIntent().getStringExtra(EXTRA_CHAPTER_NAME);
        if (chapterName != null) tvChapterName.setText(chapterName);
    }

    private void parseCards() {
        ArrayList<String> fronts = getIntent().getStringArrayListExtra(EXTRA_FRONTS);
        ArrayList<String> backs  = getIntent().getStringArrayListExtra(EXTRA_BACKS);
        if (fronts == null || backs == null) return;

        int count = Math.min(fronts.size(), backs.size());
        for (int i = 0; i < count; i++) {
            cards.add(new FlashCard(fronts.get(i), backs.get(i)));
        }
    }

    private void setupClickListeners() {
        // Lật thẻ khi nhấn vào card
        frameCard.setOnClickListener(v -> flipCard());

        btnNext.setOnClickListener(v -> {
            if (currentIndex < cards.size() - 1) {
                showCard(currentIndex + 1);
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentIndex > 0) {
                showCard(currentIndex - 1);
            }
        });
    }

    private void showCard(int index) {
        if (cards.isEmpty()) return;
        currentIndex = index;
        isFlipped = false;

        FlashCard card = cards.get(index);
        tvFront.setText(card.getFront());
        tvBack.setText(card.getBack());

        // Reset về mặt trước
        cardFront.setVisibility(View.VISIBLE);
        cardBack.setVisibility(View.GONE);
        cardFront.setRotationY(0f);
        cardBack.setRotationY(0f);

        // Update counter & progress
        tvCounter.setText((index + 1) + "/" + cards.size());
        pbFlash.setProgress((index + 1) * 100 / cards.size());

        // Update hint
        tvTapHint.setText("👆 Nhấn thẻ để xem đáp án");

        // Update buttons
        btnPrev.setEnabled(index > 0);
        btnPrev.setAlpha(index > 0 ? 1.0f : 0.4f);
        btnNext.setText(index == cards.size() - 1 ? "✅ Xong" : "Tiếp ▶");
        if (index == cards.size() - 1) {
            btnNext.setOnClickListener(v -> finish());
        } else {
            btnNext.setOnClickListener(v -> showCard(currentIndex + 1));
        }
    }

    private void flipCard() {
        if (isFlipped) {
            // Flip back to front
            animateFlip(cardBack, cardFront);
            tvTapHint.setText("👆 Nhấn thẻ để xem đáp án");
        } else {
            // Flip to back
            animateFlip(cardFront, cardBack);
            tvTapHint.setText("👆 Nhấn thẻ để xem câu hỏi");
        }
        isFlipped = !isFlipped;
    }

    private void animateFlip(final View hideView, final View showView) {
        hideView.animate()
                .rotationY(90f)
                .setDuration(150)
                .withEndAction(() -> {
                    hideView.setVisibility(View.GONE);
                    showView.setRotationY(-90f);
                    showView.setVisibility(View.VISIBLE);
                    showView.animate()
                            .rotationY(0f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }
}
