package com.pietervangorp.selfcare.engage.rps.ai;

import java.util.Vector;

import com.pietervangorp.selfcare.engage.rps.items.Item;
import com.pietervangorp.selfcare.engage.rps.items.Paper;
import com.pietervangorp.selfcare.engage.rps.items.Rock;
import com.pietervangorp.selfcare.engage.rps.items.Scissors;

/**
 * An automatic player implementation that tries to beat the opponent brute force.
 * When two of these implementations battle each other, they will never end...
 * @author pvgorp
 *
 */
public class AutomaticPlayerV2 implements AutomaticPlayer {
  java.util.Random ran= new java.util.Random();
  Vector<Item> otherPlayerItems= new Vector<Item>();
  
  final static double oneThird= 1.0/3.0;
  final static double twoThird= 2.0/3.0;
  final static double threeThird= 1.0;
  
  @Override
  public Item play() {
    if (otherPlayerItems.isEmpty()) {
      double num= ran.nextDouble();
      if (num < oneThird) {
        return new Rock();
      } else if (num < twoThird) {
        return new Scissors();
      } else {
        return new Paper();
      }
    } else {
      if (otherPlayerItems.lastElement() instanceof Rock) {
        return new Paper();        
      } else if (otherPlayerItems.lastElement() instanceof Paper) {
        return new Scissors();        
      } else {
        return new Rock();    
      }
    }
  }
  
  @Override
  public void considerOpponentItem(Item itemOfOther) {
    otherPlayerItems.add(itemOfOther);
  }
}
