package io.movetodata.scheduler.library.services;

import io.movetodata.platform.library.models.SMTPConfigModel;
import io.movetodata.platform.library.repository.SMTPConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Slf4j
@Transactional
@Service(value = "MailService")
public class MailService {
    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private SMTPConfigRepository smtpConfigRepository;

    public void sendMail(String sendTo, String subject, String body, String userName, String buttonLink) throws MessagingException {
        log.info(this.getClass().getName() + ".SendMail Start!");
        Context context = new Context();
        context.setVariable("name", userName);
        context.setVariable("data", body);
        context.setVariable("buttonLink", buttonLink);

        String htmlContent = templateEngine.process("dashboard-mail-template.html", context);

        SMTPConfigModel smtpConfigModel = smtpConfigRepository.findByConfig("platform");
        Properties properties = new Properties();
        properties.put("mail.smtp.host", smtpConfigModel.getHost());
        properties.put("mail.smtp.port", smtpConfigModel.getPort());
        properties.put("mail.smtp.auth", smtpConfigModel.getAuth());
        properties.put("mail.smtp.starttls.enable", smtpConfigModel.getTtls());

        // Create a Session with authentication credentials
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpConfigModel.getSmtpEmail(), smtpConfigModel.getSmtpPassword());
            }
        });

        try {
            // Create a MimeMessage object
            Message message = new MimeMessage(session);

            // Set the From address
            message.setFrom(new InternetAddress(smtpConfigModel.getSmtpEmail()));

            // Set the To address
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sendTo));

            // Set the subject
            message.setSubject(subject);

            // Set the body content
            message.setContent(htmlContent, "text/html");

            // Send the email
            Transport.send(message);

            log.info("Email sent successfully!");
        } catch (MessagingException e) {
            log.info("Failed to send email. Error: " + e.getMessage());
        }


        log.info(this.getClass().getName() + ".SendMail End!");
    }
}

