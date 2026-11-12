package demo.service;
import demo.web.Messages;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
@Component
public class DocumentTextReader {
    public String read(MultipartFile file){
        if(file==null||file.isEmpty())throw new IllegalArgumentException(Messages.FILE_EMPTY);
        String name=cleanName(file.getOriginalFilename());
        String lower=name.toLowerCase(Locale.ROOT);
        if(!lower.endsWith(".txt")&&!lower.endsWith(".md")&&!lower.endsWith(".pdf"))
            throw new IllegalArgumentException(String.format(Messages.FILE_UNSUPPORTED,name));
        try{
            String text;
            if(lower.endsWith(".pdf")){
                try(var pdf=Loader.loadPDF(file.getBytes())){text=new PDFTextStripper().getText(pdf);}
            }else text=new String(file.getBytes(),StandardCharsets.UTF_8);
            if(text.isBlank())throw new IllegalArgumentException(String.format(Messages.FILE_NO_TEXT,name));
            return text;
        }catch(IOException e){throw new IllegalArgumentException(String.format(Messages.FILE_UNREADABLE,name),e);}
    }
    public static String cleanName(String original){
        if(original==null||original.isBlank())return "tai-lieu.txt";
        String normalized=original.replace('\\','/');
        String result=normalized.substring(normalized.lastIndexOf('/')+1).trim();
        return result.isEmpty()?"tai-lieu.txt":result;
    }
}
