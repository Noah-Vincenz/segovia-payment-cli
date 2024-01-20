FROM openjdk:17
COPY target/payment-cli-1.0-SNAPSHOT.jar payment-cli-1.0-SNAPSHOT.jar
ENTRYPOINT java -cp payment-cli-1.0-SNAPSHOT.jar com.segovia.payment.cli.commands.Commands
