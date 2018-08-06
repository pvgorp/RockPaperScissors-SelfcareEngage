package com.pietervangorp.selfcare.engage.items;

public class Rock implements Item {
  @Override
  public boolean beats(Item other) {
    return other instanceof Scissors;
  }
  public String toString() {
    return "ROCK";
  }
}
