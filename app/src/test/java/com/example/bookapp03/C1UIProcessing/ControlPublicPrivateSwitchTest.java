package com.example.bookapp03.C1UIProcessing;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.widget.Switch;

import com.example.bookapp03.C6BookInformationManaging.database.SummaryDao;
import com.example.bookapp03.C6BookInformationManaging.database.SummaryEntity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ExecutorService;

/**
 * ControlPublicPrivateSwitch の単体テスト
 * - ブラックボックス: bind() で executor.execute() が呼ばれる
 * - ホワイトボックス: スイッチに true/false がセットされる
 */
@RunWith(MockitoJUnitRunner.class)
public class ControlPublicPrivateSwitchTest {

    @Mock private Activity mockActivity;
    @Mock private SummaryDao mockDao;
    @Mock private Switch mockSwitch;

    private ControlPublicPrivateSwitch controller;
    private final ExecutorService instantExec = Runnable::run;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ControlPublicPrivateSwitch(
            mockActivity, mockDao, "uid123", "vol456", instantExec
        );
    }

    @Test
    public void testBind_setsCheckedTrue() {
        SummaryEntity ent = new SummaryEntity("uid123","vol456","sum", true);
        when(mockDao.getSummary("uid123","vol456")).thenReturn(ent);
        controller.bind(mockSwitch);
        // ホワイトボックス: runOnUiThread で setChecked(true) が呼ばれる
        verify(mockSwitch).setChecked(true);
    }

    public void testBind() {
    }
}