package com.example.bookapp03.C1UIProcessing;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;                     // for drawer listener callback
import android.view.ViewGroup;               // for LayoutParams
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;            // ← 必須
import androidx.recyclerview.widget.LinearLayoutManager;   // ← 必須
import androidx.recyclerview.widget.RecyclerView;          // ← 必須

import com.example.bookapp03.C3BookInformationProcessing.TransmitSummary;
import com.example.bookapp03.C5UserInformationManaging.UserAuthManager;
import com.example.bookapp03.C6BookInformationManaging.database.BookInformationDatabase;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryDao;
import com.example.bookapp03.R;
import com.example.bookapp03.C1UIProcessing.HighlightMemoAdapter;
import com.example.bookapp03.C1UIProcessing.HighlightMemoData;
import com.example.bookapp03.C6BookInformationManaging.database.HighlightMemoEntity;
import com.example.bookapp03.model.Book;
import com.example.bookapp03.service.GoogleBooksApiService;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * モジュール名: 全体まとめ登録画面表示
 * 作成者: 鶴田凌
 * 作成日: 2025/06/15
 * 概要: 全体まとめ登録画面を表示し、本の名前の自動補完機能を提供するクラス
 * 履歴:
 * 2025/06/15 鶴田凌 新規作成
 * 2025/07/01 鶴田凌 本の名前引き継ぎと自動補完機能を追加
 * 2025/07/05 鶴田凌 既存まとめの読み込み機能を追加
 * 2025/07/07 鶴田凌 volumeId引き継ぎ問題を修正
 */
public class DisplaySummary extends AppCompatActivity {
    
    private static final String TAG = "DisplaySummary";
    
    private String currentUid;
    private String currentVolumeId;
    private String currentBookTitle; // 追加

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
            currentBookTitle = getIntent().getStringExtra("bookTitle");
            
            Log.d(TAG, "Intent データ - UID: " + currentUid + ", VolumeID: " + currentVolumeId + ", BookTitle: " + currentBookTitle);

            // ホーム画面から渡された本の名前を設定
            if (currentBookTitle != null && !currentBookTitle.isEmpty()) {
                bookNameInput.setText(currentBookTitle);
                Log.d(TAG, "本の名前を設定: " + currentBookTitle);
                Toast.makeText(this, "選択された書籍: " + currentBookTitle, Toast.LENGTH_SHORT).show();
            }

            // 本のタイトル自動補完設定
            setupBookTitleAutoComplete();

            // 各コントローラ生成・バインド
            setupControllers();
            initializeRecyclerView();

            // 追加: 画面遷移直後に既存まとめを読み込む
            loadExistingSummary();
            
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
     * 現在の volumeId に紐づく SummaryEntity を読み込み、UI に反映する
     */
    private void loadExistingSummary() {
        if (currentVolumeId == null || currentVolumeId.isEmpty()) {
            // 未取得なら何もしない
            return;
        }
        executor.execute(() -> {
            SummaryEntity entity = summaryDao.getSummary(currentUid, currentVolumeId);
            runOnUiThread(() -> {
                if (entity != null) {
                    // 既存のまとめをセット
                    if (entity.overallSummary != null) {
                        summaryInput.setText(entity.overallSummary);
                    }
                    switchPublic.setChecked(entity.isPublic);
                } else {
                    // 新規登録時はクリア状態
                    summaryInput.setText("");
                    switchPublic.setChecked(false);
                }
            });
        });
    }

