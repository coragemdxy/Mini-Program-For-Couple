package love.takeaway.catalog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record CategoryResponse(
            Long id,
            String name,
            String iconUrl,
            int sortOrder,
            boolean enabled,
            long version
    ) {
    }

    public record UpsertCategoryRequest(
            @NotBlank @Size(max = 80) String name,
            @Size(max = 500) String iconUrl,
            @Min(0) @Max(10000) int sortOrder,
            boolean enabled
    ) {
    }

    public record ProductResponse(
            Long id,
            Long categoryId,
            String categoryName,
            String name,
            String description,
            String coverUrl,
            BigDecimal virtualPrice,
            Integer stock,
            boolean enabled,
            boolean recommended,
            int sortOrder,
            String searchKeywords,
            long version,
            List<OptionGroupResponse> optionGroups
    ) {
    }

    public record OptionGroupResponse(
            Long id,
            String name,
            boolean required,
            int minSelect,
            int maxSelect,
            int sortOrder,
            List<OptionResponse> options
    ) {
    }

    public record OptionResponse(
            Long id,
            String name,
            BigDecimal extraPrice,
            boolean enabled,
            int sortOrder
    ) {
    }

    public record UpsertProductRequest(
            @NotNull Long categoryId,
            @NotBlank @Size(max = 120) String name,
            @Size(max = 1000) String description,
            @Size(max = 500) String coverUrl,
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal virtualPrice,
            @Min(0) Integer stock,
            boolean enabled,
            boolean recommended,
            @Min(0) @Max(10000) int sortOrder,
            @Size(max = 500) String searchKeywords,
            @Valid List<OptionGroupRequest> optionGroups
    ) {
    }

    public record OptionGroupRequest(
            @NotBlank @Size(max = 80) String name,
            boolean required,
            @Min(0) int minSelect,
            @Min(1) int maxSelect,
            @Min(0) int sortOrder,
            @NotEmpty @Valid List<OptionRequest> options
    ) {
    }

    public record OptionRequest(
            @NotBlank @Size(max = 80) String name,
            @NotNull @DecimalMin("0.00") BigDecimal extraPrice,
            boolean enabled,
            @Min(0) int sortOrder
    ) {
    }

    public record AvailabilityRequest(boolean enabled) {
    }
}

