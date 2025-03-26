package org.shop.sportwebstore.service.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.exception.ProductException;
import org.shop.sportwebstore.model.dto.ProductDto;
import org.shop.sportwebstore.model.dto.RateProductDto;
import org.shop.sportwebstore.model.entity.Category;
import org.shop.sportwebstore.model.entity.Product;
import org.shop.sportwebstore.repository.CategoryRepository;
import org.shop.sportwebstore.repository.ProductRepository;
import org.shop.sportwebstore.service.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderService orderService;

    @Transactional
    public ProductDto addProduct(String productJson, MultipartFile image) {
        ProductDto product;
        try {
            product = new ObjectMapper().readValue(productJson, ProductDto.class);
        } catch (JsonProcessingException e) {
            throw new ProductException("Error parsing product data: " + e.getMessage());
        }

        ValidationUtil.validProductData(product);
        ValidationUtil.validRestProduct(product);

        product.setImage(saveImage(image));

        List<Category> categories = categoryRepository.findByNameIn(product.getCategories());

        return ProductDto.minDto(productRepository.save(ProductDto.toEntity(product, categories)));
    }

    protected String saveImage(MultipartFile image) {
        if (image == null || image.isEmpty()) return null;

        if (!"image/png".equals(image.getContentType())) {
            throw new RuntimeException("Allowed files: PNG!");
        }

        try {
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path uploadDir = Paths.get("external-images");

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(fileName);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "external-images/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        }
    }

    public ProductDto changeProductData(String id, ProductDto productDto) {
        if (!productDto.getId().equals(id)) {
            throw new ProductException("Product id must be the same.");
        }
        ValidationUtil.validProductData(productDto);
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductException("Product not found."));
        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());
        product.setAmountLeft(productDto.getQuantity());
        return ProductDto.minEdited(productRepository.save(product));
    }

    public Map<String, Object> getProducts(int page, int size, String sort, String direction,
                                           String search, int minPrice, int maxPrice, List<String> categories, boolean isAdmin) {
        Page<Product> products = fetchProducts(page, size, sort, direction, search, minPrice, maxPrice, categories, isAdmin);
        Map<String, Object> response = new java.util.HashMap<>(Map.of(
                "products", products.getContent().stream().map(product -> ProductDto.toDto(product, false)).toList(),
                "totalElements", products.getTotalElements()
        ));
        if (page == 0) {
            response.put("categories", getCategories());
        }
        return response;
    }

    private Page<Product> fetchProducts(int page, int size, String sort, String direction, String search, int minPrice, int maxPrice, List<String> categories, boolean isAdmin) {
        Sort.Direction direct = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortObj = Sort.by(direct, sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<Product> products;
        if (categories == null || categories.isEmpty()) {
            products = productRepository.findByNameMatchesRegexIgnoreCase(".*" + search + ".*", !isAdmin, minPrice, maxPrice, pageable);
        } else {
            products = productRepository.findByNameMatchesRegexIgnoreCaseAndCategoriesIn(".*" + search + ".*", categories, !isAdmin, minPrice, maxPrice, pageable);
        }
        return products;
    }

    public List<String> getCategories() {
        return categoryRepository.findAll().stream().map(Category::getName).toList();
    }

    public Map<String, Object> getFeaturedProducts() {
        List<Product> products = productRepository.findTop9ByAvailableTrueOrderByOrdersDesc();
        return Map.of("products", products.stream().map(product -> ProductDto.toDto(product, false)).toList());
    }

    public Map<String, Object> getDetails(String id) {
        Product product = productRepository.findByIdAndAvailableTrue(id).orElseThrow(() -> new ProductException("Product not found."));
        Collection<List<Category>> categories = Collections.singleton(product.getCategories());
        List<Product> relatedProducts = productRepository.findTop4ByCategoriesInAndIdNotAndAvailableTrue(categories, id);
        return Map.of(
                "product", ProductDto.toDto(product, true),
                "relatedProducts", relatedProducts.stream().map(ProductDto::minDto).toList());
    }

    @Transactional(rollbackFor = ProductException.class)
    public void rateProduct(RateProductDto rateProductDto) {
        Product product = productRepository.findById(rateProductDto.getProductId()).orElseThrow(() -> new ProductException("Product not found."));
        if (rateProductDto.getRating() < 1 || rateProductDto.getRating() > 5) {
            throw new ProductException("Rating must be between 1 and 5.");
        }
        int key = 0;
        double value = 0;
        for (Map.Entry<Integer, Double> entry : product.getRatings().entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
        }

        double finalRating = ((value * key) + (double) rateProductDto.getRating()) / (key + 1);
        finalRating = Math.round(finalRating * 100.0) / 100.0;

        product.getRatings().put(key + 1, finalRating);
        product.getRatings().remove(key);

        orderService.setOrderProductAsRated(rateProductDto.getOrderId(), rateProductDto.getProductId());
        productRepository.save(product);
    }

    public Category addCategory(String category) {
        if (categoryRepository.existsByName(category)) {
            throw new ProductException("Category already exists.");
        }
        return categoryRepository.save(new Category(category));
    }

    public ProductDto changeProductAvailability(String id, boolean available) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductException("Product not found."));
        product.setAvailable(available);
        return ProductDto.minEdited(productRepository.save(product));
    }

    public double getMaxPrice() {
        return productRepository.findTopByAvailableTrueAndAmountLeftGreaterThanOrderByPriceDesc(0).getPrice();
    }
}
