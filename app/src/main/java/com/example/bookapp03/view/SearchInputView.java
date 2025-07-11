package com.example.bookapp03.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp03.R;
import com.example.bookapp03.adapter.SuggestionAdapter;
import com.example.bookapp03.model.Book;
import com.example.bookapp03.C4SearchProcessing.SearchFeatureProcessor;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

/**
 * 検索入力フィールド、検索ボタン、検索候補リストをまとめたカスタムビューです。
 * SearchFeatureProcessorを経由してGoogle Books APIを使った検索候補と書籍検索のロジックをカプセル化します。
 * このビューはUI層の一部として機能し、ユーザーの検索操作を受け付け、処理部に委譲します。
 */
public class SearchInputView extends LinearLayout implements SuggestionAdapter.OnSuggestionClickListener {

    private static final String TAG = "SearchInputView";

    /**
     * 検索入力フィールドのTextInputLayoutコンポーネント。
     */
    private TextInputLayout searchInputLayout;
    /**
     * ユーザーが検索キーワードを入力するTextInputEditTextコンポーネント。
     */
    private TextInputEditText searchEditText;
    /**
     * 検索実行をトリガーするButtonコンポーネント。
     */
    private Button searchButton;
    /**
     * 検索候補を表示するためのRecyclerView。
     */
    private RecyclerView suggestionsRecyclerView;
    /**
     * suggestionsRecyclerViewにデータをバインドするためのアダプター。
     */
    private SuggestionAdapter suggestionAdapter;

    /**
     * 検索機能に関するビジネスロジックを処理するSearchFeatureProcessorインスタンス。
     * このビューから検索処理部にリクエストを委譲します。
     */
    private SearchFeatureProcessor searchFeatureProcessor;
    /**
     * UIスレッドでUIの更新を行うためのHandler。
     */
    private Handler mainHandler;
    /**
     * 検索候補の遅延取得などの非同期処理をスケジュールするためのHandler。
     */
    private Handler suggestionHandler;

    /**
     * 検索アクションの結果を親コンポーネント（例: MainActivity）に通知するためのリスナー。
     */
    private OnSearchActionListener onSearchActionListener;

    /**
     * 検索イベントを親コンポーネントに通知するためのインターフェースです。
     */
    public interface OnSearchActionListener {
        /**
         * 検索が正常に実行され、結果が返されたときに呼び出されます。
         *
         * @param searchResults 検索結果の書籍リスト
         */
        void onSearchPerformed(List<Book> searchResults);

        /**
         * 検索の実行中にエラーが発生したときに呼び出されます。
         *
         * @param errorMessage エラーメッセージ
         */
        void onSearchFailed(String errorMessage);
    }

