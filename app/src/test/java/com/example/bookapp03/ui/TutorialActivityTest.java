import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.TextView;

import com.example.bookapp03.R;
import com.example.bookapp03.logic.TutorialManager;
import com.example.bookapp03.ui.TutorialActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class TutorialActivityTest {

    private TutorialActivity activity;
    private TextView dummyText;
    private Button nextButton;
    private Button skipButton;

    // TutorialManagerのインスタンスがActivity内でnewされるため、
    // 直接モックを注入するのが難しい。
    // そのため、SharedPreferencesをモックしてTutorialManagerの動作を制御する。
    private SharedPreferences sharedPreferences;

    @Before
    public void setUp() {
        // RobolectricでSharedPreferencesをシャドウする
        Context context = RuntimeEnvironment.getApplication();
        sharedPreferences = context.getSharedPreferences("tutorial_pref", Context.MODE_PRIVATE);
        // テスト前にチュートリアルが表示されていない状態にする
        sharedPreferences.edit().clear().apply();

        activity = Robolectric.buildActivity(TutorialActivity.class).create().get();

        dummyText = activity.findViewById(R.id.dummy_text);
        nextButton = activity.findViewById(R.id.next_button);
        skipButton = activity.findViewById(R.id.skip_button);
    }

    /**
     * onCreateで最初のページが正しく表示されることを確認する
     */
    @Test
    public void onCreate_displaysFirstPageCorrectly() {
        assertNotNull(dummyText);
        assertNotNull(nextButton);
        assertNotNull(skipButton);

        assertEquals("チュートリアル 1 / 3", dummyText.getText().toString());
        assertEquals("次へ", nextButton.getText().toString());
    }

    /**
     * 「次へ」ボタンが正しくページを遷移させることを確認する
     */
    @Test
    public void nextButton_advancesPages() {
        // 1ページ目から2ページ目へ
        nextButton.performClick();
        assertEquals("チュートリアル 2 / 3", dummyText.getText().toString());
        assertEquals("次へ", nextButton.getText().toString());

        // 2ページ目から3ページ目へ
        nextButton.performClick();
        assertEquals("チュートリアル 3 / 3", dummyText.getText().toString());
        assertEquals("閉じる", nextButton.getText().toString());
    }

    /**
     * 最後のページで「閉じる」ボタンを押すとActivityが終了することを確認する
     */
    @Test
    public void lastPageNextButton_finishesActivityAndMarksTutorialShown() {
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);

        // 最後のページへ移動
        nextButton.performClick(); // 1 -> 2
        nextButton.performClick(); // 2 -> 3

        // 「閉じる」ボタンをクリック
        nextButton.performClick();

        // Activityが終了したことを確認
        assertTrue(shadowActivity.isFinishing());

        // チュートリアルが表示済みになったことをSharedPreferencesで確認
        assertTrue(sharedPreferences.getBoolean("tutorial_shown", false));
    }

    /**
     * 「スキップ」ボタンを押すとActivityが終了し、チュートリアルが表示済みになることを確認する
     */
    @Test
    public void skipButton_finishesActivityAndMarksTutorialShown() {
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);

        skipButton.performClick();

        // Activityが終了したことを確認
        assertTrue(shadowActivity.isFinishing());

        // チュートリアルが表示済みになったことをSharedPreferencesで確認
        assertTrue(sharedPreferences.getBoolean("tutorial_shown", false));
    }
}