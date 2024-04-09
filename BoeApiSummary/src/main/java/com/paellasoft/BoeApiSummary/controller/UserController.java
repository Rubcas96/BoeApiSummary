package com.paellasoft.BoeApiSummary.controller;


import com.paellasoft.BoeApiSummary.dto.UserDTO;
import com.paellasoft.BoeApiSummary.entity.User;
import com.paellasoft.BoeApiSummary.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/user/register")
    public ResponseEntity<String> registerUser(@RequestBody User newUser) {
        userService.registerUser(newUser);
        return new ResponseEntity<>("Usuario registrado exitosamente", HttpStatus.CREATED);
    }

    @GetMapping("/user/info")
    public ResponseEntity<String> getUserInfo(@RequestParam Long userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            return new ResponseEntity<>("Información del usuario: " + user.toString(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/user/suscribir")
    public ResponseEntity<String> suscribirUsuario(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                userService.suscribirUsuario(userId);
                return ResponseEntity.ok("El usuario se ha suscrito correctamente al Boletín Oficial del Estado.");
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El encabezado X-User-Id no contiene un ID de usuario válido.");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al suscribir al usuario al Boletín Oficial del Estado.");
            }
        } else {
            return new ResponseEntity<>("El encabezado X-User-Id es requerido", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/user/baja")
    public ResponseEntity<String> darBaja(HttpServletRequest request) {
        // Obtener el ID del usuario del header
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isEmpty()) {
            // No se proporcionó un ID de usuario en el header, devuelve un error de autorización
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Se requiere autenticación para realizar esta acción.");
        }

        try {
            Long userId = Long.parseLong(userIdHeader);
            userService.darBaja(userId);
            return ResponseEntity.ok("El usuario se ha dado de baja correctamente del Boletín Oficial del Estado.");
        } catch (NumberFormatException e) {
            // El ID del usuario en el header no es un número válido
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El ID de usuario proporcionado en el header no es válido.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al darse de baja el usuario del Boletín Oficial del Estado.");
        }
    }

    @GetMapping("/user/dto/{id}")
    public ResponseEntity<UserDTO> obtenerUserPorId(@PathVariable Long id) {
        Optional<User> optionalUser = Optional.ofNullable(userService.getUserById(id));
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            UserDTO userDTO = UserDTO.fromEntity(user);
            return ResponseEntity.ok(userDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/user/delete")
    public void deleteUser(@RequestParam Long userId){
        userService.deleteUserById(userId);
    }


    @DeleteMapping("/user/delete/all")
    public void deleteAllUsers(@RequestParam Long userId){
        userService.deleteAllUser();
    }


}