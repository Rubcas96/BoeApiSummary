package com.paellasoft.BoeApiSummary.service;


import com.paellasoft.BoeApiSummary.Repository.IBoeRepository;
import com.paellasoft.BoeApiSummary.Repository.IBoeUser;
import com.paellasoft.BoeApiSummary.Repository.IUserRepository;
import com.paellasoft.BoeApiSummary.chatGpt.ChatGptRequest;
import com.paellasoft.BoeApiSummary.chatGpt.ChatGptResponse;
import com.paellasoft.BoeApiSummary.entity.Boe;
import com.paellasoft.BoeApiSummary.entity.BoeUser;
import com.paellasoft.BoeApiSummary.entity.User;
import com.paellasoft.BoeApiSummary.mail.EmailSender;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Component
@Transactional
public class BoeService {
    @Value("${openai.api.key}")
    private String OPENAI_API_KEY;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;


    @Autowired
    private RestTemplate template;

    @Autowired
    private IBoeRepository boeRepository;

    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private UserService userService;

    @Autowired
    private IBoeUser boeUserRepo;
    @Autowired
    private EmailSender emailSender;


    @Scheduled(cron = "0 * * * * *")
    public String obtenerBoeDelDia() {

        // Obtener la fecha actual
        LocalDate fechaActual = LocalDate.now();
        // Formatear la fecha en el formato esperado por la URL del BOE
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String fechaFormateada = fechaActual.format(formatter);
        // Construir la URL del BOE del día actual
        String url = "https://www.boe.es/boe/dias/" + fechaFormateada + "/index.php?s=1";

        // Crear cliente HTTP
        HttpClient client = HttpClient.newHttpClient();
        // Crear solicitud HTTP GET para obtener el BOE
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        try {
            // Enviar solicitud y obtener respuesta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Verificar si la solicitud fue exitosa (código de estado 200)
            if (response.statusCode() == 200) {
                // Extraer el contenido HTML del BOE
                String boeContent = response.body();
                String htmlContent = response.body();

                // Procesar HTML para extraer texto puro
                String textoPuro = extraerTextoPuro(htmlContent);
                comprobarCambiosEnBoe(textoPuro);

                // Resumir el texto utilizando la API de OpenAI

                return textoPuro;
            } else {
                // Manejar errores de solicitud HTTP
                System.out.println("Error al obtener el BOE del día: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return "";
    }


    public void comprobarCambiosEnBoe(String textoPuro) {
        // Obtener el último Boletín Oficial registrado
        Boe ultimoBoe = boeRepository.findTopByOrderByFechaBoeDesc();
        if (ultimoBoe == null) {
            registrarNuevoBoe(textoPuro);
        } else {
            // Obtener el fragmento de texto original del texto puro
            String fragmentoTextoOriginal = textoPuro.substring(5, 40);

            // Verificar si el fragmento de texto original coincide con el del último Boletín registrado
            if (fragmentoTextoOriginal.equals(ultimoBoe.getContenidoOriginal())) {
                System.out.println("Este Boletín Oficial ya está registrado.");
            } else {
                // Registrar el nuevo Boletín Oficial
                registrarNuevoBoe(textoPuro);
            }
        }
    }
    public void registrarNuevoBoe(String textoPuro) {
        // Obtener la fecha actual
        LocalDateTime fechaRegistro = LocalDateTime.now();
        DateTimeFormatter formateoRegistro = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String fechaBoe = fechaRegistro.format(formateoRegistro);

        // Resumir el texto utilizando la API de OpenAI
        String resumen = resumirConChatGpt(textoPuro);

        // Obtener los fragmentos de texto original y resumen
        String fragmentoTextoOriginal = textoPuro.substring(5, Math.min(textoPuro.length(), 40));


        // Crear el objeto Boe
        Boe boe = new Boe();
        boe.setContenidoOriginal(fragmentoTextoOriginal);
        boe.setContenidoResumido(resumen);
        boe.setFechaBoe(fechaBoe);

        // Guardar el nuevo Boletín Oficial en la base de datos
        boeRepository.save(boe);

        // Notificar a los suscriptores del nuevo Boletín Oficial
        notificarNuevoBoeASuscriptores(resumen);
        crearBoeUserParaUsuariosConSendNotification(boe, userRepository.findAll());
    }

    private void notificarNuevoBoeASuscriptores(String resumen) {
        // Obtener todos los usuarios
        List<User> usuarios = userRepository.findAll();

        for (User usuario : usuarios) {
            // Verificar si el usuario tiene la opción sendNotification seleccionada
            if (usuario.isSendNotification()) {
                String to = usuario.getEmail();
                String subject = "Nuevo Boletín Oficial disponible";
                String text = "Estimado " + usuario.getUsername() + ",\n\nSe ha detectado un nuevo Boletín Oficial. Le enviamos el resumen: \n\n"+resumen;
                emailSender.sendEmail(to, subject, text);

                System.out.println("Correo enviado a: " + usuario.getEmail());
            }
        }
    }

    public void crearBoeUserParaUsuariosConSendNotification(Boe boe, List<User> usuarios) {
        // Crear y guardar un objeto BoeUser para cada usuario con sendNotification activo
        for (User usuario : usuarios) {
            // Verificar si el usuario tiene sendNotification activo
            if (usuario.isSendNotification()) {
                BoeUser boeUser = new BoeUser();
                boeUser.setBoe(boe);
                boeUser.setUser(usuario);
                boeUserRepo.save(boeUser);
            }
        }
    }


    private String extraerTextoPuro(String htmlContent) {
        // Parsear el contenido HTML utilizando Jsoup
        Document doc = Jsoup.parse(htmlContent);

        // Extraer el texto de todas las etiquetas <p> (párrafos) y <div> (divisiones)
        Element elementosTexto = doc.selectFirst("div.sumario");

        //Elements elementosTexto = doc.select(".sumario");

        // Element elementosTexto = doc.getElementById("sec661");
        String texto = elementosTexto.text();

        // Limitar la cantidad de texto extraído
        int maxTokens = 16385; // Establecer el límite máximo de tokens permitidos
        if (texto.length() > maxTokens) {
            texto = texto.substring(0, maxTokens);    }

        return texto;
    }

    public Long getUserIdFromHeader(HttpServletRequest request) {

        String userIdHeader = request.getHeader("X-User-Id");

        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                System.out.println("El encabezado X-User-Id contiene un ID de usuario no válido");
            }
        }

        return null;
    }

    public void solicitarBoe(HttpServletRequest request, Long boeId) {
        // Obtener el ID del usuario autenticado
        Long userId = getUserIdFromHeader(request);

        if (userId != null) {
            // Obtener el usuario a partir del ID
            User user = userRepository.findById(userId).orElse(null);

            if (user != null) {
                // Obtener el BOE a partir del ID
                Boe boe = boeRepository.findById(boeId).orElse(null);

                if (boe != null) {
                    // Enviar el resumen del BOE al correo electrónico del usuario
                    String to = user.getEmail();
                    String subject = "Boe Solicitado nº"+boe.getId();
                    String text = "Estimado " + user.getUsername() + ",\n\n Le enviamos el resumen del BOE que ha solicitado: \n\n"+boe.getContenidoResumido();
                    emailSender.sendEmail(to, subject, text);
                    System.out.println("Resumen del BOE enviado al correo electrónico del usuario");
                } else {
                    System.out.println("No se encontró ningún BOE con el ID proporcionado");
                }
            } else {
                System.out.println("No se encontró ningún usuario con el ID proporcionado");
            }
        } else {
            System.out.println("Usuario no autenticado");
        }
    }


    public void sendUnsubscribedBoeSummaryToUser(Long userId) {
        // Obtener boletines a los que el usuario no está suscrito
        List<Boe> unsubscribedBoes = boeRepository.findNotSubscribedBoes(userId);

        // Verificar si se encontraron boletines
        if (!unsubscribedBoes.isEmpty()) {
            // Construir el mensaje con los resúmenes de los boletines
            StringBuilder message = new StringBuilder();
            message.append("Estimado usuario,\n\n");
            message.append("Aquí tienes los resúmenes de los boletines a los que no estás suscrito:\n\n");

            for (Boe boe : unsubscribedBoes) {
                message.append("ID: ").append(boe.getId()).append("\n");
                message.append("Contenido: ").append(boe.getContenidoResumido()).append("\n\n");
            }

            // Enviar el mensaje por correo electrónico al usuario
            Optional<User> user = userRepository.findById(userId);
                    String to = user.map(User::getEmail).orElse(null);
            String subject = "Resúmenes de boletines no suscritos";
            emailSender.sendEmail(to, subject, message.toString());
        }
    }


    private String resumirConChatGpt(String texto) {
        try {
            // Crear la solicitud a la API de OpenAI
            ChatGptRequest request = new ChatGptRequest(model, "Resume a la mitad lo destacable: "+ texto);

            // Realizar la solicitud a la API de OpenAI
            ChatGptResponse response = template.postForObject(apiUrl, request, ChatGptResponse.class);

            // Extraer el resumen del texto de la respuesta
            String resumen = response.getChoices().get(0).getMessage().getContent();

            // Retornar el resumen
            return resumen;
        } catch (Exception e) {
            // Manejar excepciones
            e.printStackTrace();
            return null;
        }
    }

public void deleteAllBoes(){
        boeRepository.deleteAll();
}

    }


