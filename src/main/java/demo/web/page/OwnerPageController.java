package demo.web.page;
import demo.model.*;
import demo.dto.NoteInfo;
import demo.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller @RequestMapping("/owner")
public class OwnerPageController {
    private final FeedbackService feedbacks;private final FeedbackClassificationService classifier;
    private final NoteService notes;private final PersonaService persona;private final IndexingService index;
    public OwnerPageController(FeedbackService feedbacks,FeedbackClassificationService classifier,NoteService notes,PersonaService persona,IndexingService index){
        this.feedbacks=feedbacks;this.classifier=classifier;this.notes=notes;this.persona=persona;this.index=index;
    }
    @GetMapping("/rag") public String rag(Model model){model.addAttribute("indexStatus",index.status());return "owner/rag";}
    @PostMapping("/reindex") public String reindex(RedirectAttributes flash){
        try{var s=index.reindexAll();flash.addFlashAttribute("success","Đã nạp "+s.indexedItems()+"/"+s.totalItems()+" mục vào kho tìm kiếm.");}
        catch(RuntimeException e){flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/owner/rag";
    }
    @GetMapping("/feedback") public String feedback(@RequestParam(required=false) Relevance filter,Model model){
        model.addAttribute("feedbacks",feedbacks.list(filter));model.addAttribute("currentFilter",filter);
        model.addAttribute("unclassifiedCount",feedbacks.unclassifiedCount());
        model.addAttribute("relevances",Relevance.values());model.addAttribute("topics",Topic.values());
        return "owner/feedback";
    }
    @PostMapping("/feedback/classify") public String classify(RedirectAttributes flash){
        try{var result=classifier.classifyAllUnclassified();
            flash.addFlashAttribute("success","Phân loại: "+result.labeled()+" thành công, "+result.failed()+" thất bại (tổng "+result.total()+").");}
        catch(RuntimeException e){flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/owner/feedback";
    }
    @PostMapping("/feedback/{id}/label") public String label(@PathVariable Long id,@RequestParam Relevance relevance,
            @RequestParam(required=false) Topic topic,RedirectAttributes flash){
        try{feedbacks.updateLabel(id,relevance,topic);flash.addFlashAttribute("success","Đã cập nhật nhãn góp ý #"+id+".");}
        catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/owner/feedback";
    }
    @GetMapping("/notes") public String notes(Model model,@RequestParam(required=false) Long edit){
        var all=notes.list();model.addAttribute("notes",all);
        model.addAttribute("editing",all.stream().filter(n->n.id().equals(edit)).findFirst().orElse(null));
        return "owner/notes";
    }
    @PostMapping("/notes") public String create(@RequestParam String title,@RequestParam String content,RedirectAttributes flash){
        try{notes.create(title,content);flash.addFlashAttribute("success","Đã tạo ghi chú.");}
        catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/owner/notes";
    }
    @PostMapping("/notes/{id}") public String update(@PathVariable Long id,@RequestParam String title,
                @RequestParam String content,RedirectAttributes flash){
        try{notes.update(id,title,content);flash.addFlashAttribute("success","Đã cập nhật ghi chú.");}
        catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/owner/notes";
    }
    @PostMapping("/notes/{id}/delete") public String delete(@PathVariable Long id,RedirectAttributes flash){
        try{notes.delete(id);flash.addFlashAttribute("success","Đã xóa ghi chú.");}
        catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/owner/notes";
    }
    @PostMapping("/notes/import") public String importNote(@RequestParam("file") MultipartFile file,RedirectAttributes flash){
        try{notes.importFile(file);flash.addFlashAttribute("success","Đã nhập ghi chú từ file.");}
        catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/owner/notes";
    }
    @GetMapping("/persona") public String persona(Model model){model.addAttribute("persona",persona.get());return "owner/persona";}
    @PostMapping("/persona") public String personaUpdate(@RequestParam String content,RedirectAttributes flash){
        try{persona.update(content);flash.addFlashAttribute("success","Đã lưu giọng điệu trợ lý.");}
        catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/owner/persona";
    }
}
