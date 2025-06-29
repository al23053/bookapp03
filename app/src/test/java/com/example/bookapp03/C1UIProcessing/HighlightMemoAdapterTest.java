package com.example.bookapp03.C1UIProcessing;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

/**
 * HighlightMemoAdapter の単体テスト
 * - ブラックボックス: setItems → getItemCount の検証
 * - ホワイトボックス: onBindViewHolder で holder.bind呼び出し
 */
@RunWith(MockitoJUnitRunner.class)
public class HighlightMemoAdapterTest {

    @Mock private ViewGroup mockParent;
    @Mock private HighlightMemoViewHolder mockHolder;

    private HighlightMemoAdapter adapter;
    private List<HighlightMemoData> sampleList;

    @Before
    public void setUp() {
        adapter = new HighlightMemoAdapter();
        sampleList = Arrays.asList(
            new HighlightMemoData(1,1,"A"),
            new HighlightMemoData(2,2,"B")
        );
        // parent.getContext() と inflate を動かすには Robolectric が必要。ここはホワイトボックスの一部を簡略化
        when(mockParent.getContext()).thenReturn(null);
    }

    @Test
    public void testSetItems_andGetItemCount() {
        adapter.setItems(sampleList);
        // ブラックボックス: アイテム数が同じになる
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testOnBindViewHolder_delegatesToViewHolder() {
        adapter.setItems(sampleList);
        // ホワイトボックス: onBindViewHolder で bind() が呼ばれる
        adapter.onBindViewHolder(mockHolder, 1);
        verify(mockHolder).bind(sampleList.get(1));
    }
}