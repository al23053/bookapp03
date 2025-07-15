/**
 * モジュール名: DisplaySelectBook
 * 作成者: 三浦寛生
 * 作成日: 2025/06/15
 * 概要: 書籍検索結果を表示するためのActivityです。
 *  * 検索クエリに基づいてGoogle Books APIから取得した書籍リストを表示します。
 * 履歴:
 * 2025/06/15 三浦寛生 新規作成
 */
package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp03.R;
import com.example.bookapp03.C4SearchProcessing.Book;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DisplaySelectBook extends AppCompatActivity implements BookCardAdapter.OnBookClickListener {

    private static final String TAG = "SearchResultActivity";
    /**
     * Intentで検索結果のJSON文字列を渡す際に使用するキー。
     */
    public static final String EXTRA_SEARCH_RESULTS = "extra_search_results";

    /**
     * 検索画面に戻るためのボタン。
     */
    private Button backToSearchButton;

    /**
     * 検索結果の書籍リストを表示するためのRecyclerView。
     */
    private RecyclerView searchResultsRecyclerView;

    /**
     * searchResultsRecyclerViewにデータをバインドするためのアダプター。
     */
    private BookCardAdapter bookCardAdapter;

    /**
     * 現在表示されている検索結果の書籍リスト。
     */
    private List<Book> searchResults;
    private ControlPushBookImage controlPushBookImage;

    /**
     * Activityが最初に作成されるときに呼び出されます。
     * レイアウトの設定、UI要素の初期化、検索結果のロードを行います。
     *
     * @param savedInstanceState 以前に保存された状態データを含むBundleオブジェクト。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        searchResultsRecyclerView = findViewById(R.id.search_results_recycler_view);
        backToSearchButton = findViewById(R.id.backToSearchButton);

        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResults = new ArrayList<>();
        bookCardAdapter = new BookCardAdapter(searchResults, this);
        searchResultsRecyclerView.setAdapter(bookCardAdapter);
        loadSearchResultsFromIntent();
        controlPushBookImage = new ControlPushBookImage(this);
        ControlBackButton.setupBackButton(backToSearchButton, this);
    }

    /**
     * 親ActivityからIntent経由で渡された検索結果データをロードします。
     * JSON文字列として渡された書籍リストをGsonを使ってデシリアライズし、アダプターにセットします。
     */
    private void loadSearchResultsFromIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRA_SEARCH_RESULTS)) {
            String jsonSearchResults = extras.getString(EXTRA_SEARCH_RESULTS);
            if (jsonSearchResults != null) {
                Gson gson = new Gson();
                Type type = new TypeToken<List<Book>>() {
                }.getType();
                searchResults = gson.fromJson(jsonSearchResults, type);

                if (searchResults != null && !searchResults.isEmpty()) {
                    bookCardAdapter.setBookList(searchResults);
                    Log.d(TAG, "検索結果を正常にロードしました。数: " + searchResults.size());
                } else {
                    Toast.makeText(this, "検索結果が見つかりませんでした。", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Intentから検索結果がロードされましたが、リストは空でした。");
                }
            } else {
                Toast.makeText(this, "検索結果のデータがありません。", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Intentに検索結果のJSON文字列がnullでした。");
            }
        } else {
            Toast.makeText(this, "検索結果の情報が渡されませんでした。", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Intentに検索結果のキーが存在しませんでした。");
            finish();
        }
    }

    /**
     * 書籍カードがクリックされたときに呼び出されるコールバックメソッドです。
     * 選択された書籍の詳細に基づいて、感想選択画面 (BookSelectionActivity) へ遷移します。
     *
     * @param book クリックされた書籍オブジェクト
     */
    @Override
    public void onBookClick(Book book) {
        controlPushBookImage.handleBookClick(book);
    }
}
