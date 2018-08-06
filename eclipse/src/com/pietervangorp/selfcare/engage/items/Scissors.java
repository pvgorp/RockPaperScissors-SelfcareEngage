package com.pietervangorp.selfcare.engage.items;

public class Scissors implements Item {
  @Override
  public boolean beats(Item other) {
    return other instanceof Paper;
  }
  public String toString() {
    return "SCISSORS";
  }
}
