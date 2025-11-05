package com.ognjen.template.monolith.controllers.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EnvelopeWebController {

    @GetMapping("/envelopes")
    public String envelopes() {
        return "envelopes.html";
    }
}
