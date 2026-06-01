//package com.shopsphere.auth_service.controller;
//
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class TestController {
//
//    // =========================
//    // Public API
//    // =========================
//    @GetMapping("/public")
//    public String publicApi() {
//
//        return "Public API";
//    }
//
//    // =========================
//    // Customer API
//    // =========================
//    @GetMapping("/customer")
//    @PreAuthorize("hasRole('CUSTOMER')")
//    public String customerApi() {
//
//        return "Customer API";
//    }
//
//    // =========================
//    // Store Owner API
//    // =========================
//    @GetMapping("/store-owner")
//    @PreAuthorize("hasRole('STORE_OWNER')")
//    public String storeOwnerApi() {
//
//        return "Store Owner API";
//    }
//
//    // =========================
//    // Admin API
//    // =========================
//    @GetMapping("/admin")
//    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
//    public String adminApi() {
//
//        return "Admin API";
//    }
//}