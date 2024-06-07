package main.java;

import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_ORANGE = "\u001B[38;5;214m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_CLEAR_LINE = "\033[2K"; // Clear the entire line

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose the functionality to test:");
        System.out.println("1. Test CAS Authentication");
        System.out.println("2. Test ChatGPT Request");
        System.out.println("3. Test Both CAS Authentication and ChatGPT Request");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consommer la nouvelle ligne

        switch (choice) {
            case 1:
                testCASAuthentication();
                break;
            case 2:
                testChatGPTRequest();
                break;
            case 3:
                testBoth();
                break;
            default:
                System.out.println("Invalid choice");
        }
    }

    public static void testCASAuthentication() {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter CAS Username: ");
            String username = scanner.nextLine();

            System.out.print("Enter CAS Password: ");
            String password = scanner.nextLine();

            AuthentificationService authService = new AuthentificationService(username, password);

            // Demander un TGT
            String tgtUrl = authService.getTicketGrantingTicket();

            // Utiliser le TGT pour obtenir un ticket de service (ST)
            String serviceUrl = "https://identites.ensea.fr/cas/v1/tickets";
            String st = authService.getServiceTicket(tgtUrl, serviceUrl);

            // Valider le ticket de service (ST) et extraire les attributs utilisateur
            UserAttributes userAttributes = authService.getUserAttributes(st, serviceUrl).get();

            // Indiquer que l'authentification est réussie
            System.out.println("\n" + ANSI_GREEN + "Authentication CAS successful for user: " + ANSI_RESET + userAttributes.getMail() );

        } catch (Exception e) {
            // En cas d'échec de l'authentification
            System.out.println("\n" + ANSI_CLEAR_LINE + ANSI_RED + "Authentication failed" + ANSI_RESET);
        }
    }

    public static void testChatGPTRequest() {
        try {
            // Créer un client ChatGPT
            ChatGPTClient chatGPTClient = new ChatGPTClient();

            // Afficher le message de connexion à ChatGPT
            System.out.print("\n" + ANSI_ORANGE + "Connection with ChatGPT..." + ANSI_RESET);

            // Valider la clé API
            if (!chatGPTClient.validateAPIKey()) {
                System.out.print("\r" + ANSI_CLEAR_LINE + ANSI_RED + "Error : API Key invalid" + ANSI_RESET);
                return;
            }

            // Envoyer une requête à ChatGPT et obtenir la réponse
            String chatGPTResponse = chatGPTClient.queryChatGPT();

            // Effacer le message de connexion à ChatGPT et afficher le résultat
            System.out.print("\r" + ANSI_CLEAR_LINE + ANSI_GREEN + "Connection successful with ChatGPT, response: " + ANSI_WHITE + chatGPTResponse + ANSI_RESET);

        } catch (Exception e) {
            // En cas d'échec de la connexion à ChatGPT
            System.out.println("\r" + ANSI_CLEAR_LINE + ANSI_RED + "Failed to connect to ChatGPT" + ANSI_RESET);
        }
    }

    public static void testBoth() {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter CAS Username: ");
            String username = scanner.nextLine();

            System.out.print("Enter CAS Password: ");
            String password = scanner.nextLine();

            AuthentificationService authService = new AuthentificationService(username, password);

            // Demander un TGT
            String tgtUrl = authService.getTicketGrantingTicket();

            // Utiliser le TGT pour obtenir un ticket de service (ST)
            String serviceUrl = "https://identites.ensea.fr/cas/v1/tickets";
            String st = authService.getServiceTicket(tgtUrl, serviceUrl);

            // Valider le ticket de service (ST) et extraire les attributs utilisateur
            UserAttributes userAttributes = authService.getUserAttributes(st, serviceUrl).get();

            // Indiquer que l'authentification est réussie
            System.out.println("\n" + ANSI_GREEN + "Authentication CAS successful for user: " + ANSI_RESET + userAttributes.getMail());

            // Créer un client ChatGPT
            ChatGPTClient chatGPTClient = new ChatGPTClient();

            // Afficher le message de connexion à ChatGPT
            System.out.print("\n" + ANSI_ORANGE + "Connection with ChatGPT..." + ANSI_RESET);

            // Valider la clé API
            if (!chatGPTClient.validateAPIKey()) {
                System.out.print("\r" + ANSI_CLEAR_LINE + ANSI_RED + "Error : API Key invalid" + ANSI_RESET);
                return;
            }

            // Envoyer une requête à ChatGPT et obtenir la réponse
            String chatGPTResponse = chatGPTClient.queryChatGPT();

            // Effacer le message de connexion à ChatGPT et afficher le résultat
            System.out.print("\r" + ANSI_CLEAR_LINE + ANSI_GREEN + "Connection successful with ChatGPT, response: " + ANSI_WHITE + chatGPTResponse + ANSI_RESET);

            // Écriture dans le fichier d'historique
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/java/connection_history.txt", true))) {
                writer.write("User: " + userAttributes.getMail() + "\n");
                writer.write("Client IP Address: " + userAttributes.getClientIpAddress() + "\n");
                writer.write("Authentication Date: " + userAttributes.getAuthenticationDate() + "\n");
                writer.write("Prompt Tokens: " + chatGPTClient.getPromptTokens() + "\n");
                writer.write("Completion Tokens: " + chatGPTClient.getCompletionTokens() + "\n");
                writer.write("Total Tokens: " + chatGPTClient.getTotalTokens() + "\n");
                writer.write("=============================================\n");
            } catch (IOException e) {
                System.err.println("Failed to write to history file: " + e.getMessage());
            }

        } catch (Exception e) {
            // En cas d'échec de l'authentification ou de la connexion à ChatGPT
            System.out.println("\r" + ANSI_CLEAR_LINE + ANSI_RED + "Operation failed" + ANSI_RESET);
        }
    }
}
