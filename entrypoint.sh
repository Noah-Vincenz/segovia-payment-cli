#!/bin/bash

mvn clean compile assembly:single && java -cp target/payment-cli-1.0-SNAPSHOT.jar com.segovia.payment.cli.commands.Commands
