package com.example.habitleague.shared.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ChallengeEventListener {
    private static final Logger log = LoggerFactory.getLogger(ChallengeEventListener.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public ChallengeEventListener(JavaMailSender mailSender,
                                  @Value("${spring.mail.username}") String fromAddress) {
        this.mailSender  = mailSender;
        this.fromAddress = fromAddress;
    }

    @Async("applicationTaskExecutor")
    @EventListener
    public void handleUserJoined(UserJoinedChallengeEvent event) {
        String destinatario  = event.getUserEmail();
        String challengeName = event.getChallengeName();
        log.info("Enviando correo a {} sobre el reto \"{}\"", destinatario, challengeName);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(destinatario);
        msg.setSubject(String.format("¡Te has unido al reto \"%s\"!", challengeName));
        msg.setText(String.format(
                "Hola,\n\n" +
                        "¡Te has unido al reto \"%s\"! ¡Mucho éxito!\n\n" +
                        "Saludos,\n" +
                        "El equipo de Habit Track",
                challengeName
        ));

        mailSender.send(msg);
    }

    @Async("applicationTaskExecutor")
    @EventListener
    public void handleChallengeCreated(ChallengeCreatedEvent event) {
        String destinatario  = event.getCreatorEmail();
        String challengeName = event.getChallengeName();
        log.info("Notificando creación de reto a {}: \"{}\"",
                destinatario, challengeName);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(destinatario);
        msg.setSubject(String.format("Tu reto \"%s\" ha sido creado!", challengeName));
        msg.setText(String.format(
                "Hola,\n\n" +
                        "Tu reto \"%s\" ha sido creado correctamente.\n\n" +  // ← solo nombre
                        "¡Mucho éxito!\n" +
                        "— El equipo de Habit Track",
                challengeName
        ));

        mailSender.send(msg);
    }

}
