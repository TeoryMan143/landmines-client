package com.sisas.landminesclient.connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sisas.landminesclient.connection.dto.Request;
import com.sisas.landminesclient.connection.dto.Response;
import com.sisas.landminesclient.connection.dto.SelectCellResponse;
import com.sisas.landminesclient.model.Cell;

import java.io.*;
import java.net.Socket;
import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

public class ServerConnector {
  private final String host;
  private final int port;
  private final Gson gson;
  private final Type boardType;

  public ServerConnector(String host, int port) {
    this.host = host;
    this.port = port;
    this.gson = new GsonBuilder().create();
    boardType = new TypeToken<Cell[][]>() {
    }.getType();
  }

  public Cell[][] getBoard() {
    try (Socket hostSocket = new Socket(host, port); BufferedReader reader = new BufferedReader(new InputStreamReader(hostSocket.getInputStream())); BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(hostSocket.getOutputStream()))) {

      Request req = new Request("GET_BOARD", null);
      String json = gson.toJson(req);
      writer.write(json);
      writer.newLine();
      writer.flush();

      String line = reader.readLine();
      Response res = gson.fromJson(line, Response.class);
      Object boardData = res.data.get("board");
      return gson.fromJson(gson.toJson(boardData), boardType);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }


  public SelectCellResponse selectCell(int i, int j) {
    try (Socket hostSocket = new Socket(host, port); BufferedReader reader = new BufferedReader(new InputStreamReader(hostSocket.getInputStream())); BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(hostSocket.getOutputStream()))) {
      Request req = new Request("SELECT_CELL", Map.of("i", String.valueOf(i), "j", String.valueOf(j)));
      String json = gson.toJson(req);
      writer.write(json);
      writer.newLine();
      writer.flush();

      String line = reader.readLine();
      Response res = gson.fromJson(line, Response.class);
      Object boardData = res.data.get("board");
      Cell[][] board = gson.fromJson(gson.toJson(boardData), boardType);
      boolean win = Boolean.TRUE.equals(res.data.get("win"));
      boolean gameEnd = Boolean.TRUE.equals(res.data.get("gameEnd"));
      return new SelectCellResponse(board, win, gameEnd);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public Cell[][] markCell(int i, int j) {
    try (Socket hostSocket = new Socket(host, port); BufferedReader reader = new BufferedReader(new InputStreamReader(hostSocket.getInputStream())); BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(hostSocket.getOutputStream()))) {
      Request req = new Request("MARK_CELL", Map.of("i", String.valueOf(i), "j", String.valueOf(j)));
      String json = gson.toJson(req);
      writer.write(json);
      writer.newLine();
      writer.flush();

      String line = reader.readLine();
      Response res = gson.fromJson(line, Response.class);
      Object boardData = res.data.get("board");
      return gson.fromJson(gson.toJson(boardData), boardType);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public Cell[][] showAllBoard() {
    try (Socket hostSocket = new Socket(host, port); BufferedReader reader = new BufferedReader(new InputStreamReader(hostSocket.getInputStream())); BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(hostSocket.getOutputStream()))) {
      Request req = new Request("SOW_ALL", null);
      String json = gson.toJson(req);
      writer.write(json);
      writer.newLine();
      writer.flush();

      String line = reader.readLine();
      Response res = gson.fromJson(line, Response.class);
      Object boardData = res.data.get("board");
      return gson.fromJson(gson.toJson(boardData), boardType);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public Cell[][] initBoard(int n, int m, int mines) {
    try (Socket hostSocket = new Socket(host, port); BufferedReader reader = new BufferedReader(new InputStreamReader(hostSocket.getInputStream())); BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(hostSocket.getOutputStream()))) {
      Request req = new Request("INIT_GAME", Map.of("n", String.valueOf(n), "m", String.valueOf(m), "minas", String.valueOf(mines)));
      String json = gson.toJson(req);
      writer.write(json);
      writer.newLine();
      writer.flush();

      Response res = gson.fromJson(reader.readLine(), Response.class);
      Object boardData = res.data.get("board");
      return gson.fromJson(gson.toJson(boardData), boardType);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
