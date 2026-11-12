package demo.service;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import static org.junit.jupiter.api.Assertions.*;
class DocumentTextReaderTest {
    private final DocumentTextReader reader=new DocumentTextReader();
    @Test void readsUtf8Markdown(){
        var f=new MockMultipartFile("file","ghi-chu.md","text/markdown","Quy trình chờ món".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertEquals("Quy trình chờ món",reader.read(f));
    }
    @Test void rejectsUnsupportedAndEmpty(){
        assertThrows(IllegalArgumentException.class,()->reader.read(new MockMultipartFile("file","a.png","image/png",new byte[]{1})));
        assertThrows(IllegalArgumentException.class,()->reader.read(new MockMultipartFile("file","a.txt","text/plain",new byte[0])));
    }
    @Test void stripsWindowsAndUnixPaths(){
        assertEquals("policy.txt",DocumentTextReader.cleanName("C:\\temp\\policy.txt"));
        assertEquals("file.md",DocumentTextReader.cleanName("/tmp/file.md"));
    }
}
