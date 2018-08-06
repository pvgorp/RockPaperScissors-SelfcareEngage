package com.pietervangorp.selfcare.engage.rps;

import com.pietervangorp.selfcare.engage.rps.ai.AutomaticPlayer;
import com.pietervangorp.selfcare.engage.rps.ai.AutomaticPlayerV1;
import com.pietervangorp.selfcare.engage.rps.ai.AutomaticPlayerV2;
import com.pietervangorp.selfcare.engage.rps.items.Item;

/**
 * Rock/paper/scissors game where a human plays against the V2 bot (which can predicted in its counter-attacks)
 */
public class GameV6 {

  private enum GameResult {
    UNDECIDED, P1WON, P2WON, TIE
  }

  public static void main(String[] args) {
    AutomaticPlayer player1= new AutomaticPlayerV1(); // KEPT INTACT TO AVOID INFINITE LOOP
    AutomaticPlayer player2= new AutomaticPlayerV2(); // CHANGED 
    
    runGame(player1, player2);    
  }
  
  /**
   * Perform a game run with two automatic Player instances.
   * This method is useful to demonstrate different automated gaming strategies.
   * 
   * Result of applying "extract method" on V4's main method 
   * (and ensuring the two Player variables are out of scope)
   * @param player1
   * @param player2
   */
  private static void runGame(AutomaticPlayer player1, AutomaticPlayer player2) {
    String howto = "Two computer players will compete by picking {paper,rock,scissors} randomly"; // CHANGED
    System.out.println(howto);
    Item i1 = null;
    Item i2 = null;    
    GameResult result = GameResult.UNDECIDED;
    int round= 1;
    do {
      System.out.println("Round "+round++);     
      i1 = player1.play();      
      i2 = player2.play();
      player1.considerOpponentItem(i2); // NEW
      player2.considerOpponentItem(i1); // NEW
      System.out.println("Computer player 1 selected " + i1); 
      System.out.println("Computer player 2 selected " + i2); 
      if (i1.beats(i2)) {
        System.out.println("Player 1 wins");    
        result = GameResult.P1WON;
      } else if (i2.beats(i1)) {
        System.out.println("Player 2 wins"); 
        result = GameResult.P2WON;
      } else {
        System.out.println("Nobody wins");
        result = GameResult.TIE;
      }
    } while (result != GameResult.P1WON && result != GameResult.P2WON);
  }
}