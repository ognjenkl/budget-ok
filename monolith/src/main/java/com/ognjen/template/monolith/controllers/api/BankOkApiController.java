package com.ognjen.template.monolith.controllers.api;

import com.ognjen.template.monolith.models.BankOkCart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/bankok")
public class BankOkApiController {

    @Value("${bankok.api.host}")
    private String bankOkApiHost;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/carts/{id}")
    public BankOkCart getCartByUserId(@PathVariable("id") int id) {
        String url = bankOkApiHost + "/carts/" + id;
        return restTemplate.getForObject(url, BankOkCart.class);
    }
}
