package com.segovia.payment.cli.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.segovia.payment.cli.model.request.PayRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CsvFileReaderService {

  private CsvFileReaderService() {
  }

  /**
   * Read a CSV file using a given {@link BufferedReader} instance.
   * This reads the file line by line and splits accordingly in order to ultimately return a list of {@link PayRequest} objects.
   */
  public static List<PayRequest> readCsvFile(BufferedReader reader) {
    List<PayRequest> listOfPayRequests = new ArrayList<>();

    try {
      String line;
      reader.readLine(); // skip first line = csv header
      while ((line = reader.readLine()) != null) {
        // split by comma to get terms - this assumes we always have the same order
        String[] paymentEntry = line.split(",");
        String paymentId = paymentEntry[0];
        String recipientPhoneNumber = paymentEntry[1];
        String amount = paymentEntry[2];
        String currency = paymentEntry[3];
        // create payment request
        PayRequest payRequest = PayRequest.builder()
                                                 .amount(BigDecimal.valueOf(Double.parseDouble(amount)))
                                                 .msisdn(recipientPhoneNumber)
                                                 .currency(currency)
                                                 .reference(paymentId)
                                                 .url("some-callback-url")
                                                 .build();
        listOfPayRequests.add(payRequest);
      }
    } catch (IOException e) {
      log.error("IOException occurred while reading file", e);
    }
    return listOfPayRequests;
  }
}
