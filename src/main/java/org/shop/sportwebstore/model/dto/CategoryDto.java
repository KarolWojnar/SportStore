package org.shop.sportwebstore.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.shop.sportwebstore.model.entity.Category;

@AllArgsConstructor
@Getter
@Setter
public class CategoryDto {
    private String name;

    public static CategoryDto toDto(Category category){
        return new CategoryDto(category.getName());
    }
}
