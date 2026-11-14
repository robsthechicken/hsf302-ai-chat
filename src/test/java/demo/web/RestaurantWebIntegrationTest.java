package demo.web;
import demo.service.PersonaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class RestaurantWebIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private PersonaService personas;
    @Test void unauthenticatedApisReturn401AndPageRedirects() throws Exception {
        mvc.perform(post("/owner/api/chat").contentType("application/json").content("{\"message\":\"hi\"}"))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error").exists());
        mvc.perform(get("/customer/chat")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }
    @Test void loginClearsPreviousChatHistory() throws Exception {
        var session=new MockHttpSession();
        session.setAttribute(demo.web.api.ChatHistory.SESSION_KEY,new java.util.ArrayList<>(java.util.List.of(
            new demo.web.api.ChatHistory.Entry("human","câu của user trước",java.util.List.of()))));
        mvc.perform(post("/login").session(session).param("username","customer1").param("password","customer123"))
            .andExpect(status().is3xxRedirection());
        mvc.perform(get("/customer/api/chat").session(session)).andExpect(status().isOk()).andExpect(content().json("[]"));
    }
    @Test void ownerCanAccessOwnerPagesButNotCustomer() throws Exception {
        MvcResult login=mvc.perform(post("/login").param("username","owner").param("password","owner123"))
            .andExpect(status().is3xxRedirection()).andReturn();
        var session=(MockHttpSession)login.getRequest().getSession(false);
        mvc.perform(get("/owner/feedback").session(session)).andExpect(status().isOk())
            .andExpect(view().name("owner/feedback"));
        mvc.perform(get("/customer/chat").session(session)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
        mvc.perform(post("/customer/api/chat").session(session).contentType("application/json").content("{\"message\":\"hi\"}"))
            .andExpect(status().isForbidden());
    }
    @Test void customerCanSubmitFeedbackWithoutAiKeys() throws Exception {
        MvcResult login=mvc.perform(post("/login").param("username","customer1").param("password","customer123"))
            .andExpect(status().is3xxRedirection()).andReturn();
        var session=(MockHttpSession)login.getRequest().getSession(false);
        mvc.perform(get("/customer/chat").session(session)).andExpect(status().isOk()).andExpect(view().name("customer/chat"));
        mvc.perform(post("/customer/feedback").session(session).param("content","Món ăn ngon nhưng hơi lâu"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/customer/chat"));
    }
    // ";x=1" là matrix parameter: routing của Spring bỏ qua nó, nên kiểm quyền cũng không được bị nó đánh lừa
    @Test void matrixParamDoesNotBypassRoleCheck() throws Exception {
        MvcResult login=mvc.perform(post("/login").param("username","customer1").param("password","customer123")).andReturn();
        var customer=(MockHttpSession)login.getRequest().getSession(false);
        String before=personas.get();
        mvc.perform(get("/owner;x=1/feedback").session(customer)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
        mvc.perform(post("/owner;x=1/persona").session(customer).param("content","HACKED"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
        assertEquals(before,personas.get());
        mvc.perform(post("/owner/api;x=1/chat").session(customer).contentType("application/json").content("{\"message\":\"hi\"}"))
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.error").exists());
        login=mvc.perform(post("/login").param("username","owner").param("password","owner123")).andReturn();
        var owner=(MockHttpSession)login.getRequest().getSession(false);
        mvc.perform(get("/customer;x=1/chat").session(owner)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
    }
    @Test void emptyApiMessageHasJsonError() throws Exception {
        MvcResult login=mvc.perform(post("/login").param("username","customer1").param("password","customer123")).andReturn();
        var session=(MockHttpSession)login.getRequest().getSession(false);
        mvc.perform(post("/customer/api/chat").session(session).contentType("application/json").content("{\"message\":\"\"}"))
          .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").exists());
    }
}
