package com.example.bookapp03.C1UIProcessing;

import static org.junit.Assert.assertNotNull;

import android.widget.ImageButton;
import android.widget.Switch;

import androidx.test.core.app.ActivityScenario;

import com.example.bookapp03.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowView;
import org.robolectric.shadows.Shadows;

/**
 * DisplayDarkmodeSetting の単体テスト
 * - ブラックボックス: onCreate でビューが取得できること
 * - ホワイトボックス: スイッチにリスナーがセットされていること
 */
@RunWith(RobolectricTestRunner.class)
public class DisplayDarkmodeSettingTest {

    @Test
    public void testOnCreate_andSwitchListener() {
        try (ActivityScenario<DisplayDarkmodeSetting> scenario =
                 ActivityScenario.launch(DisplayDarkmodeSetting.class)) {
            scenario.onActivity(activity -> {
                ImageButton btnBack = activity.findViewById(R.id.btnBack);
                Switch switchDark = activity.findViewById(R.id.switchDarkmode);

                // ブラックボックス: ビューが取得できる
                assertNotNull(btnBack);
                assertNotNull(switchDark);

                // ホワイトボックス: スイッチにリスナーがセットされている
                assertNotNull(Shadows.shadowOf(switchDark).getOnCheckedChangeListener());
            });
        }
    }
}