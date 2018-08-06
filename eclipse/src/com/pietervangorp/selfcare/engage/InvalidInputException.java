package com.pietervangorp.selfcare.engage;

public class InvalidInputException extends Exception {
  String input= "no input";
  public InvalidInputException(String input) {
    this.input= input;
  }
  
  public String toString() {
    return "Invalid input: '"+input+"'. Only Rock, Paper or Scissors is valid (casing does not matter)";
  }
}
