package demo.web;
import demo.model.*;
import org.springframework.stereotype.Component;
@Component("displayNames")
public class DisplayNames {
    public String relevance(Relevance relevance){if(relevance==null)return "";return switch(relevance){
        case UNCLASSIFIED->"Chưa phân loại";case RELEVANT->"Liên quan";case IRRELEVANT->"Không liên quan";};}
    public String topic(Topic topic){if(topic==null)return "";return switch(topic){
        case FOOD->"Món ăn";case SERVICE->"Phục vụ";case PRICE->"Giá";
        case HYGIENE->"Vệ sinh";case WAITING_TIME->"Thời gian chờ";case OTHER->"Khác";};}
    public String labelSource(LabelSource source){if(source==null)return "";return switch(source){case AI->"AI";case OWNER->"Chủ quán";};}
    public String origin(NoteOrigin origin){if(origin==null)return "";return switch(origin){case WRITTEN->"Soạn trong app";case IMPORTED->"Nhập từ file";};}
}
