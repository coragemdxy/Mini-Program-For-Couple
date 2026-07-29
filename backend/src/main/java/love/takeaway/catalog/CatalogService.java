package love.takeaway.catalog;

import love.takeaway.common.ApiException;
import love.takeaway.common.PageResponse;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

import static love.takeaway.catalog.CatalogDtos.*;

@Service
public class CatalogService {

    private final CategoryRepository categories;
    private final ProductRepository products;
    private final CatalogMapper mapper;

    public CatalogService(CategoryRepository categories, ProductRepository products, CatalogMapper mapper) {
        this.categories = categories;
        this.products = products;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> categories(boolean includeDisabled) {
        List<Category> source = includeDisabled
                ? categories.findAllByOrderBySortOrderAscIdAsc()
                : categories.findByEnabledTrueOrderBySortOrderAscIdAsc();
        return source.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> products(
            Long categoryId,
            String query,
            Boolean enabled,
            int page,
            int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize,
                Sort.by(Sort.Order.desc("recommended"), Sort.Order.asc("sortOrder"), Sort.Order.asc("id")));

        Specification<Product> spec = (root, cq, cb) -> cb.conjunction();
        if (categoryId != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }
        if (enabled != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("enabled"), enabled));
        }
        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("searchKeywords")), pattern)
            ));
        }

        Page<ProductResponse> result = products.findAll(spec, pageable).map(mapper::toResponse);
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public ProductResponse product(Long id, boolean allowDisabled) {
        Product product = products.findDetailedById(id)
                .orElseThrow(() -> ApiException.notFound("PRODUCT_NOT_FOUND", "商品不存在"));
        if (!allowDisabled && !product.isEnabled()) {
            throw ApiException.notFound("PRODUCT_NOT_FOUND", "商品不存在或已下架");
        }
        return mapper.toResponse(product);
    }

    @Transactional
    public CategoryResponse createCategory(UpsertCategoryRequest request) {
        Category category = new Category(
                request.name().trim(),
                clean(request.iconUrl()),
                request.sortOrder(),
                request.enabled()
        );
        return mapper.toResponse(categories.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, UpsertCategoryRequest request) {
        Category category = categories.findById(id)
                .orElseThrow(() -> ApiException.notFound("CATEGORY_NOT_FOUND", "分类不存在"));
        category.update(
                request.name().trim(),
                clean(request.iconUrl()),
                request.sortOrder(),
                request.enabled()
        );
        return mapper.toResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categories.findById(id)
                .orElseThrow(() -> ApiException.notFound("CATEGORY_NOT_FOUND", "分类不存在"));
        category.disable();
    }

    @Transactional
    public ProductResponse createProduct(UpsertProductRequest request) {
        Category category = activeCategory(request.categoryId());
        Product product = new Product(
                category,
                request.name().trim(),
                clean(request.description()),
                clean(request.coverUrl()),
                request.virtualPrice(),
                request.stock(),
                request.enabled(),
                request.recommended(),
                request.sortOrder(),
                clean(request.searchKeywords())
        );
        product.replaceOptionGroups(buildGroups(request.optionGroups()));
        return mapper.toResponse(products.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, UpsertProductRequest request) {
        Product product = products.findDetailedById(id)
                .orElseThrow(() -> ApiException.notFound("PRODUCT_NOT_FOUND", "商品不存在"));
        Category category = activeCategory(request.categoryId());
        product.update(
                category,
                request.name().trim(),
                clean(request.description()),
                clean(request.coverUrl()),
                request.virtualPrice(),
                request.stock(),
                request.enabled(),
                request.recommended(),
                request.sortOrder(),
                clean(request.searchKeywords())
        );
        product.replaceOptionGroups(buildGroups(request.optionGroups()));
        products.flush();
        return mapper.toResponse(product);
    }

    @Transactional
    public ProductResponse availability(Long id, boolean enabled) {
        Product product = products.findDetailedById(id)
                .orElseThrow(() -> ApiException.notFound("PRODUCT_NOT_FOUND", "商品不存在"));
        product.setEnabled(enabled);
        return mapper.toResponse(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = products.findDetailedById(id)
                .orElseThrow(() -> ApiException.notFound("PRODUCT_NOT_FOUND", "商品不存在"));
        product.setEnabled(false);
    }

    private Category activeCategory(Long id) {
        Category category = categories.findById(id)
                .orElseThrow(() -> ApiException.notFound("CATEGORY_NOT_FOUND", "分类不存在"));
        if (!category.isEnabled()) {
            throw ApiException.badRequest("CATEGORY_DISABLED", "不能把商品放入已停用分类");
        }
        return category;
    }

    private List<ProductOptionGroup> buildGroups(List<OptionGroupRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream().map(request -> {
            if (request.minSelect() > request.maxSelect()) {
                throw ApiException.badRequest("OPTION_RULE_INVALID", "规格最少选择数不能大于最多选择数");
            }
            if (request.required() && request.minSelect() < 1) {
                throw ApiException.badRequest("OPTION_RULE_INVALID", "必选规格的最少选择数必须大于零");
            }
            if (request.maxSelect() > request.options().size()) {
                throw ApiException.badRequest("OPTION_RULE_INVALID", "规格最多选择数不能超过选项数量");
            }
            ProductOptionGroup group = new ProductOptionGroup(
                    request.name().trim(),
                    request.required(),
                    request.minSelect(),
                    request.maxSelect(),
                    request.sortOrder()
            );
            request.options().forEach(option -> group.addOption(
                    option.name().trim(),
                    option.extraPrice(),
                    option.enabled(),
                    option.sortOrder()
            ));
            return group;
        }).toList();
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
