package com.bonsai.service;

import com.bonsai.entity.Category;
import com.bonsai.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Cacheable(value = "categories", key = "#type")
    public List<Category> getCategoriesByType(String type) {
        return categoryRepository.findByTypeOrderBySortOrderAsc(type);
    }

    @Cacheable(value = "allCategories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
