package demo.web;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Chạy Tomcat thật: MockMvc không gắn ;jsessionid vào URL và không áp giới hạn upload, nên không bắt được 2 lỗi này.
 * DB riêng để không xoá dữ liệu của context test MockMvc (cùng dùng create-drop).
 */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties="spring.datasource.url=jdbc:h2:mem:realserver;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class RealServerTest {
    @LocalServerPort int port;
    // mặc định không tự đi theo redirect -> đọc được status 302 và header Location
    private final HttpClient client=HttpClient.newHttpClient();

    private HttpResponse<Void> login(String username,String password) throws Exception {
        var request=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/login"))
                .header("Content-Type","application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("username="+username+"&password="+password)).build();
        return client.send(request,HttpResponse.BodyHandlers.discarding());
    }

    @Test void firstLoginRedirectHasNoSessionIdInUrl() throws Exception {
        var response=login("owner","owner123");
        assertEquals(302,response.statusCode());
        assertFalse(response.headers().firstValue("Location").orElse("").contains("jsessionid"));
    }

    @Test void tooLargeUploadRedirectsWithMessage() throws Exception {
        String cookie=login("owner","owner123").headers().firstValue("Set-Cookie").orElseThrow().split(";")[0];
        String boundary="----pwtest";
        String body="--"+boundary+"\r\nContent-Disposition: form-data; name=\"file\"; filename=\"lon.txt\"\r\n"
                +"Content-Type: text/plain\r\n\r\n"+"a".repeat(6*1024*1024)+"\r\n--"+boundary+"--\r\n";
        var request=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/owner/notes/import"))
                .header("Content-Type","multipart/form-data; boundary="+boundary).header("Cookie",cookie)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        var response=client.send(request,HttpResponse.BodyHandlers.discarding());
        assertEquals(302,response.statusCode());
        assertTrue(response.headers().firstValue("Location").orElse("").endsWith("/owner/notes"));
    }
}
