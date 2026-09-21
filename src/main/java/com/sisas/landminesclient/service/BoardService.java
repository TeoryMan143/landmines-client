package com.sisas.landminesclient.service;

import com.sisas.landminesclient.connection.ServerConnector;
import com.sisas.landminesclient.model.BoardGame;
import com.sisas.landminesclient.model.Cell;

import java.util.Scanner;

public class BoardService {

  private final ServerConnector serverConnector;
  private final BoardGame boardGame;

  public BoardService(ServerConnector serverConnector, BoardGame boardGame) {
    this.serverConnector = serverConnector;
    this.boardGame = boardGame;
  }

  public Cell[][] getBoard() {
    return boardGame.getBoard();
  }

  public void refreshBoard() {
    updateBoard(serverConnector.getBoard());
  }

  public void initBoard(int rows, int columns, int mines) {
    updateBoard(serverConnector.initBoard(rows, columns, mines));
    boardGame.setMines(mines);
  }

  public void selectCell(int row, int column) {
    updateBoard(serverConnector.selectCell(row, column));
  }

  public void showAllBoard() {
    updateBoard(serverConnector.showAllBoard());
  }

  public void markCell(int row, int column) {
    boardGame.markCell(row, column);
  }

  public void printBoard() {
    boardGame.printBoard();
  }

  public void interact() {
    refreshBoard();
    try (Scanner scanner = new Scanner(System.in)) {
      boolean playing = true;
      while (playing) {
        if (boardGame.getBoard() == null) {
          throw new IllegalStateException("The server returned no board");
        }

        int rows = boardGame.getBoard().length;
        int columns = boardGame.getBoard()[0].length;
        System.out.println("LandMines on the table: " + boardGame.getMines());
        printBoard();
        System.out.println("select a cell (i,j) between 0 and " + (rows - 1) + "," + (columns - 1)
            + " to play, or (-1,-1) to exit");
        System.out.println("you have " + boardGame.getMines() + " mines to avoid");
        System.out.println("use the format: <operation> <i> <j>");
        System.out.println("operation 1: select cell, operation 2: mark/unmark cell");
        System.out.println("type u to update the board, or -1 -1 to exit");

        String command = scanner.next();
        if (command.equalsIgnoreCase("u")) {
          refreshBoard();
          continue;
        }

        int operation;
        try {
          operation = Integer.parseInt(command);
        } catch (NumberFormatException e) {
          System.out.println("Unknown operation: " + command);
          continue;
        }
        int row = scanner.nextInt();
        int column = scanner.nextInt();
        if (row < 0 || column < 0) {
          playing = false;
          continue;
        }

        try {
          if (operation == 2) {
            markCell(row, column);
          } else if (operation == 1) {
            selectCell(row, column);
          } else {
            System.out.println("Unknown operation: " + operation);
          }
        } catch (RuntimeException e) {
          System.out.println(e.getMessage());
          playing = false;
        }
      }

      showAllBoard();
      printBoard();
      System.out.println("exit");
    }
  }

  private void updateBoard(Cell[][] board) {
    if (board == null) {
      throw new IllegalStateException("The server returned no board");
    }
    boardGame.setBoard(board);
  }
}
