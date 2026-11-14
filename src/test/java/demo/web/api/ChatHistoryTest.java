package demo.web.api;
import demo.dto.ChatReply;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class ChatHistoryTest {
    @Test void storesQuestionAndAnswerInOrderAndCapsSize(){
        var session=new MockHttpSession();
        assertTrue(ChatHistory.get(session).isEmpty());
        ChatHistory.add(session," hỏi 1 ",new ChatReply("đáp 1",List.of()));
        var entries=ChatHistory.get(session);
        assertEquals(List.of("human","bot"),entries.stream().map(ChatHistory.Entry::role).toList());
        assertEquals("hỏi 1",entries.get(0).text());
        for(int i=2;i<=40;i++)ChatHistory.add(session,"hỏi "+i,new ChatReply("đáp "+i,List.of()));
        entries=ChatHistory.get(session);
        assertEquals(50,entries.size());
        assertEquals("hỏi 16",entries.get(0).text()); // các lượt cũ nhất bị bỏ, vẫn bắt đầu bằng câu hỏi
    }
}
