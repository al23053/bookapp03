package com.example.bookapp03.C6BookInformationManaging;

import static org.junit.Assert.assertEquals;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * VolumeIdProvider の単体テスト
 * - ブラックボックス: 成功・アイテムなし・例外時で返値を確認
 * - ホワイトボックス: JSON パースロジックの境界を検証
 */
public class VolumeIdProviderTest {
    private MockWebServer server;

    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        // BASE_URL をモックサーバーに向けるためテストユーティリティで差し替え
        VolumeIdProviderTestUtil.overrideBaseUrl(
            server.url("/volumes?q=isbn:").toString()
        );
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void testFetchVolumeId_successfulResponse() {
        // JSON テキストブロックの代わりに通常の文字列リテラルを使用
        String json = "{\"items\":[{\"id\":\"VOL123\"}]}";
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(json)
        );

        String id = VolumeIdProvider.fetchVolumeId("dummy");
        assertEquals("VOL123", id);
    }

    @Test
    public void testFetchVolumeId_noItems_returnsEmpty() {
        String json = "{\"items\":[]}";
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(json)
        );

        String id = VolumeIdProvider.fetchVolumeId("dummy");
        assertEquals("", id);
    }

    @Test
    public void testFetchVolumeId_badResponse_returnsEmpty() {
        server.enqueue(new MockResponse()
            .setResponseCode(500)
        );

        String id = VolumeIdProvider.fetchVolumeId("dummy");
        assertEquals("", id);
    }
}