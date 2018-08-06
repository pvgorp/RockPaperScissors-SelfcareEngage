package com.pietervangorp.selfcare.engage;

import com.pietervangorp.selfcare.engage.ai.AutomaticPlayer;
import com.pietervangorp.selfcare.engage.ai.AutomaticPlayerV1;
import com.pietervangorp.selfcare.engage.ai.AutomaticPlayerV2;
import com.pietervangorp.selfcare.engage.items.*;

/**
 * Rock/paper/scissors game where two computer bots compete without user input.
 * This variant uses two stubbern computer implementations that will naively 
 * battle each other by looking at the other one's last attempt.
 * 
 * Because of that approach, the program will loop infinitely as soon as they 
 * make an initial choice that is identical (e.g., twice Scissors).
 */
public class GameV5 {

  private enum GameResult {
    UNDECIDED, P1WON, P2WON, TIE
  }

  public static void main(String[] args) {
    AutomaticPlayer player1= new AutomaticPlayerV2(); // CHANGED
    AutomaticPlayer player2= new AutomaticPlayerV2(); // CHANGED 
    
    runGame(player1, player2, 50);    
  }
  
  /**
   * Perform a game run with two automatic Player instances.
   * This method is useful to demonstrate different automated gaming strategies.
   * 
   * This method was defined by applying "extract method" on V4's main method 
   * (and ensuring the two Player variables are out of scope). After applying 
   * the extract method operation, also a parameter was added to terminate the 
   * loop after X rounds.
   * 
   * @param player1
   * @param player2
   */
  private static void runGame(AutomaticPlayer player1, AutomaticPlayer player2, int maxRuns) {
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
    } while (result != GameResult.P1WON && result != GameResult.P2WON && round <= maxRuns);
  }
}