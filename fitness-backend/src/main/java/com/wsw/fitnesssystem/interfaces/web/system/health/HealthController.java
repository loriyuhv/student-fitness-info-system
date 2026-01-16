package com.wsw.fitnesssystem.interfaces.web.system.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author loriyuhv
 * @version 1.0 2026/1/13 20:27
 * @since 1.0
 */
@RestController
@RequestMapping("/health")
public class HealthController {
    @GetMapping
    public String health() {
        return "OK!";
    }
}
