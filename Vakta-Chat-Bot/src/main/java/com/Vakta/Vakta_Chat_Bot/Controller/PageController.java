package com.Vakta.Vakta_Chat_Bot.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    /**
     * This method handles requests for the public chat page.
     * It uses a "forward" instruction to break the circular view path error.
     *
     * @param chatbotId The ID of the chatbot (captured from the URL but not used here).
     * @return A forward path to the static chat.html file.
     */
    @GetMapping("/chat/{chatbotId}")
    public String chatPage(@PathVariable String chatbotId) {
        // --- THIS IS THE FIX ---
        // Instead of returning "chat.html", we return "forward:/chat.html".
        // This tells Spring Boot to serve the static file directly from the
        // /resources/static/ folder and stops the rendering loop.
        return "forward:/chat.html";
        // --- END OF FIX ---
    }
}