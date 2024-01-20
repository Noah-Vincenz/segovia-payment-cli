package com.segovia.payment.cli.service;

import java.io.FileWriter;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CsvFileWriterService {


  private CsvFileWriterService() {
  }

  /**
   * Write to a CSV file using a given {@link FileWriter} instance.
   */
  public static void writeToCsvFile(FileWriter fileWriter, String lineToWrite) throws IOException {
    StringBuilder line = new StringBuilder();
    line.append(lineToWrite);
    line.append("\n");
    fileWriter.write(line.toString());
  }
}
