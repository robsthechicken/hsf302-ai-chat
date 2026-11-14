package demo.web.api;
import demo.dto.ChatReply;
import demo.dto.SourceInfo;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
/** Lịch sử chat lưu trong HttpSession: reload/chuyển trang vẫn còn, logout (invalidate session) thì mất. */
public final class ChatHistory {
    public static final String SESSION_KEY="chatHistory";
    private static final int MAX_ENTRIES=50; // giới hạn RAM cho mỗi session
    public record Entry(String role,String text,List<SourceInfo> sources) {}
    private ChatHistory(){}
    @SuppressWarnings("unchecked")
    public static synchronized List<Entry> get(HttpSession session){
        var list=(List<Entry>)session.getAttribute(SESSION_KEY);
        return list==null?List.of():List.copyOf(list);
    }
    @SuppressWarnings("unchecked")
    public static synchronized void add(HttpSession session,String question,ChatReply reply){
        var list=(List<Entry>)session.getAttribute(SESSION_KEY);
        if(list==null)list=new ArrayList<>();
        list.add(new Entry("human",question.trim(),List.of()));
        list.add(new Entry("bot",reply.answer(),reply.sources()));
        while(list.size()>MAX_ENTRIES)list.remove(0);
        session.setAttribute(SESSION_KEY,list); // set lại để session store biết attribute đã đổi
    }
}
