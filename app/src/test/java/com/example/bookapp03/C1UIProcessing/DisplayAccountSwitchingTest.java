package com.example.bookapp03.C1UIProcessing;

import static org.junit.Assert.assertNotNull;

import android.widget.ImageButton;
import android.widget.RadioGroup;

import androidx.test.core.app.ActivityScenario;

import com.example.bookapp03.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowView;

/**
 * DisplayAccountSwitchingの単体テスト
 * - ブラックボックス: onCreate() でビューが inflate されているか
 * - ホワイトボックス: 戻るボタンに OnClickListener がセットされているか
 */
@RunWith(RobolectricTestRunner.class)
public class DisplayAccountSwitchingTest {
    @Test
    public void testOnCreate_viewsAndListeners() {
        try (ActivityScenario<DisplayAccountSwitching> scenario =
                     ActivityScenario.launch(DisplayAccountSwitching.class)) {
            scenario.onActivity(activity -> {
                ImageButton btnBack = activity.findViewById(R.id.btnBack);
                RadioGroup radioGroup = activity.findViewById(R.id.radioAccountGroup);
                // ビューが正しく取得できる（ブラックボックス）
                assertNotNull(btnBack);
                assertNotNull(radioGroup);
                // org.robolectric.Shadows を使う
                ShadowView shadow = Shadows.shadowOf(btnBack);
                assertNotNull(shadow.getOnClickListener());
            });
        }
    }
}