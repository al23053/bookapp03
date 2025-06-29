import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.Target;
import com.example.bookapp03.R;
import com.example.bookapp03.data.model.BookSummaryData;
import com.example.bookapp03.ui.BookDetailActivity;
import com.example.bookapp03.ui.BookListAdapter;
import com.example.bookapp03.ui.PublicPrivateToggleHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowImageView;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowPackageManager;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class BookListAdapterTest {

    private Context context;
    private List<BookSummaryData> bookList;
    @Mock
    private PublicPrivateToggleHandler mockToggleHandler;
    @Mock
    private Glide mockGlide; // Mock Glide itself
    @Mock
    private RequestManager mockRequestManager; // Mock Glide's RequestManager
    @Mock
    private Target mockTarget; // Mock Glide's Target for into()

    private BookListAdapter adapter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        context = RuntimeEnvironment.getApplication();

        bookList = new ArrayList<>();
        bookList.add(new BookSummaryData("volume1", "Title 1", "http://example.com/cover1.jpg"));
        bookList.get(0).setPublic(true); // Set initial public status
        bookList.add(new BookSummaryData("volume2", "Title 2", "http://example.com/cover2.jpg"));
        bookList.get(1).setPublic(false);

        adapter = new BookListAdapter(bookList, mockToggleHandler);

        // Mock Glide static method. This is a bit tricky with Robolectric.
        // For testing Glide, it's often better to use a dedicated Glide test rule or inject a test dispatcher.
        // However, for basic verification that `load` and `into` are called, we can try to mock the static `with` call.
        // Note: Direct mocking of static methods like this is generally not recommended in Mockito,
        // but Robolectric provides some capabilities for it, though still complex.
        // A more robust approach might involve refactoring `BookListAdapter` to take `RequestManager` in its constructor.

        // Simulating Glide behavior by mocking its chain
        when(mockGlide.with(any(Context.class))).thenReturn(mockRequestManager);
        when(mockRequestManager.load(any(String.class))).thenReturn(mockRequestManager);
        when(mockRequestManager.into(any(ImageView.class))).thenReturn(mockTarget);
        // We are mocking Glide.with(context) to return our mockRequestManager
        // This is not straightforward with Mockito's static mocking.
        // For now, let's assume Glide.with(context) actually returns a RequestManager
        // and focus on verifying calls on that RequestManager.
        // A common pattern is to make Glide a dependency that can be injected, rather than a static call.
    }

    /**
     * Verifies that onCreateViewHolder inflates the correct layout and returns a valid ViewHolder.
     */
    @Test
    public void onCreateViewHolder_returnsCorrectViewHolder() {
        // Given
        ViewGroup parent = new RecyclerView(context);
        BookListAdapter.BookViewHolder viewHolder = adapter.onCreateViewHolder(parent, 0);

        // Then
        assertNotNull(viewHolder);
        assertNotNull(viewHolder.itemView);
        assertEquals(R.layout.item_book_summary, Shadows.shadowOf(viewHolder.itemView).getLayoutId());
        assertNotNull(viewHolder.titleView);
        assertNotNull(viewHolder.coverView);
        assertNotNull(viewHolder.publicSwitch);
    }

    /**
     * Verifies that onBindViewHolder correctly binds data to the UI elements.
     */
    @Test
    public void onBindViewHolder_bindsDataCorrectly() {
        // Given
        BookSummaryData book = bookList.get(0);
        BookListAdapter.BookViewHolder holder = createMockViewHolder();

        // When
        adapter.onBindViewHolder(holder, 0);

        // Then
        assertEquals(book.getTitle(), holder.titleView.getText().toString());
        assertEquals(book.isPublic(), holder.publicSwitch.isChecked());

        // Verify Glide interaction (simplified, as static mocking is hard)
        // We cannot directly verify static Glide.with(context).
        // Instead, we verify that the ImageView received a request.
        ShadowImageView shadowImageView = Shadows.shadowOf(holder.coverView);
        assertNotNull(shadowImageView.getImageURI());
        assertEquals(book.getImageUrl(), shadowImageView.getImageURI().toString());
    }

    /**
     * Verifies that the switch toggle correctly calls the handler.
     */
    @Test
    public void onBindViewHolder_switchToggleCallsHandler() {
        // Given
        BookSummaryData book = bookList.get(0); // Public book
        BookListAdapter.BookViewHolder holder = createMockViewHolder();
        adapter.onBindViewHolder(holder, 0);

        // When: Simulate switch click to change state to false (private)
        holder.publicSwitch.setChecked(false);
        // Manually trigger the listener as Robolectric's click simulation might not cover OnCheckedChangeListener directly
        // The listener is usually set on the actual view, so we need to get the listener.
        // However, it's easier to simulate the direct listener call:
        holder.publicSwitch.getOnCheckedChangeListener().onCheckedChanged(holder.publicSwitch, false);
        ShadowLooper.idleMainLooper(); // Allow any async operations to complete

        // Then
        verify(mockToggleHandler, times(1)).handleToggle(
                eq("current_user_id"), // Hardcoded in adapter for now
                eq(book.getVolumeId()),
                eq(false) // New state
        );

        // When: Simulate switch click to change state to true (public) for the second book
        BookSummaryData book2 = bookList.get(1); // Private book
        BookListAdapter.BookViewHolder holder2 = createMockViewHolder();
        adapter.onBindViewHolder(holder2, 1);

        holder2.publicSwitch.setChecked(true);
        holder2.publicSwitch.getOnCheckedChangeListener().onCheckedChanged(holder2.publicSwitch, true);
        ShadowLooper.idleMainLooper();

        verify(mockToggleHandler, times(1)).handleToggle(
                eq("current_user_id"),
                eq(book2.getVolumeId()),
                eq(true)
        );
    }

    /**
     * Verifies that tapping the cover image starts the BookDetailActivity with the correct volumeId.
     */
    @Test
    public void onBindViewHolder_coverTapStartsDetailActivity() {
        // Given
        BookSummaryData book = bookList.get(0);
        BookListAdapter.BookViewHolder holder = createMockViewHolder();
        adapter.onBindViewHolder(holder, 0);

        // When
        holder.coverView.performClick(); // Simulate click on ImageView
        ShadowLooper.idleMainLooper();

        // Then
        Intent startedIntent = Shadows.shadowOf(RuntimeEnvironment.getApplication()).getNextStartedActivity();
        assertNotNull(startedIntent);
        assertEquals(BookDetailActivity.class.getName(), startedIntent.getComponent().getClassName());
        assertEquals(book.getVolumeId(), startedIntent.getStringExtra("volumeId"));
    }

    /**
     * Verifies that getItemCount returns the correct number of items.
     */
    @Test
    public void getItemCount_returnsCorrectSize() {
        assertEquals(bookList.size(), adapter.getItemCount());

        // Add more books and re-check
        bookList.add(new BookSummaryData("volume3", "Title 3", "url3"));
        adapter = new BookListAdapter(bookList, mockToggleHandler); // Re-create adapter with updated list
        assertEquals(bookList.size(), adapter.getItemCount());
    }


    // Helper method to create a mock ViewHolder with mock views
    private BookListAdapter.BookViewHolder createMockViewHolder() {
        // LayoutInflater to inflate the actual view for the ViewHolder
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_book_summary, new RecyclerView(context), false);

        // Find the actual views within the inflated layout
        TextView titleView = itemView.findViewById(R.id.book_title);
        ImageView coverView = itemView.findViewById(R.id.book_cover);
        Switch publicSwitch = itemView.findViewById(R.id.public_switch); // Make sure this ID exists in your layout

        // Create a real ViewHolder instance with the inflated views
        BookListAdapter.BookViewHolder holder = new BookListAdapter.BookViewHolder(itemView);

        // Optionally, you can mock the internal views if you need more control,
        // but for basic text/checked state verification, direct access might be fine.
        // For Glide, we'll need to mock the ImageView's interaction via Robolectric shadows or a custom mock.

        return holder;
    }
}