package com.pietervangorp.selfcare.engage.items;

public class Paper implements Item {
  @Override
  public boolean beats(Item other) {
    return other instanceof Rock;
  }
  public String toString() {
    return "PAPER";
  }
}
