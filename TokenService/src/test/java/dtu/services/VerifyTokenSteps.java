package dtu.services;

import dtu.application.TokenService;
import dtu.domain.Token;
import dtu.infrastructure.AccountAccess;
import dtu.infrastructure.LocalTokenRepository;
import dtu.presentation.TokenEventHandler;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import messaging.Event;
import messaging.EventResponse;
import messaging.implementations.MockMessageQueue;

public class VerifyTokenSteps {

	String customerId = null;
	Token token = null;
	String sessionId;
	private static MockMessageQueue messageQueue = new MockMessageQueue();
	private TokenService tokenService = new TokenService(messageQueue, new LocalTokenRepository());
	private TokenEventHandler tokenEventHandler = new TokenEventHandler(messageQueue, tokenService);
	private CompletableFuture<ArrayList<Token>> tokenCreation = new CompletableFuture<>();
	
	@Given("A customer with id {string}")
	public void aCustomerWithId(String customerId) {
		this.customerId = customerId;
		this.sessionId = "uniqueSessionId";
	}
	


	@And("the customer has tokens")
	public void theCustomerHasTokens() throws Throwable {
		new Thread(() -> {
			var tokens = tokenService.createTokens(customerId, 1, sessionId);
			tokenCreation.complete(tokens);
		}).start();
	}
	
	@When("the account verification response event is received")
	public void theAccountVerificationResponseEventIsReceived() throws InterruptedException {
		EventResponse eventResponse = new EventResponse(sessionId, true, null);
		Event event = new Event("CustomerVerificationResponse." + sessionId, eventResponse );
		Thread.sleep(100);
		tokenService.handleCustomerVerificationResponse(event);
	}

	@When("a request to verify the token is received")
	public void aRequestToVerifyTheTokenIsReceived() {
//		token = tokenService.createTokens(customerId, 1, sessionId).stream().findFirst().get();
		token = tokenCreation.join().iterator().next();
		EventResponse eventResponse = new EventResponse(sessionId, true, null, token.getUuid());
		Event incommingEvent = new Event("TokenVerificationRequested", eventResponse);
		tokenEventHandler.handleTokenVerificationRequest(incommingEvent);
	}

	
	@Then("the token is verified")
	public void theTokenIsVerified() {
		EventResponse eventResponse = new EventResponse(sessionId, true, null, token);
		Event event = new Event("TokenVerificationResponse." + sessionId, eventResponse);
		assertEquals(event, messageQueue.getEvent("TokenVerificationResponse." + sessionId));
	}

}