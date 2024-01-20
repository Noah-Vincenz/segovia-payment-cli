## About the application

This application presents an interactive CLI shell that allows users to send payment requests from CSV files via the Segovia payment API.
The format required for such CSV files is specified in the root `README.md`.
Once the interactive shell is running, users specify the path of the input CSV file to be used as command option to the `pay` command and, 
ultimately, this will send a payment request to the Segovia API.
Once all payment requests have been sent, the application will start polling for statuses of payments for which the Segovia API did not return a failed
response in the previous step. 
The status endpoint is polled every 60 seconds for these payments until we have received a status code `0` from the Segovia API, implying that the specific payment has been completed.
All final responses, whether successful or failed, are printed to standard output in the shell as well as written to a file named `src/output.csv`.
Logs are also being captured on a daily basis and recorded to log files located in the `C:/temp/logs` directory.
If you with to lower the log level from INFO you can head to `src/main/resources/log4j2.xml` to udpate this. 
You can also update the `filePattern` in line 10 of this file to be `${basePath}/app-%d{yyyy-MM-dd HH:mm}.log` in order for log files to be created more frequently, ie. every minute (by default the application creates a single log file each day).

## Start up the application

There are different ways of running the application

1. Using Java and the maven plugin only
- Open a Terminal and execute `./entrypoint.sh`
- Use Terminal to execute `pay -I=src/sample-input.csv` in the prompt or specify another input file
2. Using IntelliJ IDE
- Open IntelliJ IDE and locate to *src/main/java/com/segovia/payment/cli/commands/Commands.java*
- Click on one of the green arrows on the left
- Use IntelliJ Terminal to execute `pay -I=src/sample-input.csv` in the prompt or specify another input file
3. Using Docker
- Start the Docker Desktop app
- Execute `docker-compose up -d` to start up the application
- Use Docker Desktop app Terminal to execute `pay -I=src/sample-input.csv` in the prompt or specify another input file

### Running tests locally

You can easily run tests by executing `mvn test` (Note: ensure you have the Segovia test provider running at http://127.0.0.1:7902)

###

Decisions made:
1. Why did you choose the language you chose?<br>
   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;I am most competent with Java. I thought working with Java streams to filter responses would be useful.<br>
2. Why did you structure your code the way you did?<br>
   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;I wanted to have separate services with their respective responsibilities and have meaningful packages in place. I also like having model POJOs for API request and responses in place. Unfortunately using picoCLI commands required me to make these services static<br>
3. Why did you choose the libraries you did?<br>
   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;picoCLI seemed to be easy to integrate, and I love being able to present the interactive shell to the user. Jackson is great for mapping from Strings to POJOs and vice versa.<br>
4. How did you prioritize parts of the problem?<br>
   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;I prioritised getting meaningful output by using comprehensive logging and great user experience via an interactive shell and dockerisation over adding asynchronous callback listening<br>

### Future improvements

1. Instead of polling the status endpoint we could wait to receive a response via the callback URL specified in the payment requests
2. Allow users to pass in account (currently hardcoded to be 'Segovia') via command option; similarly with API key (can use Keycloak or AWS Cognito and provide an API endpoint to returns an API key on incoming request, which can then be passed in as command option)
3. We could maintain a map: (currency enum) -> (decimal places), because different currencies have different decimal places
4. Refresh token more gracefully, ie. whenever we get 403 with a certain message refresh and retry same request once. Currently I am checking whether the initial expiry time of 5 minutes has almost passed whenever making a request (not very accurate)
5. I am currently retrieving the value from my Future<Response> within 5 seconds, if there is no response within this time the application will throw a TimeoutException and terminate. It would be better to catch this and continue with the other requests and retry the failed request later on.
6. More test cases, test especially all corner cases and with large inputs
