package com.example.bookapp03.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookapp03.R;
import com.example.bookapp03.logic.TutorialManager;

public class TutorialActivity extends AppCompatActivity {
    private int totalPages = 3;
    private int currentIndex = 0;

    private TextView dummyText;
    private Button nextButton;
    private Button skipButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_dummy);

        dummyText = findViewById(R.id.dummy_text);
        nextButton = findViewById(R.id.next_button);
        skipButton = findViewById(R.id.skip_button);

        showPage();

        nextButton.setOnClickListener(v -> {
            currentIndex++;
            if (currentIndex >= totalPages) {
                finishTutorial();
            } else {
                showPage();
            }
        });

        skipButton.setOnClickListener(v -> finishTutorial());
    }

    private void showPage() {
        dummyText.setText("チュートリアル " + (currentIndex + 1) + " / " + totalPages);
        nextButton.setText(currentIndex == totalPages - 1 ? "閉じる" : "次へ");
    }

    private void finishTutorial() {
        new TutorialManager(this).markTutorialAsShown();
        finish();
    }
}