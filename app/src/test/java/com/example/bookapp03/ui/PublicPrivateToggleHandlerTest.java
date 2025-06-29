import com.example.bookapp03.C3BookInformationProcessing.BookListViewModel;
import com.example.bookapp03.C1UIProcessing.PublicPrivateToggleHandler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PublicPrivateToggleHandlerTest {

    @Mock
    private BookListViewModel mockViewModel;

    private PublicPrivateToggleHandler toggleHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        toggleHandler = new PublicPrivateToggleHandler(mockViewModel);
    }

    /**
     * handleToggleが正しい引数でViewModelのupdatePublicStatusを呼び出すことを確認する
     */
    @Test
    public void handleToggle_callsViewModelUpdatePublicStatusWithCorrectArguments() {
        String uid = "testUser1";
        String volumeId = "testVolume1";
        boolean newPublicStatus = true;

        toggleHandler.handleToggle(uid, volumeId, newPublicStatus);

        verify(mockViewModel, times(1)).updatePublicStatus(eq(uid), eq(volumeId), eq(newPublicStatus));
    }

    /**
     * ViewModelがnullの場合にupdatePublicStatusが呼び出されないことを確認する
     */
    @Test
    public void handleToggle_doesNotCallUpdatePublicStatusWhenViewModelIsNull() {
        PublicPrivateToggleHandler nullViewModelHandler = new PublicPrivateToggleHandler(null);

        String uid = "testUser1";
        String volumeId = "testVolume1";
        boolean newPublicStatus = true;

        nullViewModelHandler.handleToggle(uid, volumeId, newPublicStatus);

        verify(mockViewModel, never()).updatePublicStatus(any(), any(), anyBoolean());
    }

    /**
     * UIDがnullの場合にupdatePublicStatusが呼び出されないことを確認する
     */
    @Test
    public void handleToggle_doesNotCallUpdatePublicStatusWhenUidIsNull() {
        String uid = null;
        String volumeId = "testVolume1";
        boolean newPublicStatus = true;

        toggleHandler.handleToggle(uid, volumeId, newPublicStatus);

        verify(mockViewModel, never()).updatePublicStatus(any(), any(), anyBoolean());
    }

    /**
     * VolumeIdがnullの場合にupdatePublicStatusが呼び出されないことを確認する
     */
    @Test
    public void handleToggle_doesNotCallUpdatePublicStatusWhenVolumeIdIsNull() {
        String uid = "testUser1";
        String volumeId = null;
        boolean newPublicStatus = true;

        toggleHandler.handleToggle(uid, volumeId, newPublicStatus);

        verify(mockViewModel, never()).updatePublicStatus(any(), any(), anyBoolean());
    }
}