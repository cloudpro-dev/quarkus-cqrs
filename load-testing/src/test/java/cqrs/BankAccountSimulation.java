package cqrs;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import cqrs.util.InjectionProfile;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulation of users performing various bank related activities.
 */
public class BankAccountSimulation extends Simulation {

    private static final Logger LOG = LoggerFactory.getLogger(BankAccountSimulation.class);

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
                        .post("http://localhost:9020/api/v1/bank")
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
                        .post("http://localhost:9020/api/v1/bank/email/#{accountId}")
                        .body(StringBody("{\"email\":\"#{newEmail}\"}"))
                        .check(status().is(204))
                )
            );

    ChainBuilder updateAddress =
            exec(
                feed(addressFeeder)
                    .exec(
                        http("Update Address")
                            .post("http://localhost:9020/api/v1/bank/address/#{accountId}")
                            .body(StringBody("{\"address\":\"#{newAddress}\"}"))
                            .check(status().is(204))
                    )
            );

    ChainBuilder deposit =
            exec(
                http("Deposit")
                    .post("http://localhost:9020/api/v1/bank/deposit/#{accountId}")
                    .body(StringBody("{\"amount\": 500.00}"))
                    .check(status().is(204))
            );

    ChainBuilder withdrawal =
            exec(
                http("Withdrawal")
                    .post("http://localhost:9020/api/v1/bank/withdraw/#{accountId}")
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
                        .get("http://localhost:9010/api/v1/bank/#{nextAccountId}")
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
                .get("http://localhost:9010/api/v1/bank/balance?page=#{maxPageNumber}")
                .check(status().is(200))
        );

    HttpProtocolBuilder protocol =
            http.baseUrl("http://localhost/")
                .acceptHeader("application/json");

    ScenarioBuilder commands = scenario("Commands")
            .exec(createAccount, updateEmail, updateAddress, deposit, withdrawal);

    ScenarioBuilder queries = scenario("Queries")
            .exec(getBalance, getBankAccount);

    OpenInjectionStep smokeTest = InjectionProfile.smokeProfile();
    OpenInjectionStep[] maxTpsTest = InjectionProfile.stepWiseProfile(10.0, 5.0, 10L, 4);

    OpenInjectionStep[] spikeTest = InjectionProfile.spikeProfile(10.0, 10L, 50, 10);

    OpenInjectionStep[] targetTpsTest = InjectionProfile.targetTpsProfile(10.0, 5, 60);

    {
        setUp(
            // commands.injectOpen(smokeTest),
            // queries.injectOpen(smokeTest)

            // commands.injectOpen(InjectionProfile.stepWiseProfile(10.0, 1.0, 150L, 12)),
            // queries.injectOpen(InjectionProfile.stepWiseProfile(10.0, 3.0, 150L, 12))

            // commands.injectOpen(spikeTest),
            // queries.injectOpen(spikeTest)

            commands.injectOpen(constantUsersPerSec(15.0).during(30)),
            queries.injectOpen(constantUsersPerSec(20.0).during(30))
        ).protocols(protocol);
    }

}

