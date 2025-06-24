package com.example.bookapp03.C1UIProcessing;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.widget.ImageButton;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;

import com.example.bookapp03.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowView;
import org.robolectric.shadows.Shadows;

/**
 * DisplaySetting の単体テスト
 * - ブラックボックス: onCreate でビューが正しくinflateされていること
 * - ホワイトボックス: 戻るボタンにリスナーがセットされ、クリックで finish() が呼ばれること
 */
@RunWith(RobolectricTestRunner.class)
public class DisplaySettingTest {

    @Test
    public void testOnCreate_andBackButton() {
        try (ActivityScenario<DisplaySetting> scenario =
                 ActivityScenario.launch(DisplaySetting.class)) {
            scenario.onActivity(activity -> {
                ImageButton btnBack = activity.findViewById(R.id.btnBack);
                TextView btnNick   = activity.findViewById(R.id.btnNicknameIcon);
                TextView btnGenre  = activity.findViewById(R.id.btnGenre);
                TextView btnDark   = activity.findViewById(R.id.btnDarkMode);
                TextView btnAcc    = activity.findViewById(R.id.btnAccountSwitch);
                TextView btnLogout = activity.findViewById(R.id.btnLogout);

                // ブラックボックス: ビューが取得できる
                assertNotNull(btnBack);
                assertNotNull(btnNick);
                assertNotNull(btnGenre);
                assertNotNull(btnDark);
                assertNotNull(btnAcc);
                assertNotNull(btnLogout);

                // ホワイトボックス: 戻るボタンにリスナーがセットされている
                ShadowView shadowBack = Shadows.shadowOf(btnBack);
                assertNotNull(shadowBack.getOnClickListener());

                // クリックで activity が finish される
                btnBack.performClick();
                assertTrue(activity.isFinishing());
            });
        }
    }
}