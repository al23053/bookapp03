package com.example.bookapp03.C1UIProcessing;

import android.content.Intent;
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

import com.example.bookapp03.R;
import com.example.bookapp03.model.Book;
import com.example.bookapp03.service.GoogleBooksApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.bookapp03.C5UserInformationManaging.UserAuthManager;
import com.example.bookapp03.C6BookInformationManaging.database.BookInformationDatabase;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoDao;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DisplayHome extends AppCompatActivity {

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
        super.onCreate(savedInstanceState);
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

        // 5) ページ数・行数増減ボタンの処理
        setupPageControls();
        setupLineControls();

        // 6) ハイライト・メモ登録処理
        setupHighlightMemoRegistration();

        // 7) その他のボタン設定
        setupOtherButtons();
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
            if (searchResults != null && position < searchResults.size()) {
                Book selectedBook = searchResults.get(position);
                currentVolumeId = selectedBook.getId();
                
                Log.d(TAG, "書籍が選択されました: " + selectedBook.getTitle());
                Log.d(TAG, "VolumeID: " + currentVolumeId);
                
                // コントローラのvolumeIdを更新
                String uid = UserAuthManager.getCurrentUid();
                controlHighlightMemo = new ControlHighlightMemo(this, uid, currentVolumeId);
                
                Toast.makeText(this, "書籍が選択されました: " + selectedBook.getTitle(), 
                               Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Google Books APIで書籍を検索して候補を表示
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
            try {
                // 1. 書籍が選択されているかチェック
                if (currentVolumeId.isEmpty()) {
                    Toast.makeText(this, "書籍を選択してください", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. 入力値の取得と検証
                String pageText = editPage.getText().toString().trim();
                String lineText = editLine.getText().toString().trim();
                String memo = editMemo.getText().toString().trim();
                
                Log.d(TAG, "=== ハイライトメモ登録開始 ===");
                Log.d(TAG, "VolumeID: " + currentVolumeId);
                Log.d(TAG, "入力値 - Page: '" + pageText + "', Line: '" + lineText + "', Memo: '" + memo + "'");
                
                if (pageText.isEmpty() || lineText.isEmpty()) {
                    Toast.makeText(this, "ページ数と行数を入力してください", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                int page = Integer.parseInt(pageText);
                int line = Integer.parseInt(lineText);
                
                // 3. データ作成と登録
                HighlightMemoData data = controlHighlightMemo.getHighlightMemo(page, line, memo);
                boolean ok = controlHighlightMemo.sendData(data);
                
                Log.d(TAG, "登録結果: " + ok);
                Toast.makeText(this, ok ? "登録成功" : "登録失敗", Toast.LENGTH_SHORT).show();
                
                if (ok) {
                    // 登録成功時は入力欄をクリア
                    editPage.setText("");
                    editLine.setText("");
                    editMemo.setText("");
                    
                    // データベース内容を確認
                    verifyDataInDatabase();
                }
                
            } catch (NumberFormatException e) {
                Log.e(TAG, "数値変換エラー", e);
                Toast.makeText(this, "ページ・行数は数字で入力してください", Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "バリデーションエラー", e);
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "予期しないエラー", e);
                Toast.makeText(this, "エラーが発生しました: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        // 全体まとめ画面へ遷移（volumeIdと本の名前を渡す）
        btnSummary.setOnClickListener(v -> {
            if (currentVolumeId.isEmpty()) {
                Toast.makeText(this, "書籍を選択してください", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String selectedBookTitle = bookNameInput.getText().toString().trim();
            Log.d(TAG, "全体まとめ画面へ遷移 - VolumeID: " + currentVolumeId + ", BookTitle: " + selectedBookTitle);
            
            // Intentに本の名前も追加
            Intent intent = new Intent(this, DisplaySummary.class);
            intent.putExtra("volumeId", currentVolumeId);
            intent.putExtra("bookTitle", selectedBookTitle);
            startActivity(intent);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (googleBooksApiService != null) {
            googleBooksApiService.shutdown();
        }
    }
}
