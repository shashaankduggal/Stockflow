package com.stockflow.category;

import com.stockflow.exception.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Category updateCategory(Long id, Category updatedCategory) {
        Category category = getCategory(id);
        category.setName(updatedCategory.getName());
        category.setDescription(updatedCategory.getDescription());
        return categoryRepository.save(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }
}
