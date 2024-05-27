package app;

import app.config.ThymeleafConfig;
import app.controllers.AdminController;
import app.controllers.OrderController;
import app.controllers.UserController;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

public class Main
{
    public static void main(String[] args) {

        // Initialiserer Javalin og Jetty webserver
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
        }).start(7070);

        // Routing
        OrderController.addRoutes(app);
        UserController.addRoutes(app);
        AdminController.addRoutes(app);
    }
}

