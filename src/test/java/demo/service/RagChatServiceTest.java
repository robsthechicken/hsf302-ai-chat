package demo.service;
import demo.config.AiModelProvider;
import demo.model.*;
import demo.repository.FeedbackRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
class RagChatServiceTest {
    @Test void includesRecentFeedbackWhenSemanticSearchFindsNothing(){
        var rag=mock(RagService.class);var repo=mock(FeedbackRepository.class);
        var prompts=mock(PromptLoader.class);var models=mock(AiModelProvider.class);
        var f=new Feedback(new AppUser("customer1","hash",Role.CUSTOMER),"Phở quá mặn");
        ReflectionTestUtils.setField(f,"id",17L);
        f.setCreatedAt(LocalDateTime.of(2026,11,14,13,40));
        when(rag.isEmpty()).thenReturn(false);
        when(rag.search(any())).thenReturn(List.of());
        when(repo.findTop5ByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(f));
        when(prompts.load(PromptLoader.RAG)).thenReturn("<context>{context}</context>");
        when(models.chat(anyString(),anyString())).thenReturn("Góp ý #17: phở quá mặn");
        var reply=new RagChatService(rag,repo,prompts,models).ask("Góp ý mới nhất là gì?");
        assertEquals("Góp ý #17: phở quá mặn",reply.answer());
        verify(models).chat(contains("[Góp ý #17 | UNCLASSIFIED | gửi lúc 2026-11-14T13:40] Phở quá mặn"),anyString());
    }
}
