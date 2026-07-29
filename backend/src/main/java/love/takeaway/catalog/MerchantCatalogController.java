package love.takeaway.catalog;

import jakarta.validation.Valid;
import love.takeaway.common.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static love.takeaway.catalog.CatalogDtos.*;

@RestController
@RequestMapping("/api/merchant")
public class MerchantCatalogController {

    private final CatalogService catalog;

    public MerchantCatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return catalog.categories(true);
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@Valid @RequestBody UpsertCategoryRequest request) {
        return catalog.createCategory(request);
    }

    @PutMapping("/categories/{id}")
    public CategoryResponse updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpsertCategoryRequest request
    ) {
        return catalog.updateCategory(id, request);
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        catalog.deleteCategory(id);
    }

    @GetMapping("/products")
    public PageResponse<ProductResponse> products(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return catalog.products(categoryId, query, enabled, page, size);
    }

    @GetMapping("/products/{id}")
    public ProductResponse product(@PathVariable Long id) {
        return catalog.product(id, true);
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody UpsertProductRequest request) {
        return catalog.createProduct(request);
    }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpsertProductRequest request
    ) {
        return catalog.updateProduct(id, request);
    }

    @PatchMapping("/products/{id}/availability")
    public ProductResponse availability(
            @PathVariable Long id,
            @RequestBody AvailabilityRequest request
    ) {
        return catalog.availability(id, request.enabled());
    }

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        catalog.deleteProduct(id);
    }
}

