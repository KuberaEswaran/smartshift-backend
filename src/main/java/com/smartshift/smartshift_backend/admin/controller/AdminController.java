package com.smartshift.smartshift_backend.admin.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("api/admin")
public class AdminController {
    @PostMapping("/addmanager")
    public String addManager(RequestMapping req){
        return "success";
    }
}