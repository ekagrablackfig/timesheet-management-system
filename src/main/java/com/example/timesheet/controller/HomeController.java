package com.example.timesheet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "redirect:/timesheets";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
