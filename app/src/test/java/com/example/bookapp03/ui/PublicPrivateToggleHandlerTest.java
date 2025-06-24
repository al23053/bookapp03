package com.example.bookapp03.ui;

import static org.mockito.Mockito.*;

import android.util.Log;

import com.example.bookapp03.presentation.viewmodel.BookListViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PublicPrivateToggleHandlerTest {

    @Mock
    private BookListViewModel mockViewModel;

    private PublicPrivateToggleHandler toggleHandler;

    @BeforeEach
    void setUp() {
        // Log.w をモック化
        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.w(anyString(), anyString())).thenReturn(0);
        }
        toggleHandler = new PublicPrivateToggleHandler(mockViewModel);
    }

    @Test
    void testHandleToggle_success() {
        String uid = "testUser";
        String volumeId = "testVolume";
        boolean newStatus = true;

        toggleHandler.handleToggle(uid, volumeId, newStatus);

        // ViewModelのupdatePublicStatusが正しい引数で呼ばれたことを検証
        verify(mockViewModel).updatePublicStatus(uid, volumeId, newStatus);
    }

    @Test
    void testHandleToggle_nullViewModel() {
        String uid = "testUser";
        String volumeId = "testVolume";
        boolean newStatus = true;

        // ViewModelがnullの場合
        PublicPrivateToggleHandler nullViewModelHandler = new PublicPrivateToggleHandler(null);
        nullViewModelHandler.handleToggle(uid, volumeId, newStatus);

        // ViewModelのメソッドは呼び出されないことを検証
        verifyNoInteractions(mockViewModel); // mockViewModelがnullHandlerに渡されていないので、これでOK

        // Log.w が呼ばれたことを検証 (これはstatic methodなので別途モックが必要)
        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.w(anyString(), anyString())).thenReturn(0);
            nullViewModelHandler.handleToggle(uid, volumeId, newStatus);
            mockedLog.verify(() -> Log.w(eq("ToggleHandler"), eq("ViewModel, UID, または VolumeId が null")));
        }
    }

    @Test
    void testHandleToggle_nullUid() {
        String volumeId = "testVolume";
        boolean newStatus = true;

        toggleHandler.handleToggle(null, volumeId, newStatus);

        verifyNoInteractions(mockViewModel); // ViewModelのメソッドは呼び出されないことを検証

        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.w(anyString(), anyString())).thenReturn(0);
            toggleHandler.handleToggle(null, volumeId, newStatus);
            mockedLog.verify(() -> Log.w(eq("ToggleHandler"), eq("ViewModel, UID, または VolumeId が null")));
        }
    }

    @Test
    void testHandleToggle_nullVolumeId() {
        String uid = "testUser";
        boolean newStatus = true;

        toggleHandler.handleToggle(uid, null, newStatus);

        verifyNoInteractions(mockViewModel); // ViewModelのメソッドは呼び出されないことを検証

        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.w(anyString(), anyString())).thenReturn(0);
            toggleHandler.handleToggle(uid, null, newStatus);
            mockedLog.verify(() -> Log.w(eq("ToggleHandler"), eq("ViewModel, UID, または VolumeId が null")));
        }
    }
}