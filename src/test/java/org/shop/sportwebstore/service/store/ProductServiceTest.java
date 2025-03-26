package org.shop.sportwebstore.service.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shop.sportwebstore.exception.ProductException;
import org.shop.sportwebstore.model.dto.ProductDto;
import org.shop.sportwebstore.model.dto.RateProductDto;
import org.shop.sportwebstore.model.entity.Category;
import org.shop.sportwebstore.model.entity.Product;
import org.shop.sportwebstore.repository.CategoryRepository;
import org.shop.sportwebstore.repository.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private MultipartFile imageFile;

    @Mock
    private Product product;

    @InjectMocks
    private ProductService productService;

    private final String productId = "prod123";
    private final String categoryName = "category1";

    @Test
    void addProduct_ShouldAddNewProduct()  {
        String productJson = "{\"name\":\"Test Product\",\"description\":\"Test Product description\",\"price\":100.0,\"quantity\":10,\"categories\":[\"category1\"]}";

        when(categoryRepository.findByNameIn(anyList())).thenReturn(List.of(new Category(categoryName)));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(productId);
            return product;
        });

        ProductDto result = productService.addProduct(productJson, null);

        assertNotNull(result);
        assertEquals(productId, result.getId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void addProduct_ShouldThrowExceptionForInvalidJson() {
        String invalidJson = "invalid json";

        assertThrows(ProductException.class, () -> productService.addProduct(invalidJson, imageFile));
    }

    @Test
    void changeProductData_ShouldUpdateProduct() {
        ProductDto productDto = createTestProductDto();
        Product existingProduct = new Product();
        existingProduct.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        ProductDto result = productService.changeProductData(productId, productDto);

        assertNotNull(result);
        assertEquals(productId, result.getId());
        verify(productRepository).save(existingProduct);
    }

    @Test
    void changeProductData_ShouldThrowExceptionForMismatchedIds() {
        ProductDto productDto = createTestProductDto();
        productDto.setId("differentId");

        assertThrows(ProductException.class, () -> productService.changeProductData(productId, productDto));
    }

    @Test
    void getProducts_ShouldReturnPaginatedProducts() {
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "name");

        Product product = new Product();
        product.setId(productId);
        Category category = new Category(categoryName);
        product.setCategories(List.of(category));
        product.setRatings(new HashMap<>(Map.of(1, 4.0)));

        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findByNameMatchesRegexIgnoreCase(anyString(), anyBoolean(), anyInt(), anyInt(), any(Pageable.class)))
                .thenReturn(productPage);
        when(categoryRepository.findAll()).thenReturn(List.of(new Category(categoryName)));

        Map<String, Object> result = productService.getProducts(0, 10, "name", "asc", "test", 0, 9999, null, false);

        assertNotNull(result);
        assertEquals(1, ((List<?>) result.get("products")).size());
        assertTrue(result.containsKey("categories"));
    }

    @Test
    void getFeaturedProducts_ShouldReturnTopProducts() {
        product.setId(productId);

        when(productRepository.findTop9ByAvailableTrueOrderByOrdersDesc()).thenReturn(List.of(product));

        when(product.getRatings()).thenReturn(new HashMap<>(Map.of(1, 4.0)));

        Map<String, Object> result = productService.getFeaturedProducts();

        assertNotNull(result);
        assertEquals(1, ((List<?>) result.get("products")).size());
    }

    @Test
    void getDetails_ShouldReturnProductWithRelated() {
        product.setId(productId);
        product.setCategories(List.of(new Category(categoryName)));

        when(productRepository.findByIdAndAvailableTrue(productId)).thenReturn(Optional.of(product));
        when(productRepository.findTop4ByCategoriesInAndIdNotAndAvailableTrue(any(), anyString()))
                .thenReturn(List.of(new Product()));
        when(product.getRatings()).thenReturn(new HashMap<>(Map.of(1, 4.0)));

        Map<String, Object> result = productService.getDetails(productId);

        assertNotNull(result);
        assertTrue(result.containsKey("product"));
        assertTrue(result.containsKey("relatedProducts"));
    }

    @Test
    void rateProduct_ShouldUpdateProductRating() {
        Product product = new Product();
        product.setId(productId);
        product.setRatings(new HashMap<>(Map.of(1, 4.0)));

        RateProductDto rateDto = new RateProductDto(productId, 5, "order123");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.rateProduct(rateDto);

        verify(productRepository).save(product);
        verify(orderService).setOrderProductAsRated("order123", productId);
    }

    @Test
    void rateProduct_ShouldThrowForInvalidRating() {
        RateProductDto rateDto = new RateProductDto(productId, 6, "order123");

        assertThrows(ProductException.class, () -> productService.rateProduct(rateDto));
    }

    @Test
    void addCategory_ShouldCreateNewCategory() {
        when(categoryRepository.existsByName(categoryName)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(new Category(categoryName));

        Category result = productService.addCategory(categoryName);

        assertNotNull(result);
        assertEquals(categoryName, result.getName());
    }

    @Test
    void addCategory_ShouldThrowForExistingCategory() {
        when(categoryRepository.existsByName(categoryName)).thenReturn(true);

        assertThrows(ProductException.class, () -> productService.addCategory(categoryName));
    }

    @Test
    void changeProductAvailability_ShouldUpdateAvailability() {
        Product product = new Product();
        product.setId(productId);
        product.setAvailable(false);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        ProductDto result = productService.changeProductAvailability(productId, true);

        assertNotNull(result);
        assertTrue(product.isAvailable());
    }

    @Test
    void saveImage_ShouldThrowForInvalidFileType() {
        when(imageFile.getContentType()).thenReturn("text/plain");

        assertThrows(RuntimeException.class, () -> productService.saveImage(imageFile));
    }

    private ProductDto createTestProductDto() {
        ProductDto dto = new ProductDto();
        dto.setId(productId);
        dto.setName("Test Product");
        dto.setPrice(100.0);
        dto.setQuantity(10);
        dto.setCategories(List.of(categoryName));
        return dto;
    }
}