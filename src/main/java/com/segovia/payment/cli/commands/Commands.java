package com.segovia.payment.cli.commands;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.asynchttpclient.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.segovia.payment.cli.mapper.StatusResponseMapper;
import com.segovia.payment.cli.model.AccessToken;
import com.segovia.payment.cli.model.FinalStatusEnum;
import com.segovia.payment.cli.model.request.AuthRequest;
import com.segovia.payment.cli.model.request.PayRequest;
import com.segovia.payment.cli.model.response.FinalStatusResponse;
import com.segovia.payment.cli.model.response.PayResponse;
import com.segovia.payment.cli.model.response.StatusResponse;
import com.segovia.payment.cli.service.AuthRequestService;
import com.segovia.payment.cli.service.CsvFileReaderService;
import com.segovia.payment.cli.service.CsvFileWriterService;
import com.segovia.payment.cli.service.PayRequestService;
import com.segovia.payment.cli.service.StatusRequestService;

import static com.segovia.payment.cli.constants.Constants.CSV_FILE_HEADER;
import static com.segovia.payment.cli.constants.Constants.DEFAULT_ACCOUNT;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter.ArgumentList;
import jline.console.completer.ArgumentCompleter.WhitespaceArgumentDelimiter;
import jline.internal.Configuration;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;
import picocli.shell.jline2.PicocliJLineCompleter;

@Slf4j
public class Commands {

    @Command(name = "", description = "Interactive shell",
            footer = {"", "Press Ctrl-C or use exit command to exit."},
            subcommands = {PaymentRequestCommand.class, ExitShell.class})
    public static class CliCommands implements Runnable {
        final ConsoleReader reader;
        final PrintWriter out;

        @Spec
        private CommandSpec spec;

        public CliCommands(ConsoleReader reader) {
            this.reader = reader;
            out = new PrintWriter(reader.getOutput());
        }

        public void run() {
            out.println(spec.commandLine().getUsageMessage());
        }
    }

    /**
     * Command to execute a payment request via the Segovia API.
     */
    @Command(name = "pay", aliases = "payment-request", mixinStandardHelpOptions = true,
            description = "Sends a payment request via the Segovia API using the input CSV file from the specified path", version = "1.0")
    public static class PaymentRequestCommand implements Callable<Void> {
        @ParentCommand CliCommands parent;
        @CommandLine.Option(
                names = {"-I", "--input-file-path"},
                description = "The local path (starting from the root of this project) to the file containing input payment data to be processed",
                required = true)
        private Path csvFilePath;
        private static final String OUTPUT_FILE_PATH = "./src/output.csv";

