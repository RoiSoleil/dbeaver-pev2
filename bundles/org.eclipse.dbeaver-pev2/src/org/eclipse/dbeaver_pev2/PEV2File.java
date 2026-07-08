package org.eclipse.dbeaver_pev2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public final class PEV2File {

  private static final String SEPARATOR = "=".repeat(50);

  private PEV2File() {
  }

  public static void write(OutputStream output, String sql, String plan) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
      writer.write(sql.trim());
      writer.newLine();
      writer.newLine();
      writer.write(SEPARATOR);
      writer.newLine();
      writer.newLine();
      writer.write(plan.trim());
    }
  }

  public static PEV2Content read(InputStream input) throws IOException {
    StringBuilder content = new StringBuilder();
    StringBuilder sql = new StringBuilder();
    StringBuilder plan = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(input, StandardCharsets.UTF_8))) {
      String line;
      boolean inPlan = false;
      boolean separatorFound = false;
      while ((line = reader.readLine()) != null) {
        content.append(line).append(System.lineSeparator());
        if (!inPlan && line.equals(SEPARATOR)) {
          inPlan = true;
          separatorFound = true;
          continue;
        }
        if (inPlan) {
          plan.append(line).append(System.lineSeparator());
        } else {
          sql.append(line).append(System.lineSeparator());
        }
      }
      if (!separatorFound) {
        throw new IOException("Invalid PEV2 file: separator not found");
      }
    }
    return new PEV2Content(content.toString(),
        sql.toString(),
        plan.toString());
  }

  public record PEV2Content(String content, String sql, String plan) {
  }
}