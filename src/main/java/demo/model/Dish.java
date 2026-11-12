package demo.model;
import jakarta.persistence.*;
@Entity @Table(name="dish")
public class Dish {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=100) private String name;
    @Column(length=500) private String description;
    @Column(nullable=false) private int priceVnd;
    @Column(nullable=false) private boolean available=true;
    /** Đường dẫn ảnh, ví dụ /images/dishes/pho-bo-tai.jpg. null -> giao diện dùng ảnh mặc định. */
    @Column(length=255) private String imageUrl;
    protected Dish() {}
    public Dish(String name, String description, int priceVnd, boolean available){this(name,description,priceVnd,available,null);}
    public Dish(String name, String description, int priceVnd, boolean available, String imageUrl){
        this.name=name;this.description=description;this.priceVnd=priceVnd;this.available=available;this.imageUrl=imageUrl;
    }
    public Long getId(){return id;} public String getName(){return name;}
    public String getDescription(){return description;} public int getPriceVnd(){return priceVnd;}
    public boolean isAvailable(){return available;} public String getImageUrl(){return imageUrl;}
    public void setImageUrl(String imageUrl){this.imageUrl=imageUrl;}
}
