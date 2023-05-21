package cqrs;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulation of users performing various bank related activities.
 */
public class BankAccountSimulation extends Simulation {

    FeederBuilder<String> accountFeeder = csv("accounts.csv").random();
    FeederBuilder<String> emailFeeder = csv("emails.csv").random();
    FeederBuilder<String> addressFeeder = csv("addresses.csv").random();

    ChainBuilder createAccount =
            exec(
                feed(accountFeeder)
                .exec(
                    http("Create")
                        .post("api/v1/bank")
                        .body(StringBody("{\"email\":\"#{email}\", \"userName\":\"#{username}\", \"address\":\"#{address}\"}"))
                        .check(status().is(201))
                        .check(bodyString().exists().saveAs("accountId"))
                )
            );

    ChainBuilder updateEmail =
            exec(
                feed(emailFeeder)
                .exec(
                    http("Update Email")
                        .post("api/v1/bank/email/#{accountId}")
                        .body(StringBody("{\"email\":\"#{newEmail}\"}"))
                        .check(status().is(204))
                )
            );

    ChainBuilder updateAddress =
            exec(
                feed(addressFeeder)
                    .exec(
                        http("Update Address")
                            .post("api/v1/bank/address/#{accountId}")
                            .body(StringBody("{\"address\":\"#{newAddress}\"}"))
                            .check(status().is(204))
                    )
            );

    ChainBuilder deposit =
            exec(
                http("Deposit")
                    .post("api/v1/bank/deposit/#{accountId}")
                    .body(StringBody("{\"amount\": 500.00}"))
                    .check(status().is(204))
            );

    ChainBuilder withdrawal =
            exec(
                http("Withdrawal")
                    .post("api/v1/bank/withdraw/#{accountId}")
                    .body(StringBody("{\"amount\": 100.00}"))
                    .check(status().is(204))
            );


    HttpProtocolBuilder httpProtocol =
            http.baseUrl("http://localhost:9020/")
                .acceptHeader("application/json");

    ScenarioBuilder users = scenario("Users").exec(createAccount, updateEmail, updateAddress, deposit, withdrawal);

    {
        setUp(
            // users.injectOpen(atOnceUsers(1)),
            users.injectOpen(rampUsers(100).during(10))
        ).protocols(httpProtocol);
    }

}
