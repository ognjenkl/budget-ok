package com.ognjen.budgetok.acceptance;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParamsTest {

  @ParameterizedTest
  @MethodSource("provideTestCases")
  void testGetValue(String[] args, String key, String expectedValue) {
    Params params = new Params(args);
    String actualValue = params.getValue(key);
    assertEquals(expectedValue, actualValue);
  }

  private static Stream<Arguments> provideTestCases() {
    return Stream.of(
      Arguments.of(
        new String[]{"envelope: Jana"},
        "envelope",
        "Jana"
      ),
      Arguments.of(
        new String[]{"envelope: Jana", "amount: 100"},
        "amount",
        "100"
      ),
      Arguments.of(
        new String[]{"envelope : Jana"},
        "envelope",
        null
      ),
      Arguments.of(
        new String[]{"envelope: Jana"},
        "nonexistent",
        null
      ),
      Arguments.of(
        new String[]{"envelope: "},
        "envelope",
        ""
      ),
      Arguments.of(
        new String[]{"description: This:has:colons"},
        "description",
        "This:has:colons"
      )
    );
  }
}