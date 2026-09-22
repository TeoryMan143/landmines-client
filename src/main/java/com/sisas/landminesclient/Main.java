package com.sisas.landminesclient;

import com.sisas.landminesclient.connection.ServerConnector;
import com.sisas.landminesclient.model.BoardGame;
import com.sisas.landminesclient.service.BoardService;

import java.io.IOException;
import java.util.Scanner;

public class Main {

  public static void main(String[] args) throws IOException {
    String host = "localhost";
    int port = 12345;

    try (Scanner scanner = new Scanner(System.in)) {
      System.out.print("Server IP or hostname [localhost]: ");
      String enteredHost = scanner.nextLine().trim();
      if (!enteredHost.isEmpty()) {
        host = enteredHost;
      }

      while (true) {
        System.out.print("Server port [12345]: ");
        String enteredPort = scanner.nextLine().trim();
        if (enteredPort.isEmpty()) {
          break;
        }

        try {
          int parsedPort = Integer.parseInt(enteredPort);
          if (parsedPort < 1 || parsedPort > 65535) {
            throw new NumberFormatException();
          }
          port = parsedPort;
          break;
        } catch (NumberFormatException e) {
          System.out.println("Invalid port. Enter a number between 1 and 65535.");
        }
      }
      BoardService boardService = new BoardService(new ServerConnector(host, port), new BoardGame());
      boardService.interact(scanner);
    }
  }
}