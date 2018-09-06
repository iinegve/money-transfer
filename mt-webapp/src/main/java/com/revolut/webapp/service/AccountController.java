package com.revolut.webapp.service;

import com.revolut.core.account.Account;
import com.revolut.core.bank.Bank;
import com.revolut.core.money.Money;
import com.revolut.persistence.DataSource;
import com.revolut.persistence.TransactionalOperation;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Slf4j
@Path("/")
public class AccountController {

    private final Bank bank;
    private final DataSource dataSource;
    private final int numberOfRetries;

    public AccountController(Bank bank, DataSource dataSource, int numberOfRetries) {
        this.bank = bank;
        this.dataSource = dataSource;
        this.numberOfRetries = numberOfRetries;
    }

    @POST
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountDto createAccount() {
        return new AccountDto(bank.createAccount());
    }

    @GET
    @Path("/account/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountDto getAccount(
        @PathParam("id") UUID id
    ) {
        return new AccountDto(bank.getAccount(id));
    }

    @GET
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    public List<AccountDto> getAllAccounts() {
        return bank.getAllAccounts().stream()
            .map(AccountDto::new)
            .collect(toList());
    }

    @POST
    @Path("/account/{id}/deposit")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deposit(
        @PathParam("id") UUID id,
        @FormParam("amount") int amount
    ) {
        Account[] account = new Account[1];
        RetriableOperation operation = new RetriableOperation(
            numberOfRetries,
            new TransactionalOperation(dataSource,
                () -> account[0] = bank.deposit(id, new Money(amount))
            ));

        try {
            operation.call();
            return Response.status(200).entity(new AccountDto(account[0])).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/account/{id}/transfer")
    public Response transfer(
        @PathParam("id") UUID from,
        @FormParam("to") UUID to,
        @FormParam("amount") int amount
    ) {
        log.debug("Transfer amount [{}] from [{}] to [{}]", amount, from, to);

        if (amount < 0)
            return Response.status(400).entity("Negative amount cannot be transferred").build();

        RetriableOperation operation = new RetriableOperation(
            numberOfRetries,
            new TransactionalOperation(dataSource,
                () -> bank.transfer(from, to, new Money(amount))));

        try {
            operation.call();
            return Response.status(200).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }
}
