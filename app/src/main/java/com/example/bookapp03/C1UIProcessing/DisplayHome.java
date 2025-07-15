/**
 * モジュール名: ホーム画面表示
 * 作成者: 鶴田凌
 * 作成日: 2025/06/20
 * 概要: 本のタイトル入力欄およびハイライトメモ登録欄を表示・操作する Activity
 * 履歴:
 * 2025/06/20 鶴田凌 新規作成
 * 2025/07/05 鶴田凌 入力値の永続化対応
 * 2025/07/07 鶴田凌 ダークモード設定の復元機能追加
 */
package com.example.bookapp03.C1UIProcessing;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate; // ← 追加

import com.example.bookapp03.C5UserInformationManaging.UserAuthManager;
import com.example.bookapp03.C6BookInformationManaging.database.BookInformationDatabase;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoDao;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoEntity;
import com.example.bookapp03.R;
import com.example.bookapp03.C4SearchProcessing.Book;
import com.example.bookapp03.C7SearchManaging.GoogleBooksApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DisplayHome extends AppCompatActivity {
    // SharedPreferences 用キー
    private static final String PREFS_NAME = "DisplayHomePrefs";
    private static final String KEY_BOOK_NAME = "key_book_name";
    private static final String KEY_PAGE = "key_page";
    private static final String KEY_LINE = "key_line";
    private static final String KEY_MEMO = "key_memo";

    // ダークモード設定用キー（追加）
    private static final String DARK_MODE_PREFS = "app_prefs";
    private static final String KEY_DARK = "dark_mode";

    private static final String TAG = "DisplayHome";

    /**
     * 本の名前入力欄（自動補完機能付き）
     */
    private AutoCompleteTextView bookNameInput;

    /**
     * ページ数入力欄
     */
    private EditText editPage;

    /**
     * 行数入力欄
     */
    private EditText editLine;

    /**
     * メモ入力欄
     */
    private EditText editMemo;

    /**
     * ページ数増減ボタン
     */
    private ImageButton btnPageUp, btnPageDown;

    /**
     * 行数増減ボタン
     */
    private ImageButton btnLineUp, btnLineDown;

    /**
     * ハイライト・メモを登録する ImageButton
     */
    private ImageButton btnHighlightSubmit;

    /**
     * 全体まとめ画面へ遷移するボタン
     */
    private Button btnSummary;

    /**
     * ユーザ設定画面へ遷移する ImageButton
     */
    private ImageButton btnUserSettings;

    /**
     * ハイライトメモの入力チェックと DB 登録を行うコントローラ
     */
    private ControlHighlightMemo controlHighlightMemo;

    /**
     * 全体まとめ画面遷移を制御するコントローラ
     */
    private ControlButtonToSummary controlButtonToSummary;

    /**
     * ユーザ設定画面遷移を制御するコントローラ
     */
    private ControlButtonToSetting controlButtonToSetting;

    /**
     * Google Books API サービス
     */
    private GoogleBooksApiService googleBooksApiService;

    /**
     * 検索結果の書籍リスト
     */
    private List<Book> searchResults;

    /**
     * 現在選択されている書籍のvolumeId
     */
    private String currentVolumeId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ダークモード設定を復元（super.onCreate前に実行）
        restoreDarkModeSettings();

        super.onCreate(savedInstanceState);
        new TutorialManager(this).showTutorialIfNeeded(this, () -> {
        });
        setContentView(R.layout.homedisplay);

        Log.d(TAG, "DisplayHome onCreate開始");

        // 1) View バインド
        initializeViews();

        // 2) Google Books API サービス初期化
        googleBooksApiService = new GoogleBooksApiService();

        // 3) UID取得とコントローラ初期化
        String uid = UserAuthManager.getCurrentUid();
        Log.d(TAG, "取得したUID: " + uid);

        // 初期状態ではvolumeIdは空
        controlHighlightMemo = new ControlHighlightMemo(this, uid, currentVolumeId);
        controlButtonToSummary = new ControlButtonToSummary();
        controlButtonToSetting = new ControlButtonToSetting();

        // 4) 本のタイトル自動補完設定
        setupBookTitleAutoComplete();

        // 5) ハイライト・メモ登録処理
        setupHighlightMemoRegistration();

        // 6) その他のボタン設定
        setupOtherButtons();

        // 追加：前回入力内容を復元
        loadSavedInputs();

        // ページ数・行数の増減ボタン設定
        setupPageControls();
        setupLineControls();

        // BottomNavigationView の設定
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        ControlBottomNavigationBar navController = new ControlBottomNavigationBar();

        // 現在の画面を選択状態にする
        navController.setCurrentItem(this, bottomNav);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 追加：アプリがバックグラウンドに行く前に保存
        saveInputs();
    }

    /**
     * View要素を初期化
     */
    private void initializeViews() {
        bookNameInput = findViewById(R.id.bookNameInput);
        editPage = findViewById(R.id.editPage);
        editLine = findViewById(R.id.editLine);
        editMemo = findViewById(R.id.editMemo);
        btnPageUp = findViewById(R.id.btnPageUp);
        btnPageDown = findViewById(R.id.btnPageDown);
        btnLineUp = findViewById(R.id.btnLineUp);
        btnLineDown = findViewById(R.id.btnLineDown);
        btnHighlightSubmit = findViewById(R.id.btnRegister);
        btnSummary = findViewById(R.id.registerButton);
        btnUserSettings = findViewById(R.id.btnUserSettings);
    }

    /**
     * 本のタイトル自動補完機能を設定
     */
    private void setupBookTitleAutoComplete() {
        bookNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 何もしない
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) { // 2文字以上で検索開始
                    searchBookSuggestions(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 何もしない
            }
        });

        // 項目が選択されたときの処理
        bookNameInput.setOnItemClickListener((parent, view, position, id) -> {
            // タップされた候補タイトルで入力欄だけ更新
            Book selected = searchResults.get(position);
            bookNameInput.setText(selected.getTitle());
            bookNameInput.setSelection(selected.getTitle().length());
            Toast.makeText(this, "書籍が選択されました: " + selected.getTitle(), Toast.LENGTH_SHORT).show();

            // ※※ここではvolumeIdを設定しない※※
        });
    }

    /**
     * Google Books APIで書籍を検索して候補を表示
     * @param query 検索クエリ
     */
    private void searchBookSuggestions(String query) {
        Log.d(TAG, "書籍検索開始: " + query);

        googleBooksApiService.searchBooks(query, new GoogleBooksApiService.SearchCallback() {
            @Override
            public void onSearchResultsReceived(List<Book> books) {
                runOnUiThread(() -> {
                    searchResults = books;
                    updateAutoCompleteAdapter(books);
                    Log.d(TAG, "検索結果: " + books.size() + "件");
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Log.e(TAG, "書籍検索エラー: " + errorMessage);
                    Toast.makeText(DisplayHome.this, "書籍検索に失敗しました",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * AutoCompleteTextViewのアダプターを更新
     * @param books 検索結果の書籍リスト
     */
    private void updateAutoCompleteAdapter(List<Book> books) {
        String[] bookTitles = new String[books.size()];
        for (int i = 0; i < books.size(); i++) {
            bookTitles[i] = books.get(i).getTitle();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                bookTitles
        );

        bookNameInput.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * ハイライトメモ登録処理を設定
     */
    private void setupHighlightMemoRegistration() {
        btnHighlightSubmit.setOnClickListener(v -> {
            String title = bookNameInput.getText().toString().trim();
            // 登録時に初めてvolumeIdを決定
            String volumeId = findVolumeIdByTitle(title);
            if (volumeId.isEmpty()) {
                Toast.makeText(this, "本の名前を正しく入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = UserAuthManager.getCurrentUid();
            controlHighlightMemo = new ControlHighlightMemo(this, uid, volumeId);

            String pageText = editPage.getText().toString().trim();
            String lineText = editLine.getText().toString().trim();
            String memo = editMemo.getText().toString().trim();
            if (pageText.isEmpty()) {
                Toast.makeText(this, "ページ数を入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lineText.isEmpty()) {
                Toast.makeText(this, "行数を入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int page = Integer.parseInt(pageText);
                int line = Integer.parseInt(lineText);
                HighlightMemoData data = controlHighlightMemo.getHighlightMemo(page, line, memo);
                boolean ok = controlHighlightMemo.sendData(data);
                Toast.makeText(this, ok ? "登録成功" : "登録失敗", Toast.LENGTH_SHORT).show();
                if (ok) {
                    // 登録成功時はメモ入力欄をクリア
                    editMemo.setText("");
                    
                    verifyDataInDatabase();
                }
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * タイトル文字列から検索結果リストをたどり volumeId を返す
     * @param title 検索対象のタイトル
     * @return volumeId 検索結果のvolumeId
     */
    private String findVolumeIdByTitle(String title) {
        if (searchResults == null) return "";
        for (Book b : searchResults) {
            if (Objects.equals(b.getTitle(), title)) {
                return b.getId();
            }
        }
        return "";
    }

    /**
     * データベース内容を確認してログ出力
     */
    private void verifyDataInDatabase() {
        String uid = UserAuthManager.getCurrentUid();

        Log.d(TAG, "データベース確認開始 - UID: " + uid + ", VolumeID: " + currentVolumeId);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                HighlightMemoDao dao = BookInformationDatabase
                        .getDatabase(this)
                        .highlightMemoDao();

                List<HighlightMemoEntity> entities = dao.getByUserAndVolume(uid, currentVolumeId);

                runOnUiThread(() -> {
                    Log.d(TAG, "=== データベース内容確認 ===");
                    Log.d(TAG, "総メモ数: " + entities.size());

                    for (int i = 0; i < entities.size(); i++) {
                        HighlightMemoEntity entity = entities.get(i);
                        Log.d(TAG, "メモ" + (i + 1) + " - Page: " + entity.page +
                                ", Line: " + entity.line +
                                ", Memo: '" + entity.memo + "'");
                    }
                    Log.d(TAG, "=== 確認終了 ===");
                });

            } catch (Exception e) {
                Log.e(TAG, "データベース確認エラー", e);
            } finally {
                executor.shutdown();
            }
        });
    }

    /**
     * その他のボタン設定
     */
    private void setupOtherButtons() {
        // 全体まとめ画面へ遷移
        btnSummary.setOnClickListener(v -> {
            // ① 入力中タイトルを取得
            String bookTitle = bookNameInput.getText().toString().trim();
            // ② タイトルから volumeId を検索（見つからなければ ""）
            String volumeId = findVolumeIdByTitle(bookTitle);
            Log.d(TAG, "Summary遷移 - Title: " + bookTitle + ", VolumeID: " + volumeId);
            // ③ 画面遷移（volumeId が空でも OK）
            controlButtonToSummary.setToSummary(this, volumeId, bookTitle);
        });

        // ユーザ設定画面へ遷移
        btnUserSettings.setOnClickListener(v -> controlButtonToSetting.setToSetting(this));

        // ナビゲーションバーアイテム選択
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setOnItemSelectedListener(item -> {
            new ControlBottomNavigationBar().handledisplay(item.getItemId(), this);
            return true;
        });
    }

    /**
     * ページ数増減ボタンの処理を設定
     */
    private void setupPageControls() {
        btnPageUp.setOnClickListener(v -> {
            int currentPage = getCurrentPageValue();
            if (currentPage < 1000) {
                editPage.setText(String.valueOf(currentPage + 1));
            } else {
                Toast.makeText(this, "ページ数の最大値は1000です", Toast.LENGTH_SHORT).show();
            }
        });

        btnPageDown.setOnClickListener(v -> {
            int currentPage = getCurrentPageValue();
            if (currentPage > 1) {
                editPage.setText(String.valueOf(currentPage - 1));
            } else {
                Toast.makeText(this, "ページ数の最小値は1です", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 行数増減ボタンの処理を設定
     */
    private void setupLineControls() {
        btnLineUp.setOnClickListener(v -> {
            int currentLine = getCurrentLineValue();
            if (currentLine < 50) {
                editLine.setText(String.valueOf(currentLine + 1));
            } else {
                Toast.makeText(this, "行数の最大値は50です", Toast.LENGTH_SHORT).show();
            }
        });

        btnLineDown.setOnClickListener(v -> {
            int currentLine = getCurrentLineValue();
            if (currentLine > 1) {
                editLine.setText(String.valueOf(currentLine - 1));
            } else {
                Toast.makeText(this, "行数の最小値は1です", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ページ数・行数の入力値を取得
     * @return 入力値
     */
    private int getCurrentPageValue() {
        try {
            String pageText = editPage.getText().toString().trim();
            if (pageText.isEmpty()) {
                editPage.setText("1");
                return 1;
            }
            int page = Integer.parseInt(pageText);
            if (page < 1) {
                editPage.setText("1");
                return 1;
            } else if (page > 1000) {
                editPage.setText("1000");
                return 1000;
            }
            return page;
        } catch (NumberFormatException e) {
            editPage.setText("1");
            return 1;
        }
    }

    /**
     * 行数入力欄の入力値を取得する
     * @return 入力値
     */
    private int getCurrentLineValue() {
        try {
            String lineText = editLine.getText().toString().trim();
            if (lineText.isEmpty()) {
                editLine.setText("1");
                return 1;
            }
            int line = Integer.parseInt(lineText);
            if (line < 1) {
                editLine.setText("1");
                return 1;
            } else if (line > 50) {
                editLine.setText("50");
                return 50;
            }
            return line;
        } catch (NumberFormatException e) {
            editLine.setText("1");
            return 1;
        }
    }

    /**
     * SharedPreferences から前回の入力値を読み込んで View にセット
     */
    private void loadSavedInputs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        bookNameInput.setText(prefs.getString(KEY_BOOK_NAME, ""));
        editPage.setText(prefs.getString(KEY_PAGE, ""));
        editLine.setText(prefs.getString(KEY_LINE, ""));
        editMemo.setText(prefs.getString(KEY_MEMO, ""));
    }

    /**
     * SharedPreferences に現在の入力値を保存
     */
    private void saveInputs() {
        SharedPreferences.Editor ed = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        ed.putString(KEY_BOOK_NAME, bookNameInput.getText().toString());
        ed.putString(KEY_PAGE, editPage.getText().toString());
        ed.putString(KEY_LINE, editLine.getText().toString());
        ed.putString(KEY_MEMO, editMemo.getText().toString());
        ed.apply();
    }

    /**
     * SharedPreferences からダークモード設定を読み込み、適用する
     */
    private void restoreDarkModeSettings() {
        SharedPreferences prefs = getSharedPreferences(DARK_MODE_PREFS, Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK, false);

        Log.d(TAG, "ダークモード設定復元: " + (isDark ? "ダークモード" : "ライトモード"));

        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    /**
     * Google Books API サービスのシャットダウン
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (googleBooksApiService != null) {
            googleBooksApiService.shutdown();
        }
    }
}
