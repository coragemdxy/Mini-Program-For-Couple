package love.takeaway.catalog;

import org.springframework.stereotype.Component;

import static love.takeaway.catalog.CatalogDtos.*;

@Component
public class CatalogMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getIconUrl(),
                category.getSortOrder(),
                category.isEnabled(),
                category.getVersion()
        );
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getDescription(),
                product.getCoverUrl(),
                product.getVirtualPrice(),
                product.getStock(),
                product.isEnabled(),
                product.isRecommended(),
                product.getSortOrder(),
                product.getSearchKeywords(),
                product.getVersion(),
                product.getOptionGroups().stream().map(this::toResponse).toList()
        );
    }

    private OptionGroupResponse toResponse(ProductOptionGroup group) {
        return new OptionGroupResponse(
                group.getId(),
                group.getName(),
                group.isRequired(),
                group.getMinSelect(),
                group.getMaxSelect(),
                group.getSortOrder(),
                group.getOptions().stream()
                        .map(option -> new OptionResponse(
                                option.getId(),
                                option.getName(),
                                option.getExtraPrice(),
                                option.isEnabled(),
                                option.getSortOrder()
                        ))
                        .toList()
        );
    }
}

