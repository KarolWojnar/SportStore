package org.shop.sportwebstore.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductsListInfo {

    private List<ProductDto> products;
    private long totalElements;
    private List<String> categories;

    public ProductsListInfo(List<ProductDto> products, long totalElements) {
        this.products = products;
        this.totalElements = totalElements;
    }
}
