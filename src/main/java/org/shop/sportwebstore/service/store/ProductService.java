package org.shop.sportwebstore.service.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.shop.sportwebstore.exception.ProductException;
import org.shop.sportwebstore.model.dto.*;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            Files.copy(image.getInputStream(), filePath);

            return "external-images/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        }
    }

    public ProductDto changeProductData(String id, ProductDto productDto) {
        if (!productDto.getId().equals(id)) {
            throw new ProductException("Product id must be the same.");
        }
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductException("Product not found."));
        if (productDto.getPrice() != null && productDto.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            product.setPrice(productDto.getPrice().setScale(2, RoundingMode.HALF_UP));
        }

        if (productDto.getName() != null && !productDto.getName().isEmpty()) {
            product.setName(productDto.getName());
        }

        if (productDto.getQuantity() >= 0) {
            product.setAmountLeft(productDto.getQuantity());
        }
        return ProductDto.minEdited(productRepository.save(product));
    }

    public ProductsListInfo getProducts(int page, int size, String sort, String direction,
                                           String search, int minPrice, int maxPrice, List<String> categories, boolean isAdmin) {
        Page<Product> products = fetchProducts(page, size, sort, direction, search, minPrice, maxPrice, categories, isAdmin);
        ProductsListInfo response = new ProductsListInfo(
                products.getContent().stream().map(product -> ProductDto.toDto(product, false)).toList(),
                products.getTotalElements()
        );
        if (page == 0) {
            response.setCategories(getCategories().stream().map(CategoryDto::getName).toList());
        }
        return response;
    }

    private Page<Product> fetchProducts(int page, int size, String sort, String direction, String search, int minPrice, int maxPrice, List<String> categories, boolean isAdmin) {
        Sort.Direction direct = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortObj = Sort.by(direct, sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        if (categories == null || categories.isEmpty()) {
            return productRepository.findByNameMatchesRegexIgnoreCase(".*" + search + ".*", !isAdmin, minPrice, maxPrice, pageable);
        }
            return productRepository.findByNameMatchesRegexIgnoreCaseAndCategoriesIn(".*" + search + ".*", categories, !isAdmin, minPrice, maxPrice, pageable);
    }

    public List<CategoryDto> getCategories() {
        return categoryRepository.findAll().stream().map(CategoryDto::toDto).toList();
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
        Product product = productRepository.findById(rateProductDto.getProductId())
                .orElseThrow(() -> new ProductException("Product not found."));

        Map<Integer, Double> ratings = product.getRatings();
        int totalRatings = ratings.isEmpty() ? 0 : Collections.max(ratings.keySet());
        double currentAvg = ratings.getOrDefault(totalRatings, 0.0);

        double finalRating = ((totalRatings * currentAvg) + (double) rateProductDto.getRating()) / (totalRatings + 1);
        finalRating = Math.round(finalRating * 100.0) / 100.0;

        double finalRating1 = finalRating;
        product.getRatings().putIfAbsent(totalRatings + 1, finalRating1);
        product.getRatings().remove(totalRatings);

        orderService.setOrderProductAsRated(rateProductDto.getOrderId(), rateProductDto.getProductId());
        productRepository.save(product);
    }

    public CategoryDto addCategory(CategoryDto category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new ProductException("Category already exists.");
        }
        return new CategoryDto(categoryRepository.save(new Category(category.getName())).getName());
    }

    public ProductDto changeProductAvailability(String id, ProductAvailability available) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductException("Product not found."));
        product.setAvailable(available.isAvailable());
        return ProductDto.minEdited(productRepository.save(product));
    }

    public BigDecimal getMaxPrice() {
        return productRepository.findTopByAvailableTrueAndAmountLeftGreaterThanOrderByPriceDesc(0).getPrice();
    }
}
