package demo.service;
import demo.model.Dish;
import demo.repository.DishRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
/** DB riêng: context này dùng create-drop, không được xoá bảng của các test khác. */
@SpringBootTest(properties="spring.datasource.url=jdbc:h2:mem:seedertest;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class DataSeederTest {
    @Autowired private DataSeeder seeder;
    @Autowired private DishRepository dishes;

    private Dish byName(String name){
        return dishes.findAll().stream().filter(d->d.getName().equals(name)).findFirst().orElseThrow();
    }

    @Test void restartOnOldDbFillsMissingDishImagesOnly() throws Exception {
        // Giả lập DB cũ: món chưa có ảnh, món đã có ảnh riêng, món không có trong seed
        Dish pho=byName("Phở bò tái"); pho.setImageUrl(null); dishes.save(pho);
        Dish bunCha=byName("Bún chả"); bunCha.setImageUrl("/images/anh-rieng.jpg"); dishes.save(bunCha);
        Long extraId=dishes.save(new Dish("Món mới của quán","",10000,true)).getId();

        seeder.run(null); // DB đã có user -> chỉ bổ sung ảnh

        assertEquals("/images/dishes/pho-bo-tai.jpg",byName("Phở bò tái").getImageUrl());
        assertEquals("/images/anh-rieng.jpg",byName("Bún chả").getImageUrl(),"không ghi đè ảnh đã có");
        assertNull(dishes.findById(extraId).orElseThrow().getImageUrl(),"món ngoài seed giữ nguyên");
    }
}
