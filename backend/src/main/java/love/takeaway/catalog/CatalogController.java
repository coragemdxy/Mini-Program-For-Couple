package love.takeaway.catalog;

import love.takeaway.common.PageResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static love.takeaway.catalog.CatalogDtos.*;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalog;

    public CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return catalog.categories(false);
    }

    @GetMapping("/products")
    public PageResponse<ProductResponse> products(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return catalog.products(categoryId, query, true, page, size);
    }

    @GetMapping("/products/{id}")
    public ProductResponse product(@PathVariable Long id) {
        return catalog.product(id, false);
    }
}