    /**
     * 本のタイトル自動補完機能を設定
     */
    private void setupBookTitleAutoComplete() {
        Log.d(TAG, "自動補完機能の設定開始");
        
        // 自動補完の設定をコードで行う
        bookNameInput.setThreshold(2); // 2文字で自動補完開始
        bookNameInput.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        
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
                
                // 重要：テキストが変更されたら、ホーム画面からのvolumeIdをクリア
                if (currentBookTitle != null && !query.equals(currentBookTitle)) {
                    Log.d(TAG, "本のタイトルが変更されました。volumeIdをクリア");
                    currentVolumeId = "";
                    currentBookTitle = "";
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
                String newTitle = selectedBook.getTitle();
                
                Log.d(TAG, "書籍が選択されました: " + newTitle);
                Log.d(TAG, "旧VolumeID: " + currentVolumeId + " → 新VolumeID: " + newVolumeId);
                
                // 新しい書籍情報を設定
                currentVolumeId = newVolumeId;
                currentBookTitle = newTitle;
                
                // コントローラを再初期化（新しいvolumeIdで）
                setupControllers();
                // 新しい書籍のまとめを読み込み
                loadExistingSummary();
                
                Toast.makeText(this, "書籍が選択されました: " + newTitle, Toast.LENGTH_SHORT).show();
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

        // ① DrawerLayout を取得
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        // ② ハンバーガーアイコン取得
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        // ③ 戻るボタン取得
        btnBack = findViewById(R.id.btnBack);

        // 戻る時に保存確認ダイアログ
        btnBack.setOnClickListener(v -> showSaveConfirmDialog());

        // ハンバーガーメニュー
        ctrlMenu = new ControlHamburgerBar(this, drawer, currentUid, currentVolumeId);
        ctrlMenu.bind(btnMenu);

        ctrlSwitch = new ControlPublicPrivateSwitch(
            this, summaryDao, currentUid, currentVolumeId, executor
        );
        ctrlSwitch.bind(switchPublic);

        ctrlRegister = new ControlSummaryRegistrationButton(
            this, summaryDao, currentUid, executor
        );
        ctrlRegister.bind(btnRegisterSummary, summaryInput, switchPublic);
        
        Log.d(TAG, "コントローラ設定完了");
    }
    
    /**
     * RecyclerView セクションの初期化
     */
    private void initializeRecyclerView() {
        RecyclerView recycler = findViewById(R.id.recyclerHighlight);
        HighlightMemoAdapter adapter = new HighlightMemoAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    List<HighlightMemoEntity> list =
                        BookInformationDatabase.getDatabase(DisplaySummary.this)
                            .highlightMemoDao()
                            .getByUserAndVolume(currentUid, currentVolumeId);

                    List<HighlightMemoData> data = new ArrayList<>();
                    for (HighlightMemoEntity e : list) {
                        data.add(new HighlightMemoData(e.page, e.line, e.memo));
                    }
                    runOnUiThread(() -> adapter.setItems(data));
                });
            }
        });
        
        Log.d(TAG, "RecyclerView セクションの初期化完了");
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

    /** 外部から本の名前入力欄を参照するためのゲッター */
    public AutoCompleteTextView getBookNameInput() {
        return bookNameInput;
    }

    /**
     * タイトル文字列に対応する volumeId を返す。
     * 1. 現在のvolumeIdが設定済みで、入力タイトルが現在のタイトルと一致する場合は、現在のvolumeIdを返す
     * 2. そうでなければ、検索結果から該当するvolumeIdを探す
     * 3. 見つからなければ空文字を返す
     */
    public String findVolumeIdByTitle(String title) {
        Log.d(TAG, "volumeId検索開始 - 入力タイトル: " + title);
        Log.d(TAG, "現在のvolumeId: " + currentVolumeId + ", 現在のタイトル: " + currentBookTitle);
        
        // 現在のvolumeIdが設定済みで、タイトルが一致する場合
        if (currentVolumeId != null && 
            !currentVolumeId.isEmpty() && 
            currentBookTitle != null && 
            title.equals(currentBookTitle)) {
            Log.d(TAG, "現在のvolumeIdを使用: " + currentVolumeId);
            return currentVolumeId;
        }
        
        // 検索結果から該当するvolumeIdを探す
        if (searchResults != null) {
            for (Book b : searchResults) {
                if (b.getTitle().equals(title)) {
                    Log.d(TAG, "検索結果からvolumeIdを取得: " + b.getId());
                    return b.getId();
                }
            }
        }
        
        Log.d(TAG, "volumeIdが見つかりません: " + title);
        return "";
    }

    /**
     * モジュール名: 編集内容保存確認ダイアログ
     * 概要: 戻るボタン押下時に保存確認を行い、「はい」で保存→ホーム遷移、
     *       「いいえ」で即ホーム遷移する。
     */
    private void showSaveConfirmDialog() {
        new AlertDialog.Builder(this)
            .setTitle("確認")
            .setMessage("編集内容を保存しますか？")
            .setPositiveButton("はい", (d, w) -> saveAndReturnHome())
            .setNegativeButton("いいえ", (d, w) -> returnHome())
            .show();
    }

    /**
     * 入力値を検証したうえで全体まとめを保存し、ホーム画面へ戻る。
     */
    private void saveAndReturnHome() {
        String title = bookNameInput.getText().toString().trim();
        String volumeId = findVolumeIdByTitle(title);
        if (volumeId.isEmpty()) {
            Toast.makeText(this, "本の名前を正しく入力してください", Toast.LENGTH_SHORT).show();
            return;
        }
        String overall = summaryInput.getText().toString();
        if (overall.length() > 500) {
            Toast.makeText(this, "全体まとめは500文字以内で入力してください", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean ok = new TransmitSummary(this)
                .transmitSummary(currentUid, volumeId, overall, false);
            runOnUiThread(() -> {
                Toast.makeText(this,
                    ok ? "保存成功" : "保存失敗",
                    Toast.LENGTH_SHORT
                ).show();
                returnHome();
            });
        });
    }

    /**
     * ホーム画面へ戻る（Activity を finish する）。
     */
    private void returnHome() {
        Intent i = new Intent(this, DisplayHome.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    public String getCurrentVolumeId() {
        return currentVolumeId;
    }
}
