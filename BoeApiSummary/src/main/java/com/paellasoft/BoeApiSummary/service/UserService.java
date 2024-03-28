package com.paellasoft.BoeApiSummary.service;


import com.paellasoft.BoeApiSummary.Repository.IBoeRepository;
import com.paellasoft.BoeApiSummary.Repository.IUserRepository;
import com.paellasoft.BoeApiSummary.entity.User;
import com.paellasoft.BoeApiSummary.mail.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IBoeRepository boeRepository;

    private final EmailSender emailSender;

    public UserService(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void registerUser(User user) {
        // Guardar usuario en la base de datos
        User savedUser = userRepository.save(user);

        // Enviar correo electrónico de confirmación
        enviarCorreoConfirmacion(savedUser);
    }

    private void enviarCorreoConfirmacion(User user) {
        String to = user.getEmail();
        String subject = "Confirmación de registro";
        String text = "Hola " + user.getUsername() + ", tu registro ha sido exitoso.";
        emailSender.sendEmail(to, subject, text);
    }

    @Transactional
    public void suscribirUsuario(Long userId) {
        // Obtener el usuario y el boletín oficial correspondientes
        Optional<User> optionalUser = userRepository.findById(userId);


        // Verificar si el usuario existe
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setSendNotification(true);


            // Envío de correo electrónico de notificacion
            String to = user.getEmail();
            String subject = "Suscripción";
            String text = "Hola " + user.getUsername() + ", te has suscrito a Boe Newsletter.";
            emailSender.sendEmail(to, subject, text);

        } else {
            throw new RuntimeException("El usuario o el Boletín Oficial especificados no existen.");
        }
    }


    @Transactional
    public void darBaja(Long userId) {
        // Obtener el usuario y el boletín oficial correspondientes
        Optional<User> optionalUser = userRepository.findById(userId);


        // Verificar si el usuario existe
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setSendNotification(false);


            // Envío de correo electrónico de notificacion
            String to = user.getEmail();
            String subject = "Suscripción";
            String text = "Hola " + user.getUsername() + ", te has dado de baja del Boe Newsletter.";
            emailSender.sendEmail(to, subject, text);

        } else {
            throw new RuntimeException("El usuario o el Boletín Oficial especificados no existen.");
        }
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }


    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

    public void deleteAllUser() {
        userRepository.deleteAll();
    }

    public User findByUsernameAndPassword(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password);

    }

}
