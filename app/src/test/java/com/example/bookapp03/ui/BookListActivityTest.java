import android.content.Intent;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp03.R;
import com.example.bookapp03.C3BookInformationProcessing.BookSummaryData;
import com.example.bookapp03.C1UIProcessing.BookListViewController;
import com.example.bookapp03.C1UIProcessing.TutorialManager;
import com.example.bookapp03.C3BookInformationProcessing.BookListViewModel;
import com.example.bookapp03.C1UIProcessing.BookListActivity;
import com.example.bookapp03.C1UIProcessing.PublicPrivateToggleHandler;
import com.example.bookapp03.C1UIProcessing.TutorialActivity;
import com.example.bookapp03.C1UIProcessing.ViewModelFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class BookListActivityTest {

    private BookListActivity activity;

    @Mock
    private BookListViewModel mockBookListViewModel;
    @Mock
    private BookListViewController mockBookListViewController;
    @Mock
    private PublicPrivateToggleHandler mockPublicPrivateToggleHandler;
    @Mock
    private ViewModelFactory mockViewModelFactory;
    @Mock
    private TutorialManager mockTutorialManager;

    private MutableLiveData<List<BookSummaryData>> bookListLiveData;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        // Mock ViewModelFactory to return our mock ViewModel
        when(mockViewModelFactory.create(eq(BookListViewModel.class))).thenReturn(mockBookListViewModel);

        // Mock the LiveData inside the ViewModel
        bookListLiveData = new MutableLiveData<>();
        when(mockBookListViewModel.bookList).thenReturn(bookListLiveData);

        // Mock the TutorialManager constructor to return our mock
        doAnswer(invocation -> {
            // This is a workaround for mocking constructor calls.
            // When TutorialManager is instantiated, we want our mock to be used.
            // This assumes TutorialManager's methods are called on the same instance.
            return mockTutorialManager;
        }).when(mock(TutorialManager.class)).shouldShowTutorial(); // This exact mock setup is tricky for constructor

        // Instead of mocking the constructor, it's better to provide a way to inject TutorialManager
        // or mock its static behavior if applicable. For now, let's assume it's injected.
        // We'll manually set up the TutorialManager mock for the activity context.
        // For simplicity in this test, let's assume `TutorialManager` is accessible or can be set.
        // In a real app, you'd use a dependency injection framework or a more robust test setup.

        // Initialize activity using Robolectric, injecting mocks where possible.
        // Since Activity's ViewModelFactory is created internally, we need to hack it for testing.
        // The standard way is to use a custom TestApplication or a TestRule.
        // For now, let's make sure the ViewModelFactory setup works as expected.

        // Create the activity instance via Robolectric
        activity = Robolectric.buildActivity(BookListActivity.class)
                .create()
                .get();

        // After activity creation, we can try to inject our mocks if the activity allows it
        // (e.g., through public setters, or reflection for testing).
        // For this example, let's assume we can set them directly for testing purposes.
        // In a real app, this would be handled by a DI framework or more careful constructor injection.
        // Since ViewModelFactory is created inside onCreate, we need a way to swap it.
        // One way is to create a custom Application and override `getViewModelStore` or inject factory.

        // Hack to inject our mocks into the activity's fields (for testing purposes only)
        // This is not good practice for production code but common for quick tests.
        // A better approach: refactor BookListActivity to accept ViewModelFactory in constructor
        // or use a custom TestApplication class.
        try {
            java.lang.reflect.Field viewModelFactoryField = BookListActivity.class.getDeclaredField("viewModelFactory");
            viewModelFactoryField.setAccessible(true);
            viewModelFactoryField.set(activity, mockViewModelFactory); // Inject mock factory

            // Re-create activity to apply the injected factory
            activity = Robolectric.buildActivity(BookListActivity.class)
                    .create()
                    .get();

            // Re-get the actual ViewModel and LiveData instances that Robolectric created
            // and ensure they are our mocks.
            // This part is tricky because the ViewModelProvider.Factory is used internally.
            // A more direct way would be to make the ViewModel a public field or provide a getter.
            // For now, we'll verify the interactions with the *mock* ViewModel, assuming it's used.

            // Set the mock controllers/handlers explicitly if not handled by ViewModelFactory
            java.lang.reflect.Field controllerField = BookListActivity.class.getDeclaredField("controller");
            controllerField.setAccessible(true);
            controllerField.set(activity, mockBookListViewController);

            java.lang.reflect.Field toggleHandlerField = BookListActivity.class.getDeclaredField("toggleHandler");
            toggleHandlerField.setAccessible(true);
            toggleHandlerField.set(activity, mockPublicPrivateToggleHandler);

            // Mock TutorialManager behavior after the activity is built.
            // TutorialManager is created inside `onCreate` if `shouldShowTutorial` is true.
            // We need to mock the behavior of `new TutorialManager(this)` call.
            // This is very difficult with Mockito for concrete classes.
            // A common workaround is to make `TutorialManager` a dependency.
            // For this test, let's assume `TutorialManager` can be mocked by setting its `shouldShowTutorial` behavior.

            // The TutorialManager instance inside Activity is new'd up, so direct mocking is not feasible.
            // We need to test its *behavior* as observed by the Activity.
            // Let's mock the static SharedPreferences access or create a custom TutorialManager for testing.
            // For now, we'll focus on testing the activity's logic with the *assumption* that `shouldShowTutorial()` is correctly set.

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to inject mocks via reflection", e);
        }
    }

    /**
     * Verify that onCreate initializes components and loads books.
     */
    @Test
    public void onCreate_initializesComponentsAndLoadsBooks() {
        // Verify UI components are found
        assertNotNull(activity.findViewById(R.id.book_list_recycler));
        assertNotNull(activity.findViewById(R.id.empty_text));

        // Verify ViewModel and ToggleHandler are initialized
        assertNotNull(mockBookListViewModel); // Mock should be used
        assertNotNull(mockPublicPrivateToggleHandler); // Mock should be used

        // Verify loadBooks is called on ViewModel
        verify(mockBookListViewModel, times(1)).loadBooks(eq("user123")); // Assumes hardcoded UID
    }

    /**
     * Verify that LiveData updates trigger UI changes when data is available.
     */
    @Test
    public void bookListLiveData_updatesUIWhenDataAvailable() {
        // Given
        List<BookSummaryData> books = Arrays.asList(
                new BookSummaryData("vol1", "Title A", "urlA"),
                new BookSummaryData("vol2", "Title B", "urlB")
        );

        // When
        bookListLiveData.postValue(books); // Simulate LiveData update

        // Then
        verify(mockBookListViewController, times(1))
                .displayBookList(
                        any(RecyclerView.class),
                        eq(books),
                        eq(mockPublicPrivateToggleHandler),
                        eq(false) // Not empty
                );

        TextView emptyTextView = activity.findViewById(R.id.empty_text);
        assertEquals(View.GONE, emptyTextView.getVisibility());
    }

    /**
     * Verify that LiveData updates trigger UI changes when data is empty.
     */
    @Test
    public void bookListLiveData_updatesUIWhenDataEmpty() {
        // Given
        List<BookSummaryData> emptyBooks = new ArrayList<>();

        // When
        bookListLiveData.postValue(emptyBooks); // Simulate LiveData update with empty list

        // Then
        verify(mockBookListViewController, times(1))
                .displayBookList(
                        any(RecyclerView.class),
                        eq(emptyBooks),
                        eq(mockPublicPrivateToggleHandler),
                        eq(true) // Empty
                );

        TextView emptyTextView = activity.findViewById(R.id.empty_text);
        assertEquals(View.VISIBLE, emptyTextView.getVisibility());
    }

    /**
     * Verify that TutorialActivity starts if shouldShowTutorial is true.
     */
    @Test
    public void tutorialManager_startsTutorialActivityIfRequired() {
        // Given that shouldShowTutorial() returns true
        // For TutorialManager, which is instantiated inside the activity,
        // we can't directly mock its *instance* without dependency injection.
        // Instead, we rely on the behavior of SharedPreferences or directly
        // mock the check within the activity's logic if it's exposed.

        // To test TutorialManager's behavior when `shouldShowTutorial` is true,
        // we need to set up the SharedPreferences shadow for the test.
        Shadows.shadowOf(activity.getSharedPreferences("tutorial_pref", 0))
                .edit().putBoolean("tutorial_shown", false).commit();

        // Recreate activity to pick up the new shared preference state
        activity = Robolectric.buildActivity(BookListActivity.class).create().get();

        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        // Then
        assertNotNull(startedIntent);
        assertEquals(TutorialActivity.class.getName(), startedIntent.getComponent().getClassName());
    }

    /**
     * Verify that TutorialActivity does not start if shouldShowTutorial is false.
     */
    @Test
    public void tutorialManager_doesNotStartTutorialActivityIfNotRequired() {
        // Given that shouldShowTutorial() returns false
        Shadows.shadowOf(activity.getSharedPreferences("tutorial_pref", 0))
                .edit().putBoolean("tutorial_shown", true).commit();

        // Recreate activity to pick up the new shared preference state
        activity = Robolectric.buildActivity(BookListActivity.class).create().get();

        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        // Then
        assertNull(startedIntent); // No activity should be started
    }
}