package com.pietervangorp.selfcare.engage.ai;

import com.pietervangorp.selfcare.engage.items.*;

public class AutomaticPlayerV1 implements AutomaticPlayer {
  java.util.Random ran= new java.util.Random();
  final static double oneThird= 1.0/3.0;
  final static double twoThird= 2.0/3.0;
  final static double threeThird= 1.0;
  
  @Override
  public Item play() {
    double num= ran.nextDouble();
    if (num < oneThird) {
      return new Rock();
    } else if (num < twoThird) {
      return new Scissors();
    } else {
      return new Paper();
    }
  }
  
  @Override
  public void considerOpponentItem(Item itemOfOther) {
    // do nothing (ignore)
  }
}
