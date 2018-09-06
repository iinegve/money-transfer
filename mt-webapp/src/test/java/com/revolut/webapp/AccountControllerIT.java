package com.revolut.webapp;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.revolut.webapp.ApplicationConfiguration;
import com.revolut.webapp.JettyServer;
import com.revolut.core.account.AccountException;
import com.revolut.webapp.service.AccountDto;
import lombok.SneakyThrows;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountControllerIT {

    private static final int port = 8080;
    private static final String host = "http://localhost";

    private static JettyServer server;

    @BeforeClass
    public static void startUp() throws Exception {
        RestAssured.port = port;
        RestAssured.baseURI = host;

        server = new JettyServer(new URI(host + ":" + port), new ApplicationConfiguration());
        server.start();
    }

    @AfterClass
    public static void shutDown() throws Exception {
        server.stop();
    }

    @Test
    public void create() {
        Response response = given().post("account").andReturn();
        assertThat(response.statusCode()).isEqualTo(200);

        AccountDto createdAccount = response.as(AccountDto.class);
        assertThat(createdAccount.getBalance()).isEqualTo(0);
    }

    @Test
    public void getAccount() {
        AccountDto account = createAccount();

        AccountDto actual = given().get("account/{id}", account.getId()).as(AccountDto.class);

        assertThat(actual).isEqualTo(account);
    }

    @Test
    public void getAllAccounts() {
        AccountDto first = createAccount();
        AccountDto second = createAccount();

        AccountDto[] accounts = getAccounts();

        assertThat(accounts).contains(first, second);
    }

    @Test
    public void deposit() {
        AccountDto account = createAccount();

        Response response = depositTo(account, 1000);
        assertThat(response.statusCode()).isEqualTo(200);

        assertThat(getAccounts()).contains(new AccountDto(account.getId(), 1000));
    }

    @Test
    public void transfer() {
        AccountDto first = createAccount();
        AccountDto second = createAccount();
        depositTo(first, 1000);

        Response response = given()
            .param("to", second.getId())
            .param("amount", 200)
            .post("account/{id}/transfer", first.getId());
        assertThat(response.statusCode()).isEqualTo(200);

        assertThat(getAccounts()).contains(
            new AccountDto(first.getId(), 800),
            new AccountDto(second.getId(), 200)
        );
    }

    @Test
    public void transfer_exceedLimit() {
        AccountDto first = createAccount();
        AccountDto second = createAccount();
        depositTo(first, 1000);

        Response response = transfer(first.getId(), second.getId(), 1200);
        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(response.asString()).contains(AccountException.NOT_ENOUGH_MONEY);
    }

    @Test
    public void transfer_accountDoesNotExist() {
        AccountDto second = createAccount();

        UUID randomId = UUID.randomUUID();
        Response response = transfer(randomId, second.getId(), 200);

        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(response.asString()).contains("There is no account with accountId [" + randomId + "]");
    }

    @Test
    public void transfer_negativeMoney() {
        AccountDto first = createAccount();
        AccountDto second = createAccount();

        Response response = transfer(first.getId(), second.getId(), -200);

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test(timeout = 10000)
    public void concurrentTransfer() {
        AccountDto first = createAccount();
        AccountDto second = createAccount();
        depositTo(first, 10000);

        CountDownLatch startLatch = new CountDownLatch(3);
        CountDownLatch stopLatch = new CountDownLatch(3);
        ExecutorService executors = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++) {
            executors.execute(() -> {
                startLatch.countDown();
                await(startLatch);
                for (int i1 = 0; i1 < 10; i1++) {
                    transfer(first.getId(), second.getId(), 100);
                }
                stopLatch.countDown();
            });
        }

        await(stopLatch);
        assertThat(getAccounts()).contains(
            new AccountDto(first.getId(), 7000),
            new AccountDto(second.getId(), 3000)
        );
    }

    @Test(timeout = 10000)
    public void concurrentDeposit() {
        AccountDto first = createAccount();

        CountDownLatch startLatch = new CountDownLatch(3);
        CountDownLatch stopLatch = new CountDownLatch(3);
        ExecutorService executors = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++) {
            executors.execute(() -> {
                startLatch.countDown();
                await(startLatch);
                for (int i1 = 0; i1 < 10; i1++) {
                    depositTo(first, 100);
                }
                stopLatch.countDown();
            });
        }

        await(stopLatch);
        assertThat(getAccounts()).contains(
            new AccountDto(first.getId(), 3000)
        );
    }

    @SneakyThrows
    private static void await(CountDownLatch latch) {
        latch.await();
    }

    private static Response transfer(UUID from, UUID to, int amount) {
        return given()
            .param("to", to)
            .param("amount", amount)
            .post("account/{id}/transfer", from);
    }

    private static AccountDto createAccount() {
        return given().post("account").as(AccountDto.class);
    }

    private static AccountDto[] getAccounts() {
        return given().get("account").as(AccountDto[].class);
    }

    private static Response depositTo(AccountDto first, int amount) {
        return given()
            .param("amount", amount)
            .post("/account/{id}/deposit", first.getId());
    }
}
