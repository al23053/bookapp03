package com.example.bookapp03.C1UIProcessing;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;

import com.example.bookapp03.C3BookInformationProcessing.TransmitSummary;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryDao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * ControlSummaryRegistrationButton の単体テスト
 * - ブラックボックス: setOnClickListener の設定を確認
 * - ホワイトボックス: クリックで executor.execute() が呼ばれることを検証
 */
@RunWith(MockitoJUnitRunner.class)
public class ControlSummaryRegistrationButtonTest {

    @Mock Activity mockActivity;
    @Mock SummaryDao mockDao;
    @Mock ImageButton mockButton;
    @Mock EditText mockInput;
    @Mock Switch mockSwitch;

    private TestExecutor executor;
    private ControlSummaryRegistrationButton controller;

    private static class TestExecutor implements ExecutorService {
        final List<Runnable> tasks = new ArrayList<>();
        @Override public void execute(Runnable command) { tasks.add(command); }
        // その他のメソッドはデフォルト実装で空置
        public void shutdown() {} public List<Runnable> shutdownNow() { return null; }
        public boolean isShutdown() { return false; } public boolean isTerminated() { return false; }
        public boolean awaitTermination(long l, java.util.concurrent.TimeUnit tu) { return false; }
        public <T> java.util.concurrent.Future<T> submit(java.util.concurrent.Callable<T> c) { return null; }
        public <T> java.util.concurrent.Future<T> submit(Runnable r, T t) { return null; }
        public java.util.concurrent.Future<?> submit(Runnable r) { return null; }
        public <T> java.util.List<java.util.concurrent.Future<T>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<T>> cl) { return null; }
        public <T> java.util.List<java.util.concurrent.Future<T>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<T>> cl, long l, java.util.concurrent.TimeUnit tu) { return null; }
        public <T> T invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<T>> cl) { return null; }
        public <T> T invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<T>> cl, long l, java.util.concurrent.TimeUnit tu) { return null; }
    }

    @Before
    public void setUp() {
        executor = new TestExecutor();
        controller = new ControlSummaryRegistrationButton(
            mockActivity, mockDao, "uid123", "vol456", executor
        );
    }

    @Test
    public void testBind_setsOnClickListener() {
        controller.bind(mockButton, mockInput, mockSwitch);
        verify(mockButton).setOnClickListener(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void testClick_submitsToExecutor() {
        when(mockInput.getText()).thenReturn(new android.text.SpannableStringBuilder("ok"));
        when(mockSwitch.isChecked()).thenReturn(false);

        controller.bind(mockButton, mockInput, mockSwitch);
        // クリックイベントを取得して実行
        ArgumentCaptor<android.view.View.OnClickListener> cap =
            ArgumentCaptor.forClass(android.view.View.OnClickListener.class);
        verify(mockButton).setOnClickListener(cap.capture());
        cap.getValue().onClick(null);

        // ホワイトボックス: executor.execute() が一度呼ばれている
        assertEquals(1, executor.tasks.size());
    }
}