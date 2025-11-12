package com.ognjen.budgetok.acceptance;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Params {

  private final String[] args;

  public String getValue(String name) {
    for (String arg : args) {
      int index = arg.indexOf(name + ": ");
      if(index != -1) {
        return arg.substring(index+ name.length() + 2);
      }
    }
    return null;
  }
}
