package com.sisas.landminesclient;

import com.sisas.landminesclient.connection.ServerConnector;
import com.sisas.landminesclient.model.BoardGame;
import com.sisas.landminesclient.service.BoardService;

import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException {
    BoardService boardService = new BoardService(
        new ServerConnector("localhost", 12345),
        new BoardGame());
    boardService.interact();
  }
}