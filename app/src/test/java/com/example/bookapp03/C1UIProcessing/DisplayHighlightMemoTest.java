package com.example.bookapp03.C1UIProcessing;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;

import com.example.bookapp03.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowDrawerLayout;
import org.robolectric.shadows.Shadows;

/**
 * DisplayHighlightMemoの単体テスト
 * - ブラックボックス: onCreate で RecyclerView が取得できること
 * - ホワイトボックス: ドロワーが開かれていること
 */
@RunWith(RobolectricTestRunner.class)
public class DisplayHighlightMemoTest {

    @Test
    public void testOnCreate_drawerAndRecycler() {
        try (ActivityScenario<DisplayHighlightMemo> scenario =
                 ActivityScenario.launch(DisplayHighlightMemo.class)) {
            scenario.onActivity(activity -> {
                DrawerLayout drawer = activity.findViewById(R.id.drawer_layout);
                RecyclerView recycler = activity.findViewById(R.id.recyclerHighlight);

                // ブラックボックス: ビューが取得できる
                assertNotNull(drawer);
                assertNotNull(recycler);

                // ホワイトボックス: ドロワーが開かれている
                ShadowDrawerLayout shadow = Shadows.shadowOf(drawer);
                assertTrue(shadow.isDrawerOpen(GravityCompat.END));
            });
        }
    }
}