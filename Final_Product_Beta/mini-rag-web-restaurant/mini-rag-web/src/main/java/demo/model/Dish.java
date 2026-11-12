package demo.model;
import jakarta.persistence.*;
@Entity @Table(name="dish")
public class Dish {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=100) private String name;
    @Column(length=500) private String description;
    @Column(nullable=false) private int priceVnd;
    @Column(nullable=false) private boolean available=true;
    protected Dish() {}
    public Dish(String name, String description, int priceVnd, boolean available){this.name=name;this.description=description;this.priceVnd=priceVnd;this.available=available;}
    public Long getId(){return id;} public String getName(){return name;}
    public String getDescription(){return description;} public int getPriceVnd(){return priceVnd;}
    public boolean isAvailable(){return available;}
}
