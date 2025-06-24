package com.example.bookapp03.C1UIProcessing;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.view.View;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * HighlightMemoViewHolder の単体テスト
 * - ブラックボックス: bind 呼び出しで TextView#setText が呼ばれる
 * - ホワイトボックス: 表示フォーマットを検証
 */
@RunWith(MockitoJUnitRunner.class)
public class HighlightMemoViewHolderTest {

    @Mock private View mockItemView;
    @Mock private TextView mockTvPageLine;
    @Mock private TextView mockTvMemo;

    private HighlightMemoViewHolder viewHolder;

    @Before
    public void setUp() {
        // findViewById の戻りをモック登録
        when(mockItemView.findViewById(R.id.tvPageLine)).thenReturn(mockTvPageLine);
        when(mockItemView.findViewById(R.id.tvMemo)).thenReturn(mockTvMemo);
        viewHolder = new HighlightMemoViewHolder(mockItemView);
    }

    @Test
    public void testBind_setsFormattedText() {
        HighlightMemoData data = new HighlightMemoData(5, 12, "テスト");
        viewHolder.bind(data);

        // ブラックボックス: setText が呼ばれる
        verify(mockTvPageLine).setText("P: 5  L: 12");
        verify(mockTvMemo).setText("テスト");
    }
}