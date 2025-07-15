/**
 * モジュール名: GenreSelectionActivity
 * 作成者: 増田学斗
 * 作成日: 2025/06/15
 * 概要: ユーザーが本のジャンルを選択する画面。選択内容をFirestoreに英語で保存し、
 * UI上では日本語で表示する。保存済みのジャンルがある場合はチェック状態を復元する。
 * 履歴:
 * 2025/06/15 増田学斗 新規作成
 * 2025/07/07 増田学斗 Firestoreからジャンル情報を読み込みチェック状態を復元する機能を追加
 * 2025/07/15 増田学斗 ジャンルを英語保存・日本語表示するマッピング機能を追加
 */
package com.example.bookapp03.C1UIProcessing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp03.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GenreSelectionActivity extends AppCompatActivity {

    private static final String TAG = "GenreSelectionActivity";
    private boolean isFirstTime = true; // 初回設定かどうかのフラグ
    private ArrayList<CheckBox> genreCheckboxes = new ArrayList<>(); // チェックボックス一覧
    private String nickname; // 受け取ったニックネーム
    private String iconBase64; // 受け取ったBase64形式のアイコン
    private Button buttonNext; // 「次へ」ボタン

    /**
     * 英語ジャンル → 日本語ジャンル のマッピング
     */
    private static final HashMap<String, String> genreMap = new HashMap<String, String>() {{
        // 省略：ジャンル名マッピング（英語→日本語）
        put("Architecture", "建築");
        put("Art", "美術");
        put("Biography & Autobiography", "伝記・自伝");
        put("Body, Mind & Spirit", "ボディ・マインド・スピリット");
        put("Business & Economics", "ビジネス・経済");
        put("Comics & Graphic Novels", "コミックス・グラフィックノベル");
        put("Children's stories", "児童書");
        put("Computers", "コンピュータ");
        put("Cooking", "料理");
        put("Crafts & Hobbies", "クラフト・趣味");
        put("Design", "デザイン");
        put("Drama", "演劇");
        put("Education", "教育");
        put("Family & Relationships", "家族・人間関係");
        put("Fiction", "小説");
        put("Foreign Language Study", "外国語学習");
        put("Health & Fitness", "健康・フィットネス");
        put("History", "歴史");
        put("House & Home", "家・住まい");
        put("Humor", "ユーモア");
        put("Juvenile Fiction", "児童小説");
        put("Juvenile Nonfiction", "児童ノンフィクション");
        put("Language Arts & Disciplines", "言語学・文学研究");
        put("Law", "法律");
        put("Literary Collections", "文学評論");
        put("Literary Criticism", "文芸評論");
        put("Mathematics", "数学");
        put("Medical", "医学");
        put("Music", "音楽");
        put("Nature", "自然");
        put("Performing Arts", "舞台芸術");
        put("Pets", "ペット");
        put("Philosophy", "哲学");
        put("Photography", "写真");
        put("Poetry", "詩");
        put("Political Science", "政治学");
        put("Psychology", "心理学");
        put("Reference", "リファレンス");
        put("Religion", "宗教");
        put("Science", "科学");
        put("Self-Help", "自己啓発");
        put("Social Science", "社会科学");
        put("Sports & Recreation", "スポーツ・レクリエーション");
        put("Study Aids", "学習補助");
        put("Technology & Engineering", "テクノロジー・エンジニアリング");
        put("Transportation", "交通");
        put("Travel", "旅行");
        put("True Crime", "実録・犯罪");
    }};

    /**
     * 日本語ジャンル → 英語ジャンル の逆マッピング（保存・読み込み時に使用）
     */
    private static final HashMap<String, String> reverseGenreMap = new HashMap<>();

    static {
        for (String key : genreMap.keySet()) {
            reverseGenreMap.put(genreMap.get(key), key);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_selection);

        Log.d(TAG, "onCreate開始");
        retrieveIntentData(); // 受け取ったデータ（ニックネーム等）を取り出す
        initializeViews();    // チェックボックス初期化
        loadExistingGenres(); // Firestoreからジャンル復元
        setupNextButton();    // 次へボタンの動作設定
    }

    /**
     * 前画面からのデータ（nickname, iconBase64, 初回設定かどうか）を取得
     */
    private void retrieveIntentData() {
        nickname = getIntent().getStringExtra("nickname");
        iconBase64 = getIntent().getStringExtra("iconBase64");
        isFirstTime = getIntent().getBooleanExtra("isFirstTime", true);
    }

    /**
     * ID生成処理を関数かして整合性を担保
     */
    private String toSnakeCaseId(String englishKey) {
        return englishKey
                .replace("&", "and")                      // & を and に
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")            // 非英数字は _
                .replaceAll("_+", "_")                    // __ を _
                .replaceAll("^_|_$", "");                 // 先頭/末尾の _ を除去
    }


    /**
     * レイアウトに定義されたチェックボックスを genreCheckboxes に格納
     * ID名は checkbox_ジャンル名（英語）をスネークケースに変換した形式
     */
    private void initializeViews() {
        for (String jp : reverseGenreMap.keySet()) {
            String english = reverseGenreMap.get(jp);
            String idName = "checkbox_" + toSnakeCaseId(english);
            int resID = getResources().getIdentifier(idName, "id", getPackageName());
            CheckBox cb = findViewById(resID);
            if (cb != null) {
                genreCheckboxes.add(cb);
            } else {
                Log.w(TAG, "未検出チェックボックスID: " + idName);
            }
        }
        buttonNext = findViewById(R.id.buttonNextToComplete);
    }


    /**
     * 「次へ」ボタン押下時の処理。選択されたジャンル（日本語）を英語に変換し、Firestoreに保存。
     */
    private void setupNextButton() {
        buttonNext.setOnClickListener(v -> {
            ArrayList<String> selectedGenres = new ArrayList<>();

            for (CheckBox cb : genreCheckboxes) {
                if (cb.isChecked()) {
                    String japanese = cb.getText().toString();
                    String english = reverseGenreMap.get(japanese);
                    if (english != null) selectedGenres.add(english);
                }
            }

            if (selectedGenres.isEmpty()) {
                Toast.makeText(this, "1つ以上選択してください", Toast.LENGTH_SHORT).show();
                return;
            }

            saveGenresToFirestore(selectedGenres);
        });
    }

    /**
     * Firestoreにジャンル（英語）を保存。ニックネームとアイコンも一緒に保存。
     */
    private void saveGenresToFirestore(List<String> selectedGenres) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String, Object> userData = new HashMap<>();
        userData.put("nickname", nickname);
        userData.put("iconBase64", iconBase64);
        userData.put("genre", selectedGenres);

        db.collection("users").document(uid).set(userData, SetOptions.merge()).addOnSuccessListener(unused -> {
            Log.d(TAG, "ユーザ情報の保存に成功");
            if (isFirstTime) {
                startActivity(new Intent(this, CompleteActivity.class));
                finish();
            } else {
                setResult(Activity.RESULT_OK);
                finish();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Firestore保存失敗", e);
            Toast.makeText(this, "登録失敗", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Firestoreから保存されたジャンル（英語）を読み込み、該当するチェックボックスをチェック状態にする
     */
    private void loadExistingGenres() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get().addOnSuccessListener(document -> {
            if (document.exists()) {
                List<String> savedGenres = (List<String>) document.get("genre");
                if (savedGenres != null) {
                    for (CheckBox cb : genreCheckboxes) {
                        String japanese = cb.getText().toString();
                        String english = reverseGenreMap.get(japanese);
                        if (english != null && savedGenres.contains(english)) {
                            cb.setChecked(true);
                        }
                    }
                    Log.d(TAG, "既存ジャンルの復元成功");
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "ジャンル読み込み失敗", e);
        });
    }
}
