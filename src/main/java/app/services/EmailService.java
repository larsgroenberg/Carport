package app.services;

import app.entities.User;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import java.io.IOException;

/**
 * Klasse der bliver brugt til at sende emails
 */
public class EmailService {

    public static void sendEmail(User currentUser) throws IOException {
        // Erstat xyx@gmail.com med din egen email, som er afsender
        Email from = new Email("auto.mail.sender.service@gmail.com");

        from.setName("Johannes Fog Byggemarked");

        Mail mail = new Mail();
        mail.setFrom(from);

        String API_KEY = System.getenv("SENDGRID_API_KEY");

        Personalization personalization = new Personalization();
        String name = currentUser.getName();
        String email = currentUser.getEmail();
        String password = currentUser.getPassword();

        personalization.addTo(new Email(currentUser.getEmail()));
        personalization.addDynamicTemplateData("name", name);
        personalization.addDynamicTemplateData("email", email);
        personalization.addDynamicTemplateData("password", password);
        personalization.addDynamicTemplateData("link", "http://localhost:7070/customersitelogin");
        mail.addPersonalization(personalization);

        mail.addCategory("carportapp");

        SendGrid sg = new SendGrid(API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");

            mail.templateId = "d-8202a2164f2a479aa9e6b2ad690bad4c";
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            System.out.println("Error sending mail");
            throw ex;
        }
    }
}