    /**
     * SearchInputViewのコンストラクタです。
     * コードからインスタンス化される場合に使用されます。
     *
     * @param context ビューが実行されている現在のコンテキスト
     */
    public SearchInputView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    /**
     * SearchInputViewのコンストラクタです。
     * XMLレイアウトファイルからインフレートされる場合に使用されます。
     *
     * @param context ビューが実行されている現在のコンテキスト
     * @param attrs   XMLで指定された属性セット
     */
    public SearchInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * SearchInputViewのコンストラクタです。
     * XMLレイアウトファイルからインフレートされ、デフォルトのスタイル属性が適用される場合に使用されます。
     *
     * @param context      ビューが実行されている現在のコンテキスト
     * @param attrs        XMLで指定された属性セット
     * @param defStyleAttr デフォルトのスタイル属性
     */
    public SearchInputView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * ビューの共通初期化処理を行います。
     * レイアウトのインフレート、UI要素の参照取得、ハンドラー、アダプター、リスナーの設定を行います。
     *
     * @param context ビューが実行されている現在のコンテキスト
     * @param attrs   XMLで指定された属性セット（現在は使用されません）
     */
    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_search_input, this, true);

        // UI要素の初期化
        searchInputLayout = findViewById(R.id.search_input_layout_internal);
        searchEditText = findViewById(R.id.search_edit_text_internal);
        searchButton = findViewById(R.id.search_button_internal);
        suggestionsRecyclerView = findViewById(R.id.suggestions_recycler_view_internal);

        // ハンドラーの初期化
        mainHandler = new Handler(Looper.getMainLooper());
        suggestionHandler = new Handler(Looper.getMainLooper());

        // サジェストRecyclerViewのセットアップ
        setupSuggestionsRecyclerView();

        // 検索入力フィールドのリスナー設定
        setupSearchInputListeners();

        // 検索ボタンのリスナー設定
        setupSearchButtonListener();
    }

    /**
     * SearchFeatureProcessorのインスタンスをこのカスタムビューに注入します。
     * これにより、検索処理部にアクセスできるようになります。
     *
     * @param processor 初期化済みのSearchFeatureProcessorインスタンス
     */
    public void setSearchFeatureProcessor(SearchFeatureProcessor processor) {
        this.searchFeatureProcessor = processor;
    }

    /**
     * 検索アクションリスナーを設定します。
     * このリスナーを通じて、検索結果やエラーが親コンポーネントに通知されます。
     *
     * @param listener 検索イベントを受け取るOnSearchActionListenerインスタンス
     */
    public void setOnSearchActionListener(OnSearchActionListener listener) {
        this.onSearchActionListener = listener;
    }

    /**
     * 検索候補を表示するためのRecyclerViewの初期設定を行います。
     * レイアウトマネージャーとアダプターを設定します。
     */
    private void setupSuggestionsRecyclerView() {
        suggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        suggestionAdapter = new SuggestionAdapter(this);
        suggestionsRecyclerView.setAdapter(suggestionAdapter);
    }

    /**
     * 検索入力フィールド（TextInputEditText）にテキスト変更リスナーと
     * エディターアクションリスナーを設定します。
     * これにより、ユーザーの入力やキーボードアクションを検知します。
     */
    private void setupSearchInputListeners() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            /**
             * テキストが変更された後に呼び出されます。
             * 入力されたクエリに基づいて検索候補の取得をスケジュールします。
             * @param s 変更されたテキストのEditableオブジェクト
             */
            @Override
            public void afterTextChanged(Editable s) {
                suggestionHandler.removeCallbacksAndMessages(null);
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    suggestionHandler.postDelayed(() -> fetchSuggestions(query), 500);
                } else {
                    suggestionsRecyclerView.setVisibility(View.GONE);
                }
            }
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchEditText.getText().toString());
                return true;
            }
            return false;
        });
    }

    /**
     * 検索ボタンのクリックリスナーを設定します。
     * ボタンがクリックされたときに検索を実行します。
     */
    private void setupSearchButtonListener() {
        searchButton.setOnClickListener(v -> performSearch(searchEditText.getText().toString()));
    }

    /**
     * SearchFeatureProcessorを使用して検索候補を取得します。
     * このメソッドはバックグラウンドで検索処理部にリクエストを委譲します。
     *
     * @param query 検索クエリ文字列
     */
    private void fetchSuggestions(String query) {
        // SearchFeatureProcessorが注入されているかチェック
        if (searchFeatureProcessor == null) {
            Log.e(TAG, "SearchFeatureProcessorが初期化されていません。");
            mainHandler.post(() -> Toast.makeText(getContext(), "検索サービスが利用できません。", Toast.LENGTH_SHORT).show());
            suggestionsRecyclerView.setVisibility(View.GONE);
            return;
        }

        // 検索処理部に対して検索候補の取得を依頼
        searchFeatureProcessor.fetchSuggestions(query, new SearchFeatureProcessor.SuggestionsCallback() {
            /**
             * 検索候補が正常に取得されたときに呼び出されます。
             * UIスレッドでサジェストリストの表示を更新します。
             * @param suggestions 取得された検索候補のリスト
             */
            @Override
            public void onSuggestionsReceived(List<String> suggestions) {
                mainHandler.post(() -> {
                    if (!suggestions.isEmpty()) {
                        suggestionAdapter.setSuggestions(suggestions);
                        suggestionsRecyclerView.setVisibility(View.VISIBLE);
                    } else {
                        suggestionAdapter.setSuggestions(null); // 候補がなければリストをクリア
                        suggestionsRecyclerView.setVisibility(View.GONE); // リストを非表示
                    }
                });
            }

            /**
             * 検索候補の取得中にエラーが発生したときに呼び出されます。
             * UIスレッドでエラーメッセージをトースト表示し、サジェストリストを非表示にします。
             * @param errorMessage エラーメッセージ
             */
            @Override
            public void onFailure(String errorMessage) {
                mainHandler.post(() -> {
                    Toast.makeText(getContext(), "検索候補の取得に失敗しました: " + errorMessage, Toast.LENGTH_LONG).show();
                    suggestionsRecyclerView.setVisibility(View.GONE);
                });
            }
        });
    }

    /**
     * SearchFeatureProcessorを使用して書籍を検索し、結果をOnSearchActionListenerに通知します。
     * このメソッドはバックグラウンドで検索処理部にリクエストを委譲します。
     *
     * @param query 検索クエリ文字列
     */
    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            mainHandler.post(() -> Toast.makeText(getContext(), "検索キーワードを入力してください", Toast.LENGTH_SHORT).show());
            return;
        }
        mainHandler.post(() -> Toast.makeText(getContext(), "「" + query + "」を検索中...", Toast.LENGTH_SHORT).show());
        suggestionsRecyclerView.setVisibility(View.GONE); // 検索開始時に候補を非表示にする

        // SearchFeatureProcessorが注入されているかチェック
        if (searchFeatureProcessor == null) {
            Log.e(TAG, "SearchFeatureProcessorが初期化されていません。");
            mainHandler.post(() -> Toast.makeText(getContext(), "検索サービスが利用できません。", Toast.LENGTH_SHORT).show());
            if (onSearchActionListener != null) {
                onSearchActionListener.onSearchFailed("検索サービスが利用できません。");
            }
            return;
        }

        searchFeatureProcessor.performSearch(query, new SearchFeatureProcessor.SearchCallback() {
            /**
             * 書籍検索が正常に実行され、結果が返されたときに呼び出されます。
             * 親コンポーネントのOnSearchActionListenerを通じて結果を通知します。
             * @param searchResults 検索結果の書籍リスト
             */
            @Override
            public void onSearchResultsReceived(List<Book> searchResults) {
                mainHandler.post(() -> {
                    if (onSearchActionListener != null) {
                        onSearchActionListener.onSearchPerformed(searchResults);
                    }
                });
            }

            /**
             * 書籍検索の実行中にエラーが発生したときに呼び出されます。
             * 親コンポーネントのOnSearchActionListenerを通じてエラーを通知し、UIスレッドでトースト表示します。
             * @param errorMessage エラーメッセージ
             */
            @Override
            public void onFailure(String errorMessage) {
                mainHandler.post(() -> {
                    Toast.makeText(getContext(), "書籍検索に失敗しました: " + errorMessage, Toast.LENGTH_LONG).show();
                    if (onSearchActionListener != null) {
                        onSearchActionListener.onSearchFailed(errorMessage);
                    }
                });
            }
        });
    }

    /**
     * 検索候補がクリックされたときに呼び出されるコールバックメソッドです。
     * クリックされた候補を検索バーに設定し、そのキーワードで検索を実行します。
     *
     * @param suggestion クリックされた検索候補の文字列
     */
    @Override
    public void onSuggestionClick(String suggestion) {
        searchEditText.setText(suggestion);
        suggestionsRecyclerView.setVisibility(View.GONE);
        performSearch(suggestion);
    }

    /**
     * ビューが破棄される際に、関連するハンドラーのコールバックをクリアします。
     * これによりメモリリークを防ぎます。
     * アクティビティやフラグメントのライフサイクル（例: onDestroyメソッド）からこのメソッドを呼び出すことを推奨します。
     */
    public void onDestroy() {
        suggestionHandler.removeCallbacksAndMessages(null);
        Log.d(TAG, "SearchInputView handlers cleared.");
    }

    /**
     * 検索入力フィールドのテキストをクリアし、検索候補リストを非表示にします。
     */
    public void clearSearchText() {
        searchEditText.setText("");
        suggestionsRecyclerView.setVisibility(View.GONE);
    }
}
