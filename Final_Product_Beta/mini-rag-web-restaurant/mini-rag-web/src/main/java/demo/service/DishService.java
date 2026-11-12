package demo.service;
import demo.model.Dish;
import demo.repository.DishRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class DishService {
    private final DishRepository dishes;
    public DishService(DishRepository dishes){this.dishes=dishes;}
    public List<Dish> listAvailable(){return dishes.findByAvailableTrueOrderByIdAsc();}
}
