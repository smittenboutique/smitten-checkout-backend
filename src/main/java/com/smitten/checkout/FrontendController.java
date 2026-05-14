package com.smitten.checkout;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping("/fbcheckout")
    public String fbcheckout() {
        return "redirect:/fbcheckout.html";
    }
}