        public Void call() throws IOException, ExecutionException, InterruptedException, TimeoutException {
            log.info(CSV_FILE_HEADER);
            System.out.println(CSV_FILE_HEADER);
            ObjectMapper mapper = new ObjectMapper();
            String token = AuthRequestService.sendAuthRequest(AuthRequest.builder()
                                                                         .account(DEFAULT_ACCOUNT)
                                                                         .build())
                                             .getToken();
            AccessToken accessToken = AccessToken.builder()
                                                 .expiryDateTime(LocalDateTime.now().plusMinutes(5))
                                                 .token(token)
                                                 .build();
            FileWriter fileWriter = new FileWriter(OUTPUT_FILE_PATH);
            List<PayRequest> payRequestsToExecute = CsvFileReaderService.readCsvFile(Files.newBufferedReader(csvFilePath));
            List<FinalStatusResponse> finalStatusResponses = new ArrayList<>();
            for (PayRequest request : payRequestsToExecute) {
                Response payResponse = PayRequestService.sendPayRequest(request, accessToken);
                log.info("Received payResponse with status code [{}] and body [{}]", payResponse.getStatusCode(), payResponse.getResponseBody());
                var finalStatusResponse = FinalStatusResponse.builder()
                                                             .id(request.getReference())
                                                             .status(FinalStatusEnum.UNKNOWN.getValue()).build();
                if (payResponse.getStatusCode() == 200) {
                    PayResponse payResponseMapped = mapper.readValue(payResponse.getResponseBody(), PayResponse.class);
                    log.info("payResponseMapped with status [{}] and conversation id [{}]", payResponseMapped.getStatus(), payResponseMapped.getConversationID());
                    if (payResponseMapped.getStatus() == 0) { // succeeded
                        finalStatusResponse.setServerGeneratedId(payResponseMapped.getConversationID());
                        finalStatusResponse.setDetails(payResponseMapped.getMessage());
                        finalStatusResponses.add(finalStatusResponse); // add status and fee from transaction response
                    } else { // failed to do payment
                        finalStatusResponse.setServerGeneratedId(payResponseMapped.getConversationID());
                        finalStatusResponse.setStatus(FinalStatusEnum.FAILED.getValue());
                        finalStatusResponse.setDetails(payResponseMapped.getMessage());
                        log.info("FAILED PAYMENT [{}]", finalStatusResponse);
                        System.out.println(finalStatusResponse);
                        CsvFileWriterService.writeToCsvFile(fileWriter, finalStatusResponse.toString());
                    }
                } else { // failed request
                    finalStatusResponse.setStatus(FinalStatusEnum.FAILED.getValue());
                    finalStatusResponse.setDetails("Server was unable to process the request");
                    log.info("FAILED REQUEST [{}]", finalStatusResponse);
                    System.out.println(finalStatusResponse);
                    CsvFileWriterService.writeToCsvFile(fileWriter, finalStatusResponse.toString());
                }
            }
            while (!finalStatusResponses.isEmpty()) {
                List<FinalStatusResponse> tempList = finalStatusResponses.stream()
                                                                         .filter(it -> !Objects.equals(it.getStatus(), FinalStatusEnum.SUCCEEDED.getValue()))
                                                                         .toList();
                log.info("Trying to send status requests for {} payments", finalStatusResponses.size());
                for (FinalStatusResponse resp : tempList) {
                    log.info("Sending status request for payment with id [{}] and server generateidId [{}]", resp.getId(), resp.getServerGeneratedId());
                    try {
                        Response statusResponse = StatusRequestService.sendStatusRequest(resp.getServerGeneratedId(), accessToken);
                        log.info("Received statusResponse with status code [{}] and body [{}]", statusResponse.getStatusCode(), statusResponse.getResponseBody());
                        if (statusResponse.getStatusCode() == 200) {
                            StatusResponse statusResponseMapped = mapper.readValue(statusResponse.getResponseBody(), StatusResponse.class);
                            log.info("statusResponseMapped with status [{}]", statusResponseMapped.getStatus());
                            if (statusResponseMapped.getStatus() == 0) {
                                resp.setStatus(StatusResponseMapper.mapStatusResponse(statusResponseMapped.getStatus()));
                                resp.setFee(statusResponseMapped.getFee());
                                resp.setDetails(statusResponseMapped.getMessage());
                                finalStatusResponses.remove(resp);
                                log.info("SUCCESSFUL PAYMENT [{}]", resp);
                                System.out.println(resp);
                                CsvFileWriterService.writeToCsvFile(fileWriter, resp.toString());
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException | TimeoutException | JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                if (!finalStatusResponses.isEmpty()) Thread.sleep(60000); // try again in 1 minute if there are some leftover elements
            }
            fileWriter.close();
            return null;
        }
    }

    /**
     * Command that exits interactive shell.
     */
    @Command(name = "exit", mixinStandardHelpOptions = true,
            description = "Exit interactive shell", version = "1.0")
    public static class ExitShell implements Callable<Void> {

        @ParentCommand CliCommands parent;

        public Void call() throws Exception {
            System.exit(0);
            return null;
        }
    }

    public static void main(String[] args) {

        // JLine 2 does not detect some terminal as not ANSI compatible (e.g  Eclipse Console)
        // See : https://github.com/jline/jline2/issues/185
        // This is an optional workaround which allow to use picocli heuristic instead
        if (!CommandLine.Help.Ansi.AUTO.enabled() && //
            Configuration.getString(TerminalFactory.JLINE_TERMINAL, TerminalFactory.AUTO).equalsIgnoreCase(TerminalFactory.AUTO)) {
            TerminalFactory.configure(TerminalFactory.Type.NONE);
        }

        try {
            ConsoleReader reader = new ConsoleReader();
            IFactory factory = new CustomFactory(new InteractiveParameterConsumer(reader));

            // set up the completion
            CliCommands commands = new CliCommands(reader);
            CommandLine cmd = new CommandLine(commands, factory);
            reader.addCompleter(new PicocliJLineCompleter(cmd.getCommandSpec()));

            // start the shell and process input until the user quits with Ctrl-D
            String line;
            while ((line = reader.readLine("prompt> ")) != null) {
                ArgumentList list = new WhitespaceArgumentDelimiter()
                        .delimit(line, line.length());
                new CommandLine(commands, factory)
                        .execute(list.getArguments());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
