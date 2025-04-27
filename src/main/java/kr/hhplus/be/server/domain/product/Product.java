package kr.hhplus.be.server.domain.product;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.vo.Money;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    private static final int MAX_DESCRIPTION_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private LocalDate releaseDate;

    private String imageUrl;

    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Product create(String name, String brand, Money price,
                                 LocalDate releaseDate, String imageUrl, String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ProductException.DescriptionTooLongException(MAX_DESCRIPTION_LENGTH);
        }

        LocalDateTime now = LocalDateTime.now();
        Product product = new Product();
        product.name = name;
        product.brand = brand;
        product.price = price.value();
        product.releaseDate = releaseDate;
        product.imageUrl = imageUrl;
        product.description = description;
        product.createdAt = now;
        product.updatedAt = now;
        return product;
    }

    public void updateDescription(String newDescription) {
        if (newDescription != null && newDescription.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ProductException.DescriptionTooLongException(MAX_DESCRIPTION_LENGTH);
        }
        this.description = newDescription;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isReleased() {
        return !releaseDate.isAfter(LocalDate.now());
    }

    public void validateOrderable(int totalStock) {
        if (!isReleased()) {
            throw new ProductException.NotReleasedException(this.id);
        }
        if (totalStock <= 0) {
            throw new ProductException.OutOfStockException(this.id);
        }
    }

    public boolean isSameBrand(String brandName) {
        return this.brand.equalsIgnoreCase(brandName);
    }
}
