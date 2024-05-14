package app.services;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Klasse der bliver brugt til at sende emails, vha. SMTP (Simple Mail Transfer Protocol)
 */
public class EmailService {
    private final String username = System.getenv("Email_Name");
    private final String password = System.getenv("Email_Password");

    private final Properties properties;

    /**
     * EmailService Constructor
     * Laver en instants a constructor med SMTP konfiguration
     */
    public EmailService(){
        properties = new Properties();
        properties.put("mail.smtp.auth", "true"); //gør at authentication er nødvendigt før email kan sendes
        properties.put("mail.smtp.starttls.enable", "true"); //STARTTLS opgraderer en usikker forbindelse til en sikker forbindelse
        properties.put("mail.smtp.host", "smtp.gmail.com"); // Fortæller at vi skal bruge gmail
        properties.put("mail.smtp.port", "587"); // 587 port bliver brugt for forbindelser der er sikret med STARTTLS
    }

    public void sendEmail(String toEmail, String subject, String body) {
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("Email sent successfully.");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
