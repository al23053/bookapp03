package com.example.bookapp03.ui;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView; // Added for testing ImageView with Glide

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookapp03.C1UIProcessing.BookDetailActivity;
import com.example.bookapp03.C1UIProcessing.ViewModelFactory;
import com.example.bookapp03.R;
import com.example.bookapp03.C3BookInformationProcessing.BookDetailData;
import com.example.bookapp03.C1UIProcessing.HighlightMemoData;
import com.example.bookapp03.C1UIProcessing.BookDetailViewController;
import com.example.bookapp03.C3BookInformationProcessing.BookDetailViewModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowImageView; // For Glide check

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class BookDetailActivityTest {

    private ActivityController<BookDetailActivity> activityController;
    private BookDetailActivity activity;

    @Mock
    private BookDetailViewModel mockViewModel;
    @Mock
    private BookDetailViewController mockController;
    @Mock
    private ViewModelProvider mockViewModelProvider;
    @Mock
    private ViewModelFactory mockViewModelFactory; // ViewModelFactoryもモック
    @Mock
    private View mockRootView; // findViewByIdの対象となるView

    // LiveDataを制御するためのMutableLiveDataインスタンス
    private MutableLiveData<BookDetailData> bookDetailLiveData;
    private MutableLiveData<List<HighlightMemoData>> highlightMemosLiveData;

    private final String TEST_UID = "user123";
    private final String TEST_VOLUME_ID = "testVol001";
    private BookDetailData testBookDetailData;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // LiveDataの初期化
        bookDetailLiveData = new MutableLiveData<>();
        highlightMemosLiveData = new MutableLiveData<>();

        // mockViewModelの振る舞いを定義
        when(mockViewModel.getBookDetail()).thenReturn(bookDetailLiveData);
        when(mockViewModel.getHighlightMemos()).thenReturn(highlightMemosLiveData);

        // ViewModelProviderのモックを設定
        // ViewModelFactory.create() が呼ばれた際に mockViewModel を返すように設定
        when(mockViewModelProvider.get(BookDetailViewModel.class)).thenReturn(mockViewModel);
        // ViewModelFactoryのcreateメソッドのモック
        when(mockViewModelFactory.create(eq(BookDetailViewModel.class))).thenReturn(mockViewModel);


        // ActivityControllerを初期化する前に、ViewModelProviderとViewModelFactoryを差し込む
        // RobolectricでActivityのViewModelProviderをモックするためには、
        // Activityのコンストラクタが呼ばれる前に静的/プロキシで差し込む必要がある
        // もしくは、ActivityのテストコンストラクタやsetContentView後にリフレクションでViewModelProviderを差し込む
        // ActivityをspyにしてViewModelProviderをmockする方が一般的
        // 今回はシンプルにActivityControllerでbuildActivityする際にIntentを渡し、
        // ViewModelProviderをRobolectricのテスト内で差し込む形にする。
        // ActivityがViewModelFactoryを自分でインスタンス化しているので、
        // ViewModelFactoryをモックし、そのFactoryから返されるViewModelもモックする。

        // R.id.content のモックを設定
        // findRootViewById は android.R.id.content を使うので、これはactivity.findViewById(android.R.id.content)をモックする
        // RobolectricがR.layout.activity_book_detailを inflate するため、
        // findViewById の呼び出しはRobolectricが処理してくれる。
        // したがって、mockRootViewは実際にinflateされたViewである必要があり、
        // activity.findViewById(android.R.id.content) で取得できる。
        // テスト内でActivityを起動してからそのViewを取得し、それに対してfindViewByIdを呼ぶ形にする。

        // テスト用データ
        testBookDetailData = new BookDetailData(TEST_VOLUME_ID, "Test Book Title", "Test Summary", "http://example.com/cover.jpg", "public");

        // System.setProperty("robolectric.alwaysCallParentMethods", "false"); // 必要に応じて
    }

    /**
     * Intentに有効なvolumeIdがある場合のActivity起動テスト
     * ViewModelのloadBookDetailが呼ばれ、UIが正しく更新されることを検証
     */
    @Test
    public void onCreate_withValidVolumeId_loadsBookDetailAndDisplaysUI() {
        // Given
        Intent intent = new Intent();
        intent.putExtra("volumeId", TEST_VOLUME_ID);
        // ActivityがgetApplicationContext()を呼ぶため、モックコンテキストを設定する必要がある
        // Robolectricは自動的にアプリケーションコンテキストを提供するため、明示的なモックは不要

        // Activityを起動し、Lifecycle.State.CREATED まで進める
        activityController = Robolectric.buildActivity(BookDetailActivity.class, intent);
        activity = activityController.setup().get(); // onCreate, onStart, onResumeを呼ぶ

        // ViewModelFactory を Activity の ViewModelProvider に差し込む
        // Robolectric の Activity は、デフォルトで AndroidViewModelFactory を使用する
        // カスタムファクトリーを使用するために、ViewModelProvider の設定をオーバーライド
        // ActivityのViewModelProviderを取得し、内部のFactoryをモックFactoryに置き換える
        // これは少しトリッキーな部分
        // Option 1: Activityをspyにして、getViewModelProvider()をモック
        // Option 2: ViewModelFactoryをpublicなstaticフィールドにするなどして差し込む
        // Option 3: RobolectricのTestApplicaionを継承して、getApplication()がカスタムファクトリを返すようにする
        // 最もシンプルなのは、ActivityのViewModelProviderが提供するViewModelを直接モックすること
        // `when(activity.getViewModelProvider().get(BookDetailViewModel.class)).thenReturn(mockViewModel);`
        // しかし、onCreate()内で既にViewModelが取得されているため、それよりも前に設定する必要がある。
        // そこで、ViewModelFactoryをモックし、ActivityがViewModelFactoryを通じてViewModelを取得する流れをシミュレートする。
        // ActivityのViewModelProvider取得部分をRobolectricのShadowで差し込むのが理想的だが、
        // ここではFactoryをモックし、そのFactoryからMockViewModelが返されることを前提とする

        // activity.findViewById(android.R.id.content) はRobolectricが実際にViewを返すので、
        // mockRootView を使う代わりに、inflateされたViewを直接使う
        View rootView = activity.findViewById(android.R.id.content);
        assertNotNull(rootView);

        // activity内のcontrollerフィールドにモックを設定
        try {
            java.lang.reflect.Field controllerField = activity.getClass().getDeclaredField("controller");
            controllerField.setAccessible(true);
            controllerField.set(activity, mockController);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set mockController via reflection", e);
        }

        // ViewModelFactory を ViewModelProvider に渡し、ViewModelProvider が mockViewModel を返すようにする
        // 通常はViewModelFactoryのテストとは別にActivityのテストをするため、
        // ViewModelProviderからmockViewModelが返るように設定する
        // RobolectricでActivityのViewModelProviderをモックする正しい方法は、
        // ActivityTestRuleやActivityScenarioを使うか、TestApplicaionをCustomApplicationにするか
        // もしくは、ViewModelFactoryのインスタンスを生成する部分を差し替える。
        // ここでは、ViewModelProvider.FactoryをモックしてViewModelProviderのgetメソッドがmockViewModelを返すようにする。
        ViewModelProvider testViewModelProvider = new ViewModelProvider(activity, mockViewModelFactory);
        try {
            java.lang.reflect.Field viewModelProviderField = activity.getClass().getDeclaredField("viewModel");
            viewModelProviderField.setAccessible(true);
            viewModelProviderField.set(activity, mockViewModel); // 直接mockViewModelを注入
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set mockViewModel via reflection", e);
        }

        // ViewModelのloadBookDetailが呼ばれたことを確認
        // activityController.setup() で onCreate が呼ばれるので、その中でloadBookDetailが呼ばれる
        verify(mockViewModel, times(1)).loadBookDetail(eq(TEST_UID), eq(TEST_VOLUME_ID));
        verify(mockViewModel, never()).loadHighlightMemos(anyString(), anyString()); // まだ呼ばれない

        // LiveDataの変更をシミュレート (データがロードされた場合)
        bookDetailLiveData.postValue(testBookDetailData);
        ShadowLooper.idleMainLooper(); // LiveDataの変更がオブザーバーにディスパッチされるのを待つ

        // controller.displayBookDetailsが正しい引数で呼ばれたことを確認
        verify(mockController, times(1)).displayBookDetails(eq(testBookDetailData), eq(rootView), eq(false));

        // HighlightMemosがロードされたことを確認
        verify(mockViewModel, times(1)).loadHighlightMemos(eq(TEST_UID), eq(TEST_VOLUME_ID));

        // UI要素のテキストが正しく設定されていることを確認（ViewController経由なので間接的に検証）
        TextView titleView = rootView.findViewById(R.id.book_title);
        TextView summaryView = rootView.findViewById(R.id.book_summary);
        TextView statusView = rootView.findViewById(R.id.book_status);
        ImageView coverView = rootView.findViewById(R.id.book_cover);
        TextView noBookMessage = rootView.findViewById(R.id.no_book_message);

        // controllerが正しく動作していればこれらのTextViewは設定されているはず
        // Controllerはモックされているため、ここではverify(controller).displayBookDetailsの引数検証で十分
        // もしControllerの内部実装までテストするなら、Activityテストとは別にControllerの単体テストで行うべき

        assertNotNull(titleView);
        assertNotNull(summaryView);
        assertNotNull(statusView);
        assertNotNull(coverView);
        assertNotNull(noBookMessage);

        assertEquals(View.GONE, noBookMessage.getVisibility()); // データがあるため非表示
        // GlideはShadowImageViewで確認可能
        ShadowImageView shadowCoverView = org.robolectric.Shadows.shadowOf(coverView);
        assertNotNull(shadowCoverView.getImageURI());
    }


    /**
     * IntentにvolumeIdがない場合のActivity起動テスト
     * ViewModelのloadBookDetailが呼ばれず、"該当書籍なし"メッセージが表示されることを検証
     */
    @Test
    public void onCreate_withoutVolumeId_displaysNoBookMessage() {
        // Given
        Intent intent = new Intent(); // volumeIdなし
        activityController = Robolectric.buildActivity(BookDetailActivity.class, intent);
        activity = activityController.setup().get();

        View rootView = activity.findViewById(android.R.id.content);
        assertNotNull(rootView);

        try {
            java.lang.reflect.Field controllerField = activity.getClass().getDeclaredField("controller");
            controllerField.setAccessible(true);
            controllerField.set(activity, mockController);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set mockController via reflection", e);
        }

        try {
            java.lang.reflect.Field viewModelProviderField = activity.getClass().getDeclaredField("viewModel");
            viewModelProviderField.setAccessible(true);
            viewModelProviderField.set(activity, mockViewModel); // 直接mockViewModelを注入
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set mockViewModel via reflection", e);
        }

        // Then
        // volumeIdがないため、loadBookDetailは呼ばれないことを確認
        verify(mockViewModel, never()).loadBookDetail(anyString(), anyString());
        verify(mockViewModel, never()).loadHighlightMemos(anyString(), anyString());

        // controller.displayBookDetailsがnullとtrueで呼ばれることを確認
        verify(mockController, times(1)).displayBookDetails(eq(null), eq(rootView), eq(true));

        // "該当書籍なし"メッセージが可視であることを確認 (controllerが正しく設定していれば)
        TextView noBookMessage = rootView.findViewById(R.id.no_book_message);
        assertNotNull(noBookMessage);
        assertEquals(View.VISIBLE, noBookMessage.getVisibility());

        // 他のUI要素は非表示または空であるべきだが、controllerがモックされているためここでは間接的に確認
        TextView titleView = rootView.findViewById(R.id.book_title);
        TextView summaryView = rootView.findViewById(R.id.book_summary);
        TextView statusView = rootView.findViewById(R.id.book_status);
        ImageView coverView = rootView.findViewById(R.id.book_cover);

        assertEquals(View.GONE, titleView.getVisibility()); // 通常は初期状態GONE
        assertEquals(View.GONE, summaryView.getVisibility());
        assertEquals(View.GONE, statusView.getVisibility());
        assertEquals(View.GONE, coverView.getVisibility());
    }

    /**
     * ViewModelからnullのデータが返された場合の表示テスト
     * "該当書籍なし"メッセージが表示されることを検証
     */
    @Test
    public void onBookDetailLiveData_nullValue_displaysNoBookMessage() {
        // Given
        Intent intent = new Intent();
        intent.putExtra("volumeId", TEST_VOLUME_ID);
        activityController = Robolectric.buildActivity(BookDetailActivity.class, intent);
        activity = activityController.setup().get();

        View rootView = activity.findViewById(android.R.id.content);
        assertNotNull(rootView);

        try {
            java.lang.reflect.Field controllerField = activity.getClass().getDeclaredField("controller");
            controllerField.setAccessible(true);
            controllerField.set(activity, mockController);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set mockController via reflection", e);
        }

        try {
            java.lang.reflect.Field viewModelProviderField = activity.getClass().getDeclaredField("viewModel");
            viewModelProviderField.setAccessible(true);
            viewModelProviderField.set(activity, mockViewModel); // 直接mockViewModelを注入
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set mockViewModel via reflection", e);
        }

        // When ViewModel returns null
        bookDetailLiveData.postValue(null);
        ShadowLooper.idleMainLooper(); // LiveDataの変更がオブザーバーにディスパッチされるのを待つ

        // Then
        // loadBookDetailは一度呼ばれている
        verify(mockViewModel, times(1)).loadBookDetail(eq(TEST_UID), eq(TEST_VOLUME_ID));
        // controller.displayBookDetailsがnullとtrueで呼ばれることを確認
        verify(mockController, times(1)).displayBookDetails(eq(null), eq(rootView), eq(true));

        TextView noBookMessage = rootView.findViewById(R.id.no_book_message);
        assertNotNull(noBookMessage);
        assertEquals(View.VISIBLE, noBookMessage.getVisibility());

        // 他のUI要素は非表示または空であるべき
        TextView titleView = rootView.findViewById(R.id.book_title);
        assertEquals(View.GONE, titleView.getVisibility());
    }
}