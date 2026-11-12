package demo.service;
import demo.config.AiModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
@Component @Order(2)
public class RagStartupLoader implements ApplicationRunner {
    private static final Logger log=LoggerFactory.getLogger(RagStartupLoader.class);
    private final AiModelProvider models;private final IndexingService indexing;
    public RagStartupLoader(AiModelProvider models,IndexingService indexing){this.models=models;this.indexing=indexing;}
    @Override public void run(ApplicationArguments args){
        if(!models.hasEmbeddingKey()){log.warn("Chưa có API key embedding nên bỏ qua việc nạp kho tìm kiếm.");return;}
        try{var status=indexing.reindexAll();log.info("Nạp kho tìm kiếm: {}/{}",status.indexedItems(),status.totalItems());}
        catch(RuntimeException e){log.warn("Không thể nạp kho tìm kiếm: {}",e.getMessage());}
    }
}
