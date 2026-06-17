package com.bonsai.config;

import com.bonsai.entity.Category;
import com.bonsai.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            initCategories();
        }
    }

    private void initCategories() {
        List<Category> species = Arrays.asList(
            createSpecies("五针松", "松柏类", 1, "🌲"),
            createSpecies("黑松", "松柏类", 2, "🌲"),
            createSpecies("罗汉松", "松柏类", 3, "🎄"),
            createSpecies("真柏", "松柏类", 4, "🌿"),
            createSpecies("榕树", "杂木类", 5, "🌳"),
            createSpecies("榆树", "杂木类", 6, "🌳"),
            createSpecies("枫树", "杂木类", 7, "🍁"),
            createSpecies("对接白蜡", "杂木类", 8, "🌿"),
            createSpecies("黄杨", "杂木类", 9, "🌳"),
            createSpecies("三角梅", "观花观果类", 10, "🌸")
        );

        List<Category> styles = Arrays.asList(
            createStyle("直干式", "传统造型", 1),
            createStyle("斜干式", "传统造型", 2),
            createStyle("曲干式", "传统造型", 3),
            createStyle("悬崖式", "意境造型", 4),
            createStyle("临水式", "意境造型", 5),
            createStyle("文人树", "意境造型", 6),
            createStyle("丛林式", "组合造型", 7),
            createStyle("附石式", "组合造型", 8),
            createStyle("水旱盆景", "组合造型", 9),
            createStyle("微型盆景", "规格类", 10)
        );

        categoryRepository.saveAll(species);
        categoryRepository.saveAll(styles);
    }

    private Category createSpecies(String name, String groupName, int sortOrder, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setType("species");
        category.setGroupName(groupName);
        category.setSortOrder(sortOrder);
        category.setIcon(icon);
        return category;
    }

    private Category createStyle(String name, String groupName, int sortOrder) {
        Category category = new Category();
        category.setName(name);
        category.setType("style");
        category.setGroupName(groupName);
        category.setSortOrder(sortOrder);
        return category;
    }
}
