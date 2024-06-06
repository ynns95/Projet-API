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

    public static void main(String[] args) {
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

            // Créer un client ChatGPT
            ChatGPTClient chatGPTClient = new ChatGPTClient();

            // Envoyer une requête à ChatGPT et obtenir la réponse
            String chatGPTResponse = chatGPTClient.queryChatGPT();

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

            // Indiquer que l'authentification est réussie
            System.out.println("\n" + ANSI_GREEN + "Authentication CAS successful for user: " + userAttributes.getMail() + ANSI_RESET);

            // Afficher la réponse de ChatGPT
            System.out.println("\n" + ANSI_ORANGE + "Connection with ChatGPT..." + ANSI_ORANGE);
            System.out.println("\n" + ANSI_GREEN + "Connection successful with ChatGPT, response: " + ANSI_GREEN + chatGPTResponse);

        } catch (Exception e) {

            // En cas d'échec de l'authentification
            System.out.println("\n" + ANSI_RED +"Authentication failed" + ANSI_RED);
        }
    }
}

