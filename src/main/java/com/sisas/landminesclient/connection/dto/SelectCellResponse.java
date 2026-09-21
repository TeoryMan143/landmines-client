package com.sisas.landminesclient.connection.dto;

import com.sisas.landminesclient.model.Cell;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class SelectCellResponse {
  private final Cell[][] board;
  private final boolean win;
  private final boolean gameEnd;
}
