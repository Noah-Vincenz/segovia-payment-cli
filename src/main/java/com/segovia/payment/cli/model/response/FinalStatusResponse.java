package com.segovia.payment.cli.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinalStatusResponse {
    String id;
    String serverGeneratedId;
    String status;
    String fee;
    String details;

    @Override
    public String toString() {
        return id +
               "," +
               (serverGeneratedId == null ? "" : serverGeneratedId) +
               "," +
               (status == null ? "" : status) +
               "," +
               (fee == null ? "" : fee) +
               "," +
               (details == null ? "" : details);
    }
}
