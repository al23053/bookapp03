package com.example.bookapp03.C1UIProcessing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.widget.RadioGroup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

/**
 * モジュール名: ControlAccountSwitching 単体テスト
 * 作成者: 作成者名
 * 作成日: 2025/06/22
 * 概要: RadioGroup#setOnCheckedChangeListener が正しく呼び出されるか検証
 * 履歴:
 *   2025/06/22 作成者名 新規作成
 */
@RunWith(RobolectricTestRunner.class)
public class ControlAccountSwitchingTest {

    @Mock
    private Activity mockActivity;

    @Mock
    private RadioGroup mockRadioGroup;

    private ControlAccountSwitching controller;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ControlAccountSwitching(mockActivity, mockRadioGroup);
    }

    /**
     * bind() を呼び出すと
     * RadioGroup#setOnCheckedChangeListener(...) が必ず呼ばれる。
     */
    @Test
    public void testBind_setsOnCheckedChangeListener() {
        // 実行
        controller.bind();
        // 検証
        verify(mockRadioGroup)
            .setOnCheckedChangeListener(any(RadioGroup.OnCheckedChangeListener.class));
    }
}