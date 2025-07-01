package com.example.bookapp03.C1UIProcessing;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.bookapp03.C5UserInformationManaging.UserAuthManager;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryDao;
import com.example.bookapp03.C6BookInformationManaging.database.BookInformationDatabase;
import com.example.bookapp03.model.Book;
import com.example.bookapp03.service.GoogleBooksApiService;
import com.example.bookapp03.R;

/**
 * モジュール名: 全体まとめ登録画面表示
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: 全体まとめ登録画面を表示し、本の名前の自動補完機能を提供するクラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 * 2025/07/01 鶴田凌 本の名前引き継ぎと自動補完機能を追加
 */
public class DisplaySummary extends AppCompatActivity {
    
    private static final String TAG = "DisplaySummary";
    
    private String currentUid;
    private String currentVolumeId;

    /**
     * 本の名前入力欄（自動補完機能付き）
     */
    private AutoCompleteTextView bookNameInput;
    
    private EditText summaryInput;
    private Switch switchPublic;
    private ImageButton btnBack, btnMenu, btnRegisterSummary;

    private SummaryDao summaryDao;
    private ExecutorService executor;

    /**
     * Google Books API サービス
     */
    private GoogleBooksApiService googleBooksApiService;

    /**
     * 検索結果の書籍リスト
     */
    private List<Book> searchResults;

    private ControlBackToHomeButton ctrlBack;
    private ControlHamburgerBar ctrlMenu;
    private ControlPublicPrivateSwitch ctrlSwitch;
    private ControlSummaryRegistrationButton ctrlRegister;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summarydisplay);
        
        Log.d(TAG, "DisplaySummary onCreate開始");

        try {
            // SummaryDao／Executor 初期化
            summaryDao = BookInformationDatabase
                    .getDatabase(this)
                    .summaryDao();
            executor = Executors.newSingleThreadExecutor();

            // Google Books API サービス初期化
            googleBooksApiService = new GoogleBooksApiService();

            // View バインド
            initializeViews();

            // Intent からデータ取得
            currentUid = UserAuthManager.getCurrentUid();
            currentVolumeId = getIntent().getStringExtra("volumeId");
            String bookTitle = getIntent().getStringExtra("bookTitle");
            
            Log.d(TAG, "Intent データ - UID: " + currentUid + ", VolumeID: " + currentVolumeId + ", BookTitle: " + bookTitle);

            // ホーム画面から渡された本の名前を設定
            if (bookTitle != null && !bookTitle.isEmpty()) {
                bookNameInput.setText(bookTitle);
                Log.d(TAG, "本の名前を設定: " + bookTitle);
                Toast.makeText(this, "選択された書籍: " + bookTitle, Toast.LENGTH_SHORT).show();
            }

            // 本のタイトル自動補完設定
            setupBookTitleAutoComplete();

            // 各コントローラ生成・バインド
            setupControllers();
            
            Log.d(TAG, "DisplaySummary 初期化完了");
            
        } catch (Exception e) {
            Log.e(TAG, "DisplaySummary 初期化エラー", e);
            Toast.makeText(this, "初期化エラーが発生しました", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * View要素を初期化
     */
    private void initializeViews() {
        bookNameInput = findViewById(R.id.bookNameInput);
        summaryInput = findViewById(R.id.summaryInput);
        switchPublic = findViewById(R.id.switchPublic);
        btnBack = findViewById(R.id.btnBack);
        btnMenu = findViewById(R.id.btnMenu);
        btnRegisterSummary = findViewById(R.id.btnRegisterSummary);
        
        Log.d(TAG, "View要素の初期化完了");
    }

    /**
     * 本のタイトル自動補完機能を設定
     */
    private void setupBookTitleAutoComplete() {
        Log.d(TAG, "自動補完機能の設定開始");
        
        // 自動補完の設定をコードで行う
        bookNameInput.setThreshold(2); // 2文字で自動補完開始
        bookNameInput.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT); // ✅ 修正済み
        
        bookNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 何もしない
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) { // 2文字以上で検索開始
                    Log.d(TAG, "自動補完検索開始: " + query);
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
                String newVolumeId = selectedBook.getId();
                
                Log.d(TAG, "書籍が選択されました: " + selectedBook.getTitle());
                Log.d(TAG, "旧VolumeID: " + currentVolumeId + " → 新VolumeID: " + newVolumeId);
                
                // VolumeIDが変更された場合は更新
                if (!newVolumeId.equals(currentVolumeId)) {
                    currentVolumeId = newVolumeId;
                    
                    // コントローラを再初期化（新しいvolumeIdで）
                    setupControllers();
                    
                    Toast.makeText(this, "書籍が変更されました: " + selectedBook.getTitle(), 
                                   Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "同じ書籍が選択されました");
                }
            }
        });
        
        Log.d(TAG, "自動補完機能の設定完了");
    }

    /**
     * Google Books APIで書籍を検索して候補を表示
     */
    private void searchBookSuggestions(String query) {
        Log.d(TAG, "Google Books API検索開始: " + query);
        
        googleBooksApiService.searchBooks(query, new GoogleBooksApiService.SearchCallback() {
            @Override
            public void onSearchResultsReceived(List<Book> books) {
                runOnUiThread(() -> {
                    searchResults = books;
                    updateAutoCompleteAdapter(books);
                    Log.d(TAG, "検索結果取得: " + books.size() + "件");
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Log.e(TAG, "書籍検索エラー: " + errorMessage);
                    // エラーは表示しない（ユーザビリティのため）
                });
            }
        });
    }

    /**
     * AutoCompleteTextViewのアダプターを更新
     */
    private void updateAutoCompleteAdapter(List<Book> books) {
        if (books == null || books.isEmpty()) {
            Log.d(TAG, "検索結果が空です");
            return;
        }
        
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
        
        Log.d(TAG, "アダプター更新完了: " + bookTitles.length + "件");
    }
    
    /**
     * コントローラを設定
     */
    private void setupControllers() {
        Log.d(TAG, "コントローラ設定開始 - VolumeID: " + currentVolumeId);
        
        ctrlBack = new ControlBackToHomeButton(this);
        ctrlBack.bind(btnBack);

        ctrlMenu = new ControlHamburgerBar(this, currentUid, currentVolumeId);
        ctrlMenu.bind(btnMenu);

        ctrlSwitch = new ControlPublicPrivateSwitch(
                this, summaryDao, currentUid, currentVolumeId, executor
        );
        ctrlSwitch.bind(switchPublic);

        ctrlRegister = new ControlSummaryRegistrationButton(
                this, summaryDao, currentUid, currentVolumeId, executor
        );
        ctrlRegister.bind(btnRegisterSummary, summaryInput, switchPublic);
        
        Log.d(TAG, "コントローラ設定完了");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        if (googleBooksApiService != null) {
            googleBooksApiService.shutdown();
        }
        Log.d(TAG, "DisplaySummary onDestroy完了");
    }
}
