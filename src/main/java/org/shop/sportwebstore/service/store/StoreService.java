package org.shop.sportwebstore.service.store;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shop.sportwebstore.exception.ProductException;
import org.shop.sportwebstore.model.dto.ProductCart;
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

import java.util.Collections;
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
        Product product = productRepository.findByIdAndAmountLeftIsGreaterThan(productId, 0).orElseThrow(() -> new ProductException("Product not found."));
        String authUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String userId = userRepository.findByEmail(authUser).orElseThrow().getId();
        Cart cart = cartRedisService.getCart(userId);
        if (cart == null) {
            cart = new Cart(userId);
        }
        if (checkAmount(cart, product)) {
            cart.addProduct(productId, 1);
            cartRedisService.saveCart(userId, cart);
        }
    }

    private boolean checkAmount(Cart cart, Product product) {
        return product.getAmountLeft() >= cart.getQuantity(product.getId()) + 1;
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
                "products", products.getContent().stream().map(product -> ProductDto.toDto(product, false)).toList(),
                "totalElements", products.getTotalElements()
        );
    }

    public List<String> getCategories() {
        return categoryRepository.findAll().stream().map(Category::getName).toList();
    }

    public Map<String, Object> getFeaturedProducts() {
        List<Product> products = productRepository.findTop9ByOrderByOrdersDesc();
        return Map.of("products", products.stream().map(product -> ProductDto.toDto(product, false)).toList());
    }

    public Map<String, Object> getDetails(String id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductException("Product not found."));
        List<Product> relatedProducts = productRepository.findTop4ByCategoriesInAndIdNot(Collections.singleton(product.getCategories()), id);
        return Map.of(
                "product", ProductDto.toDto(product, true),
                "relatedProducts", relatedProducts.stream().map(ProductDto::minDto).toList());
    }

    public Map<String, Object> getCart() {
        String authUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String userId = userRepository.findByEmail(authUser).orElseThrow().getId();
        Cart cart = cartRedisService.getCart(userId);
        if (cart == null) {
            return Map.of("products", List.of());
        }
        List<Product> products = productRepository.findAllById(cart.getProducts().keySet());
        List<ProductCart> productCarts = products.stream().map(product -> ProductCart.toDto(product, cart.getProducts().get(product.getId()))).toList();
        return Map.of("products", productCarts);
    }

    public void removeFromCart(String id) {
        String authUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String userId = userRepository.findByEmail(authUser).orElseThrow().getId();
        Cart cart = cartRedisService.getCart(userId);
        if (cart == null) {
            throw new ProductException("Cart is empty.");
        }
        cart.removeProduct(id);
        cartRedisService.saveCart(userId, cart);
    }

    public void deleteAllFromProduct(String id) {
        String authUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String userId = userRepository.findByEmail(authUser).orElseThrow().getId();
        Cart cart = cartRedisService.getCart(userId);
        if (cart == null) {
            throw new ProductException("Cart is empty.");
        }
        cart.getProducts().remove(id);
        cartRedisService.saveCart(userId, cart);
    }

    public void deleteCart() {
        String authUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String userId = userRepository.findByEmail(authUser).orElseThrow().getId();
        cartRedisService.deleteCart(userId);
    }
}
