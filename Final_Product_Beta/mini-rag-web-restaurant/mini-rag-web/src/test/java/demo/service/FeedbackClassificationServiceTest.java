package demo.service;
import demo.model.Relevance;
import demo.model.Topic;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FeedbackClassificationServiceTest {
    @Test void parsesValidLabel(){
        var parsed=FeedbackClassificationService.parseLabel("```json\n{\"relevant\": true, \"topic\": \" waiting_time \"}\n```");
        assertTrue(parsed.isPresent());
        assertEquals(Relevance.RELEVANT,parsed.get().relevance());
        assertEquals(Topic.WAITING_TIME,parsed.get().topic());
    }
    @Test void rejectsInvalidJsonAndTypes(){
        assertTrue(FeedbackClassificationService.parseLabel("hello").isEmpty());
        assertTrue(FeedbackClassificationService.parseLabel("{\"relevant\": \"yes\", \"topic\": \"FOOD\"}").isEmpty());
        assertTrue(FeedbackClassificationService.parseLabel("{\"relevant\": false, \"topic\": \"UNKNOWN\"}").isEmpty());
        assertTrue(FeedbackClassificationService.parseLabel(null).isEmpty());
    }
    @Test void irrelevantIsNotAnInstruction(){
        var parsed=FeedbackClassificationService.parseLabel("{\"relevant\":false,\"topic\":\"OTHER\"}");
        assertEquals(Relevance.IRRELEVANT,parsed.orElseThrow().relevance());
    }
}
