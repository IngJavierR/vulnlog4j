package com.example.vulnlog4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VulnController {

    private static final Logger LOGGER = LogManager.getLogger(VulnController.class);

    @GetMapping("/vuln")
    public String vuln(@RequestParam("input") String input) {
        LOGGER.info("Input: " + input);
        return "";
    }
}
