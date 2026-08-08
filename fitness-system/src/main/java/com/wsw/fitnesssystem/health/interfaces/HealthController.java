package com.wsw.fitnesssystem.health.interfaces;

import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public String studentTest() {
        return "student test";
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public String teacherTest() {
        return "teacher test";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminTest() {
        return "admin test";
    }

    @GetMapping("/admin/hello")
    public String adminHello() {
        return "admin hello";
    }

    @GetMapping("/student/records")
    @PreAuthorize("hasRole('ADMIN') && hasAuthority('fitness:record:view')")
    public String getStudentRecord() {
        return "student records test!!!";
    }
}
