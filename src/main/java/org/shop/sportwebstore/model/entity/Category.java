package org.shop.sportwebstore.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categories")
@Getter
@Setter
public class Category {
    @Id
    private String id;
    private String name;
}
