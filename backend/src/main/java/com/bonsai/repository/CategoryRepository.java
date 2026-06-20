package com.bonsai.repository;

import com.bonsai.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByTypeOrderBySortOrderAsc(String type);
    Optional<Category> findByNameAndType(String name, String type);
}
