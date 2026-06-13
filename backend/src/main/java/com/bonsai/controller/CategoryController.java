package com.bonsai.controller;

import com.bonsai.dto.Result;
import com.bonsai.entity.Category;
import com.bonsai.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> getAllCategories() {
        return Result.success(categoryService.getAllCategories());
    }

    @GetMapping("/type/{type}")
    public Result<List<Category>> getCategoriesByType(@PathVariable String type) {
        return Result.success(categoryService.getCategoriesByType(type));
    }
}
