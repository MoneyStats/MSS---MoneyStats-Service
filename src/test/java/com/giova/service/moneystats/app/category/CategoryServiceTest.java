package com.giova.service.moneystats.app.category;

import static org.mockito.Mockito.when;

import com.giova.service.moneystats.app.category.dto.Category;
import com.giova.service.moneystats.app.category.entity.CategoryEntity;
import com.giova.service.moneystats.generic.Response;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CategoryServiceTest {

    @Mock private CategoryCacheService iCategoryDAO;

    @Mock private CategoryMapper categoryMapper;

    @InjectMocks private CategoryService categoryService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllCategories() {
        List<CategoryEntity> categoryEntityList = new ArrayList<>();
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(1L);
        categoryEntity.setName("Test Category");
        categoryEntityList.add(categoryEntity);

        List<Category> categories = new ArrayList<>();
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        categories.add(category);

        when(iCategoryDAO.findAll()).thenReturn(categoryEntityList);
        when(categoryMapper.mapCategoryEntityToCategory(categoryEntityList)).thenReturn(categories);

        ResponseEntity<Response> responseEntity = categoryService.getAllCategories();
        Response response = responseEntity.getBody();

        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertEquals("List of category!", response.getMessage());
        Assertions.assertEquals(categories, response.getData());
    }
}
