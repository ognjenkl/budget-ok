package com.ognjen.budgetok.contract;

import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("contract-test")
@PactBroker(
    host = "${PACT_BROKER_HOST:}",
    port = "${PACT_BROKER_PORT:}",
    scheme = "${PACT_BROKER_SCHEME:}",
    authentication = @PactBrokerAuth(
        username = "${PACT_BROKER_USERNAME:}",
        password = "${PACT_BROKER_PASSWORD:}"
    )
)

public class PactTestConfiguration {

  @Bean
  public StateHandler stateHandler() {
    return new StateHandler();
  }

  public static class StateHandler {

    @State("a request to create an envelope")
    public void toCreateEnvelopeState() {
      // State handling logic can be implemented here if needed
    }
  }
}
