package cqrs.util;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class Scenario {

    private static final FeederBuilder<String> accountFeeder = csv("data/accounts.csv").random();
    private static final FeederBuilder<String> emailFeeder = csv("data/emails.csv").random();
    private static final FeederBuilder<String> addressFeeder = csv("data/addresses.csv").random();

    public ScenarioBuilder getScenario() {
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

        return scenario("Users")
                .exec(createAccount, updateEmail, updateAddress, deposit, withdrawal);
    }

}
