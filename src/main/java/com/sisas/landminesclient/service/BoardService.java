package com.sisas.landminesclient.service;

import com.sisas.landminesclient.connection.ServerConnector;
import com.sisas.landminesclient.connection.dto.SelectCellResponse;
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

  public SelectCellResponse selectCell(int row, int column) {
    SelectCellResponse response = serverConnector.selectCell(row, column);
    updateBoard(response.getBoard());
    return response;
  }

  public void showAllBoard() {
    updateBoard(serverConnector.showAllBoard());
  }

  public void markCell(int row, int column) {
    updateBoard(serverConnector.markCell(row, column));
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
        System.out.println("type i <rows> <columns> <mines> to reset the board");
        System.out.println("type s to reveal everything and end the game");

        String command = scanner.next();
        if (command.equalsIgnoreCase("u")) {
          refreshBoard();
          continue;
        }
        if (command.equalsIgnoreCase("i")) {
          int newRows = scanner.nextInt();
          int newColumns = scanner.nextInt();
          int newMines = scanner.nextInt();
          initBoard(newRows, newColumns, newMines);
          continue;
        }
        if (command.equalsIgnoreCase("s")) {
          showAllBoard();
          printBoard();
          System.out.println("Board revealed. Game ended.");
          playing = false;
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
            SelectCellResponse response = selectCell(row, column);
            if (response.isGameEnd()) {
              if (response.isWin()) {
                System.out.println("You win, congratulations!");
              } else {
                System.out.println("Game over. You hit a mine.");
              }
              playing = false;
            }
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
