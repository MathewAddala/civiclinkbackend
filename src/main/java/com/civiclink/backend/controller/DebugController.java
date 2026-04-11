package com.civiclink.backend.controller;

import com.civiclink.backend.entity.User;
import com.civiclink.backend.repository.UserRepository;
import com.civiclink.backend.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
public class DebugController {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RequestMappingHandlerMapping mapping;

    public DebugController(JwtService jwtService, UserRepository userRepository, RequestMappingHandlerMapping mapping) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.mapping = mapping;
    }

    @GetMapping("/whoami")
    public ResponseEntity<Map<String, Object>> whoAmI(@RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found."));
        return ResponseEntity.ok(Map.of(
                "email", email,
                "name", user.getName(),
                "role", user.getRole()
        ));
    }

    @GetMapping("/routes")
    public ResponseEntity<List<Map<String, Object>>> routes(@RequestHeader("Authorization") String authorization) {
        String email = jwtService.extractEmail(authorization.replace("Bearer ", ""));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!"admin".equalsIgnoreCase(user.getRole())) {
            throw new IllegalArgumentException("Only admin can view routes.");
        }

        List<Map<String, Object>> routes = mapping.getHandlerMethods().entrySet().stream()
                .flatMap(entry -> {
                    org.springframework.web.servlet.mvc.method.RequestMappingInfo info = entry.getKey();
                    org.springframework.web.method.HandlerMethod handlerMethod = entry.getValue();

                    Set<String> patterns = info.getPatternValues();
                    Set<org.springframework.web.bind.annotation.RequestMethod> requestMethods =
                            info.getMethodsCondition().getMethods();

                    String methodLabel = requestMethods.isEmpty() ? "ANY" : requestMethods.toString();

                    return patterns.stream().map(p -> Map.<String, Object>of(
                            "method", methodLabel,
                            "path", p,
                            "handler", handlerMethod.toString()
                    ));
                })
                .filter(m -> Objects.toString(m.get("path"), "").startsWith("/api/"))
                .sorted(Comparator.comparing(m -> Objects.toString(m.get("path"), "")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(routes);
    }
}
