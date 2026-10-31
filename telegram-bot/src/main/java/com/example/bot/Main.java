package com.example.bot;

import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Entry point for the PapeRide Telegram bot.
 *
 * Reads credentials from environment variables or .env file:
 *   BOT_USERNAME           — the bot's Telegram username (without @)
 *   BOT_TOKEN              — the token you received from @BotFather
 *   WEBSITE_URL            — base URL of the PapeRide website, e.g. http://localhost:3000
 *   WEBSITE_INTERNAL_TOKEN — must equal TELEGRAM_INTERNAL_TOKEN on the website
 *   PORT                   — optional HTTP port for keepalive/ping health checks (defaults to 8080)
 */
public class Main {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        String botUsername = getEnv("BOT_USERNAME", dotenv);
        String botToken    = getEnv("BOT_TOKEN", dotenv);

        if (botUsername == null || botUsername.isBlank()) {
            System.err.println("ERROR: BOT_USERNAME is not set");
            System.exit(1);
        }
        if (botToken == null || botToken.isBlank()) {
            System.err.println("ERROR: BOT_TOKEN is not set");
            System.exit(1);
        }

        String websiteUrl   = getEnv("WEBSITE_URL", dotenv);
        String websiteToken = getEnv("WEBSITE_INTERNAL_TOKEN", dotenv);

        if (websiteUrl == null || websiteUrl.isBlank()) {
            System.err.println("ERROR: WEBSITE_URL is not set");
            System.exit(1);
        }
        if (websiteToken == null || websiteToken.isBlank()) {
            System.err.println("ERROR: WEBSITE_INTERNAL_TOKEN is not set");
            System.exit(1);
        }

        // Start lightweight HTTP keep-alive server for Render and external pingers
        startHealthServer(getEnv("PORT", dotenv));

        WebsiteClient website = new WebsiteClient(websiteUrl, websiteToken);
        System.out.println("Waiting for website at " + websiteUrl + " to respond...");
        int attempts = 0;
        while (true) {
            try {
                website.checkHealth();
                System.out.println("✅ Website reachable at " + websiteUrl);
                break;
            } catch (Exception e) {
                attempts++;
                System.err.println("Website not reachable yet (" + e.getMessage() + "), retrying in 10s (attempt " + attempts + ")...");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {}
            }
        }

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new PapeRideBot(botUsername, botToken, website));
            System.out.println("✅ PapeRideBot is up and running!");
        } catch (TelegramApiException e) {
            System.err.println("Failed to start the bot: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String getEnv(String key, Dotenv dotenv) {
        String val = System.getenv(key);
        if (val != null && !val.isBlank()) {
            return val;
        }
        return dotenv.get(key);
    }

    private static void startHealthServer(String portStr) {
        int port = 8080;
        if (portStr != null && !portStr.isBlank()) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException ignored) {}
        }
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", exchange -> {
                if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, -1);
                } else {
                    byte[] response = "OK - PapeRide Telegram Bot is alive\n".getBytes();
                    exchange.sendResponseHeaders(200, response.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response);
                    }
                }
            });
            server.setExecutor(null);
            server.start();
            System.out.println("✅ Health server listening on port " + port);
        } catch (IOException e) {
            System.err.println("Warning: could not start health HTTP server: " + e.getMessage());
        }
    }
}
