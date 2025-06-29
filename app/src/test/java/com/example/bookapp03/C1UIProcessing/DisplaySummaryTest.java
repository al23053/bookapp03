package com.example.bookapp03.C1UIProcessing;

import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.test.core.app.ActivityScenario;

import com.example.bookapp03.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * DisplaySummary の単体テスト
 * - ブラックボックス: onCreateでビュー取得
 * - ホワイトボックス: 各ビューにリスナーがセットされている
 */
@RunWith(RobolectricTestRunner.class)
public class DisplaySummaryTest {

    @Test
    public void testOnCreate_andControllerBindings() {
        try (ActivityScenario<DisplaySummary> scenario =
                     ActivityScenario.launch(DisplaySummary.class)) {
            scenario.onActivity(activity -> {
                EditText etSummary = activity.findViewById(R.id.summaryInput);
                Switch swPublic    = activity.findViewById(R.id.switchPublic);
                ImageButton btnBack  = activity.findViewById(R.id.btnBack);
                ImageButton btnMenu  = activity.findViewById(R.id.btnMenu);
                ImageButton btnReg   = activity.findViewById(R.id.btnRegisterSummary);

                // ブラックボックス: ビューが inflate されている
                assertNotNull(etSummary);
                assertNotNull(swPublic);
                assertNotNull(btnBack);
                assertNotNull(btnMenu);
                assertNotNull(btnReg);

                // ホワイトボックス: 戻るボタンには OnClickListener がセット済み
                assertNotNull(shadowOf(btnBack).getOnClickListener());
            });
        }
    }
}