package com.hutech.demo.controller;

import com.hutech.demo.model.Product;
import com.hutech.demo.service.CategoryService;

import com.hutech.demo.service.ProductSevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller

@RequestMapping("/")
public class ProductController {
    @Autowired
    private ProductSevice productService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/")
    public String redirectToProducts() {
        return "redirect:/products";
    }

    @GetMapping("/products")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String showProductList(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "/products/product-list";
    }
    // For adding a new product
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/products/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());

        return "/products/add-product";
    }
    private String saveImageStatic(MultipartFile image) throws IOException {
        File saveFile = new ClassPathResource("static/images").getFile();
        String fileName = UUID.randomUUID()+ "." + StringUtils.getFilenameExtension(image.getOriginalFilename());
        Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + fileName);
        Files.copy(image.getInputStream(), path);
        return fileName;
    }
    // Process the form for adding a new product
    @PostMapping("/products/add")
    public String addProduct(@Valid Product product, BindingResult result,@RequestParam("image") MultipartFile imageFile) {
        if (result.hasErrors()) {
            return "/products/add-product";
        }
        if (!imageFile.isEmpty()) {
            try {
                String imageName = saveImageStatic(imageFile);
                product.setImageData("/images/" +imageName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        productService.addProduct(product);
        return "redirect:/products";
    }
    // For editing a product
    @GetMapping("/products/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "/products/update-product";
    }
    // Process the form for updating a product
    @PostMapping("/products/update/{id}")
    public String updateProduct(@PathVariable Long id, @Valid Product product,BindingResult result) {
        if (result.hasErrors()) {
            product.setId(id);
            return "/products/update-product";
        }
        productService.updateProduct(product);
        return "redirect:/products";
    }
    // Handle request to delete a product
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return "redirect:/products";
    }
}