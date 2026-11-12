package demo.service;
import demo.config.AiModelProvider;
import demo.model.Dish;
import demo.web.Messages;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CustomerChatServiceTest {
    @Test void menuEmptyDoesNotCallAi(){
        var dishes=mock(DishService.class);var persona=mock(PersonaService.class);
        var models=mock(AiModelProvider.class);var prompts=mock(PromptLoader.class);
        when(dishes.listAvailable()).thenReturn(List.of());
        var reply=new CustomerChatService(dishes,persona,models,prompts).reply("Món gì ngon?");
        assertEquals(Messages.MENU_EMPTY,reply.answer());assertTrue(reply.sources().isEmpty());
        verifyNoInteractions(models);
    }
    @Test void onlyAvailableDishesArePassedToAi(){
        var dishes=mock(DishService.class);var persona=mock(PersonaService.class);
        var models=mock(AiModelProvider.class);var prompts=mock(PromptLoader.class);
        when(dishes.listAvailable()).thenReturn(List.of(new Dish("Phở bò","Thơm",65000,true)));
        when(persona.get()).thenReturn("Nhẹ nhàng");when(prompts.load(anyString())).thenReturn("Chỉ dùng món có trong menu");
        when(models.chat(anyString(),eq("Món gì ngon?"))).thenReturn("Gợi ý phở bò");
        var reply=new CustomerChatService(dishes,persona,models,prompts).reply("Món gì ngon?");
        assertEquals("Gợi ý phở bò",reply.answer());assertTrue(reply.sources().isEmpty());
        var system=org.mockito.ArgumentCaptor.forClass(String.class);
        verify(models).chat(system.capture(),eq("Món gì ngon?"));
        assertTrue(system.getValue().contains("Phở bò | 65.000 đồng"));
    }
    @Test void blankAndLongMessagesAreRejected(){
        var chat=new CustomerChatService(mock(DishService.class),mock(PersonaService.class),mock(AiModelProvider.class),mock(PromptLoader.class));
        assertEquals(Messages.EMPTY_MESSAGE,assertThrows(IllegalArgumentException.class,()->chat.reply(" ")).getMessage());
        assertEquals(Messages.MESSAGE_TOO_LONG,assertThrows(IllegalArgumentException.class,()->chat.reply("a".repeat(4001))).getMessage());
    }
}
