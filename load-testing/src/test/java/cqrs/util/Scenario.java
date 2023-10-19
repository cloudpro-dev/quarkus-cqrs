package cqrs.util;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static cqrs.util.ExternalPropertiesConfig.eventStoreUrl;
import static cqrs.util.ExternalPropertiesConfig.viewStoreUrl;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class Scenario {

    private static final Logger LOG = LoggerFactory.getLogger(Scenario.class);

    FeederBuilder<String> accountFeeder = csv("data/accounts.csv").random();
    FeederBuilder<String> emailFeeder = csv("data/emails.csv").random();
    FeederBuilder<String> addressFeeder = csv("data/addresses.csv").random();

    ConcurrentLinkedDeque<String> queue = new ConcurrentLinkedDeque<>();

    AtomicInteger accountCounter = new AtomicInteger();

    ChainBuilder createAccount =
            exec(
                    feed(accountFeeder)
                            .exec(
                                    http("Create")
                                            .post(eventStoreUrl + "/api/v1/bank")
                                            .body(StringBody("{\"email\":\"#{email}\", \"userName\":\"#{username}\", \"address\":\"#{address}\"}"))
                                            .check(status().is(201))
                                            .check(bodyString().exists().saveAs("accountId"))
                            )
                            .exec(s -> {
                                if(s.contains("accountId")) {
                                    String accountId = s.getString("accountId");
                                    if(accountId != null) {
                                        // put to queue
                                        queue.offer(accountId);
                                        accountCounter.getAndIncrement();
                                    }
                                }
                                return s;
                            })
            );

    ChainBuilder updateEmail =
            exec(
                    feed(emailFeeder)
                            .exec(
                                    http("Update Email")
                                            .post(eventStoreUrl + "/api/v1/bank/email/#{accountId}")
                                            .body(StringBody("{\"email\":\"#{newEmail}\"}"))
                                            .check(status().is(204))
                            )
            );

    ChainBuilder updateAddress =
            exec(
                    feed(addressFeeder)
                            .exec(
                                    http("Update Address")
                                            .post(eventStoreUrl + "/api/v1/bank/address/#{accountId}")
                                            .body(StringBody("{\"address\":\"#{newAddress}\"}"))
                                            .check(status().is(204))
                            )
            );

    ChainBuilder deposit =
            exec(
                    http("Deposit")
                            .post(eventStoreUrl + "/api/v1/bank/deposit/#{accountId}")
                            .body(StringBody("{\"amount\": 500.00}"))
                            .check(status().is(204))
            );

    ChainBuilder withdrawal =
            exec(
                    http("Withdrawal")
                            .post(eventStoreUrl + "/api/v1/bank/withdraw/#{accountId}")
                            .body(StringBody("{\"amount\": 100.00}"))
                            .check(status().is(204))
            );

    ChainBuilder getBankAccount =
            exec(s -> {
                String accountId = queue.poll();
                LOG.debug("Next account ID from queue is {}", accountId);
                return s.set("nextAccountId", accountId);
            })
                    .doIf(s -> s.contains("nextAccountId") && s.get("nextAccountId") != null).then(
                            exec(
                                    http("Get Bank Account")
                                            .get(viewStoreUrl + "/api/v1/bank/#{nextAccountId}")
                                            .check(status().in(200, 404)) // 404 is not an error
                            )
                    );

    ChainBuilder getBalance =
            exec(s -> {
                Integer total = accountCounter.get();
                Integer maxPage = total / 5;
                LOG.debug("Max page number is {}", maxPage);
                return s.set("maxPageNumber", maxPage);
            })
                    .exec(
                            http("Get Balance")
                                    .get(viewStoreUrl + "/api/v1/bank/balance?page=#{maxPageNumber}")
                                    .check(status().is(200))
                    );

    // redundant for now
    HttpProtocolBuilder protocol =
            http.baseUrl("http://localhost/") // not used
                    .acceptHeader("application/json");

    /**
     * Get the commands for the event-store application
     * @return list of commands
     */
    public ScenarioBuilder getEventStoreCommands() {
        return scenario("Commands")
                .exec(createAccount, updateEmail, updateAddress, deposit, withdrawal);
    }

    /**
     * Get the commands for the view-store application
     * @return list of commands
     */
    public ScenarioBuilder getViewStoreCommands() {
        return scenario("Queries")
                .exec(getBalance, getBankAccount);
    }

}
