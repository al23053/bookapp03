package com.example.bookapp03.ui;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.example.bookapp03.C1UIProcessing.ViewModelFactory;
import com.example.bookapp03.C3BookInformationProcessing.BookDetailViewModel;
import com.example.bookapp03.C3BookInformationProcessing.BookListViewModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class ViewModelFactoryTest {

    private Context applicationContext;
    private ViewModelFactory viewModelFactory;

    @Before
    public void setUp() {
        applicationContext = RuntimeEnvironment.getApplication();
        viewModelFactory = new ViewModelFactory(applicationContext);
    }

    /**
     * BookListViewModelを要求した場合に、正しいインスタンスが返されることを確認する
     */
    @Test
    public void create_returnsBookListViewModel() {
        ViewModel viewModel = viewModelFactory.create(BookListViewModel.class);

        assertNotNull(viewModel);
        assertTrue(viewModel instanceof BookListViewModel);
    }

    /**
     * BookDetailViewModelを要求した場合に、正しいインスタンスが返されることを確認する
     */
    @Test
    public void create_returnsBookDetailViewModel() {
        ViewModel viewModel = viewModelFactory.create(BookDetailViewModel.class);

        assertNotNull(viewModel);
        assertTrue(viewModel instanceof BookDetailViewModel);
    }

    /**
     * 未知のViewModelクラスを要求した場合にIllegalArgumentExceptionがスローされることを確認する
     */
    @Test(expected = IllegalArgumentException.class)
    public void create_throwsExceptionForUnknownViewModel() {
        // 存在しない（またはViewModelを継承していない）ダミークラスを作成
        class UnknownViewModel extends ViewModel {}

        viewModelFactory.create(UnknownViewModel.class);
    }

    // BookRepositoryImplが正しく注入されていることのテストは、
    // ViewModelFactory内でBookRepositoryImplが直接newされているため、
    // Mockitoのspyなどを使わないと直接的に検証するのは難しいです。
    // このテストでは、ViewModelインスタンスが正しく作成されたことを確認することで、
    // 間接的に依存関係の注入も行われていると見なします。
    // より厳密なテストには、BookRepositoryImplもモック化し、ViewModelFactoryに注入できるように
    // Factoryの設計自体を変更する必要があります（例: ファクトリのコンストラクタでBookRepositoryを受け取る）。
}