package com.paellasoft.BoeApiSummary.controller;


import com.paellasoft.BoeApiSummary.entity.User;
import com.paellasoft.BoeApiSummary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam("username") String username, @RequestParam("password") String password) {
        // Verificar las credenciales del usuario
        User user = userService.findByUsernameAndPassword(username, password);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }

        // Devolver el ID del usuario en el header de la respuesta
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", String.valueOf(user.getId()));
        return ResponseEntity.ok().headers(headers).body("Login exitoso");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        HttpHeaders headers = new HttpHeaders();
        headers.remove("X-User-Id");
        return ResponseEntity.ok().headers(headers).body("Logout exitoso");
    }



}