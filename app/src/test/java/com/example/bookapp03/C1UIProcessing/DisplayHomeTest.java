package com.example.bookapp03.C1UIProcessing;

import static org.junit.Assert.assertNotNull;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.test.core.app.ActivityScenario;

import com.example.bookapp03.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowView;
import org.robolectric.shadows.Shadows;

/**
 * DisplayHome の単体テスト
 * - ブラックボックス: onCreate ですべてのUI部品が取得できること
 * - ホワイトボックス: 各ボタンにリスナーがセットされていること
 */
@RunWith(RobolectricTestRunner.class)
public class DisplayHomeTest {

    @Test
    public void testOnCreate_andClickListeners() {
        try (ActivityScenario<DisplayHome> scenario =
                 ActivityScenario.launch(DisplayHome.class)) {
            scenario.onActivity(activity -> {
                EditText editPage = activity.findViewById(R.id.editPage);
                EditText editLine = activity.findViewById(R.id.editLine);
                EditText editMemo = activity.findViewById(R.id.editMemo);
                ImageButton btnReg = activity.findViewById(R.id.btnRegister);
                Button btnSummary = activity.findViewById(R.id.registerButton);
                ImageButton btnUser = activity.findViewById(R.id.btnUserSettings);

                // ブラックボックス: ビューが取得できる
                assertNotNull(editPage);
                assertNotNull(editLine);
                assertNotNull(editMemo);
                assertNotNull(btnReg);
                assertNotNull(btnSummary);
                assertNotNull(btnUser);

                // ホワイトボックス: リスナーがセットされている
                assertNotNull(Shadows.shadowOf(btnReg).getOnClickListener());
                assertNotNull(Shadows.shadowOf(btnSummary).getOnClickListener());
                assertNotNull(Shadows.shadowOf(btnUser).getOnClickListener());
            });
        }
    }
}