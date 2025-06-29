package com.example.bookapp03.logic;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.bookapp03.R;
import com.example.bookapp03.data.model.BookDetailData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class BookDetailViewControllerTest {

    private BookDetailViewController controller;

    @Mock
    private View mockRootView;
    @Mock
    private TextView mockTitleView;
    @Mock
    private TextView mockSummaryView;
    @Mock
    private TextView mockStatusView;
    @Mock
    private TextView mockNoBookMessageView;
    @Mock
    private ImageView mockCoverView;

    // Glideのモック化に必要なモック
    @Mock
    private RequestManager mockRequestManager;
    @Mock
    private RequestBuilder<String> mockRequestBuilder; // Glideのload()が返すオブジェクト
    @Mock
    private RequestOptions mockRequestOptions; // Glideのapply()が返すオブジェクト

    // Log.w をモックするためのMockedStatic
    private MockedStatic<Log> mockedLog;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new BookDetailViewController();

        // findViewById の一般的な振る舞いを設定
        when(mockRootView.findViewById(R.id.book_title)).thenReturn(mockTitleView);
        when(mockRootView.findViewById(R.id.book_summary)).thenReturn(mockSummaryView);
        when(mockRootView.findViewById(R.id.book_status)).thenReturn(mockStatusView);
        when(mockRootView.findViewById(R.id.book_cover)).thenReturn(mockCoverView);
        when(mockRootView.findViewById(R.id.no_book_message)).thenReturn(mockNoBookMessageView);

        // Glideのモックチェーンを設定
        // Glide.with(any(View.class)) が mockRequestManager を返す
        mockStatic(Glide.class);
        when(Glide.with(any(View.class))).thenReturn(mockRequestManager);
        // mockRequestManager.load(anyString()) が mockRequestBuilder を返す
        when(mockRequestManager.load(anyString())).thenReturn(mockRequestBuilder);
        when(mockRequestManager.load(eq((String)null))).thenReturn(mockRequestBuilder); // nullの場合も対応

        // mockRequestBuilder.apply(any(RequestOptions.class)) が mockRequestBuilder 自身を返す
        when(mockRequestBuilder.apply(any(RequestOptions.class))).thenReturn(mockRequestBuilder);
        // mockRequestBuilder.into(any(ImageView.class)) は void なので doNothing()
        doNothing().when(mockRequestBuilder).into(any(ImageView.class));
        doNothing().when(mockRequestBuilder).into(any(Target.class)); // Glide 4.x の into() には Target を取るオーバーロードもあるため追加

        // Log.w のモック
        mockedLog = mockStatic(Log.class);
        when(Log.w(anyString(), anyString())).thenReturn(0); // 戻り値はintなので適当な値を設定
    }

    @After
    public void tearDown() {
        // MockedStaticをクローズすることを忘れない
        if (mockedLog != null) {
            mockedLog.close();
        }
        if (mockedGlide != null) { // Glide MockedStaticもクローズ
            mockedGlide.close();
        }
    }


    /**
     * displayBookDetails: dataがnullで、showNoBookMessageがtrueの場合
     * 「該当書籍なし」メッセージが表示されることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void displayBookDetails_dataNullAndShowMessageTrue_showsNoBookMessage() {
        // Given
        boolean showNoBookMessage = true;

        // When
        controller.displayBookDetails(null, mockRootView, showNoBookMessage);

        // Then
        // 「該当書籍なし」メッセージが表示され、setVisibility(View.VISIBLE) が呼ばれる
        verify(mockRootView).findViewById(R.id.no_book_message);
        verify(mockNoBookMessageView).setVisibility(View.VISIBLE);

        // 他のTextViews/ImageView には setText などが呼ばれないことを確認
        verify(mockTitleView, never()).setText(anyString());
        verify(mockSummaryView, never()).setText(anyString());
        verify(mockStatusView, never()).setText(anyString());
        verify(mockCoverView, never()).setImageDrawable(any()); // Glideは使われない
        mockedLog.verify(() -> Log.w(anyString(), anyString()), never()); // Log.w は呼ばれない
    }

    /**
     * displayBookDetails: dataがnullで、showNoBookMessageがfalseの場合
     * 警告ログが出力され、メッセージは表示されないことをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void displayBookDetails_dataNullAndShowMessageFalse_logsWarningAndNoMessage() {
        // Given
        boolean showNoBookMessage = false;

        // When
        controller.displayBookDetails(null, mockRootView, showNoBookMessage);

        // Then
        // Log.w が呼ばれることを検証
        mockedLog.verify(() -> Log.w(eq("BookDetailViewController"), eq("bookDetailDataがnull")), times(1));

        // 「該当書籍なし」メッセージは表示されないことを検証
        verify(mockRootView, never()).findViewById(R.id.no_book_message);
        verify(mockNoBookMessageView, never()).setVisibility(anyInt());

        // 他のTextViews/ImageView には setText などが呼ばれないことを確認
        verify(mockTitleView, never()).setText(anyString());
        verify(mockSummaryView, never()).setText(anyString());
        verify(mockStatusView, never()).setText(anyString());
        verify(mockCoverView, never()).setImageDrawable(any());
    }

    /**
     * displayBookDetails: 全てのデータとビューが揃っている場合の正常系テスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void displayBookDetails_allDataPresent_setsAllViewsCorrectly() {
        // Given
        BookDetailData data = new BookDetailData(
                "vol123", "書籍タイトル", "書籍のあらすじです。",
                "http://example.com/cover.jpg", "public"
        );
        boolean showNoBookMessage = false;

        // When
        controller.displayBookDetails(data, mockRootView, showNoBookMessage);

        // Then
        // 各TextViewに正しいテキストが設定されることを検証
        verify(mockTitleView).setText("書籍タイトル");
        verify(mockSummaryView).setText("書籍のあらすじです。");
        verify(mockStatusView).setText("公開");
        verify(mockStatusView).setTextColor(Color.BLUE); // "public" なので青色

        // Glideが正しいURLで呼び出されることを検証
        verify(mockRequestManager).load("http://example.com/cover.jpg");
        verify(mockRequestBuilder).into(mockCoverView);

        // 「該当書籍なし」メッセージは表示されないことを検証
        verify(mockRootView, never()).findViewById(R.id.no_book_message);
        verify(mockNoBookMessageView, never()).setVisibility(anyInt());
        mockedLog.verify(() -> Log.w(anyString(), anyString()), never()); // Log.w は呼ばれない
    }

    /**
     * displayBookDetails: `publicStatus` が "private" の場合、ステータステキストが赤になることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void displayBookDetails_publicStatusIsPrivate_setsStatusTextAndColorRed() {
        // Given
        BookDetailData data = new BookDetailData(
                "vol123", "書籍タイトル", "書籍のあらすじです。",
                "http://example.com/cover.jpg", "private"
        );

        // When
        controller.displayBookDetails(data, mockRootView, false);

        // Then
        verify(mockStatusView).setText("非公開");
        verify(mockStatusView).setTextColor(Color.RED);
    }

    /**
     * displayBookDetails: `publicStatus` が "public" または "private" 以外の場合、ステータステキストが灰色になることをテスト
     * 命令網羅、分岐網羅
     */
    @Test
    public void displayBookDetails_publicStatusIsOther_setsStatusTextAndColorGray() {
        // Given
        BookDetailData data = new BookDetailData(
                "vol123", "書籍タイトル", "書籍のあらすじです。",
                "http://example.com/cover.jpg", "unknown_status" // "public" "private" 以外
        );

        // When
        controller.displayBookDetails(data, mockRootView, false);

        // Then
        verify(mockStatusView).setText("不明"); // デフォルト値の「不明」
        verify(mockStatusView).setTextColor(Color.GRAY); // 灰色
    }

    /**
     * displayBookDetails: `rootView.findViewById()` が null を返す場合 (TextView/ImageViewが見つからない場合)
     * 例外が発生せず、NullPointerException が発生しないことをテスト
     * 命令網羅
     */
    @Test
    public void displayBookDetails_findViewByIdReturnsNull_noExceptionsAndContinues() {
        // Given
        BookDetailData data = new BookDetailData(
                "vol123", "書籍タイトル", "書籍のあらすじです。",
                "http://example.com/cover.jpg", "public"
        );
        // 全ての findViewById が null を返すように設定
        when(mockRootView.findViewById(anyInt())).thenReturn(null);

        // When
        controller.displayBookDetails(data, mockRootView, false);

        // Then
        // 例外が発生しないことを確認 (自動的にテストがパスすればOK)
        // 各TextViewの setText が呼ばれないことを検証
        verify(mockTitleView, never()).setText(anyString());
        verify(mockSummaryView, never()).setText(anyString());
        verify(mockStatusView, never()).setText(anyString());
        // Glideのloadメソッドは呼ばれるが、intoは呼ばれないことを確認
        verify(mockRequestManager).load("http://example.com/cover.jpg"); // loadは呼ばれる
        verify(mockRequestBuilder, never()).into(any(ImageView.class)); // しかしintoはmockCoverViewがnullのため呼ばれない
    }

    /**
     * displayBookDetails: カバー画像URLがnullの場合
     * Glideがnullでロードを試み、intoが呼ばれないことをテスト
     * 命令網羅
     */
    @Test
    public void displayBookDetails_coverImageUrlIsNull_glideLoadsNullAndDoesNotTargetView() {
        // Given
        BookDetailData data = new BookDetailData(
                "vol123", "書籍タイトル", "書籍のあらすじです。",
                null, "public" // 画像URLがnull
        );

        // When
        controller.displayBookDetails(data, mockRootView, false);

        // Then
        // Glide.with().load(null) が呼ばれることを検証
        verify(mockRequestManager).load(eq((String) null));
        // into は呼ばれないことを検証 (ImageViewがnullでなくても)
        verify(mockRequestBuilder, never()).into(any(ImageView.class)); // null URLの場合、intoは呼ばれない
    }

    /**
     * displayBookDetails: カバー画像URLが空文字列の場合
     * Glideが空文字列でロードを試み、intoが呼ばれないことをテスト
     * 命令網羅
     */
    @Test
    public void displayBookDetails_coverImageUrlIsEmpty_glideLoadsEmptyAndDoesNotTargetView() {
        // Given
        BookDetailData data = new BookDetailData(
                "vol123", "書籍タイトル", "書籍のあらすじです。",
                "", "public" // 画像URLが空
        );

        // When
        controller.displayBookDetails(data, mockRootView, false);

        // Then
        // Glide.with().load("") が呼ばれることを検証
        verify(mockRequestManager).load(eq(""));
        // into は呼ばれないことを検証 (ImageViewがnullでなくても)
        verify(mockRequestBuilder, never()).into(any(ImageView.class)); // 空URLの場合、intoは呼ばれない
    }
}