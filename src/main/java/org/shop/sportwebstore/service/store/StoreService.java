package org.shop.sportwebstore.service.store;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shop.sportwebstore.exception.ProductException;
import org.shop.sportwebstore.model.dto.ProductDto;
import org.shop.sportwebstore.model.entity.Cart;
import org.shop.sportwebstore.model.entity.Category;
import org.shop.sportwebstore.model.entity.Product;
import org.shop.sportwebstore.repository.CategoryRepository;
import org.shop.sportwebstore.repository.ProductRepository;
import org.shop.sportwebstore.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {

    private final CartRedisService cartRedisService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public void addToCart(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductException("Product is unavailable.");
        }
        String authUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String userId = userRepository.findByEmail(authUser).orElseThrow().getId();
        Cart cart = cartRedisService.getCart(userId);
        if (cart == null) {
            cart = new Cart(userId);
        }
        cart.addProduct(productId, 1);
        cartRedisService.saveCart(userId, cart);
    }

    public Map<String, Object> getProducts(int page, int size, String sort, String direction,
                                           String search, List<String> categories) {
        Sort.Direction direct = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortObj = Sort.by(direct, sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<Product> products;
        if (categories == null || categories.isEmpty()) {
            products = productRepository.findByNameMatchesRegexIgnoreCase(".*" + search + ".*", pageable);
        } else {
            products = productRepository.findByNameMatchesRegexIgnoreCaseAndCategoriesIn(".*" + search + ".*", categories, pageable);
        }
        return Map.of(
                "products", products.getContent().stream().map(ProductDto::toDto).toList(),
                "totalElements", products.getTotalElements()
        );
    }

    public List<String> getCategories() {
        return categoryRepository.findAll().stream().map(Category::getName).toList();
    }
}
