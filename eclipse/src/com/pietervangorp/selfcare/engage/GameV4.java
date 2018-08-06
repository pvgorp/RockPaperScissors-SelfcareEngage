package com.pietervangorp.selfcare.engage;

import com.pietervangorp.selfcare.engage.ai.AutomaticPlayer;
import com.pietervangorp.selfcare.engage.ai.AutomaticPlayerV1;
import com.pietervangorp.selfcare.engage.items.*;

/**
 * Rock/paper/scissors game where two computer bots compete without user input.
 * Bot behavior is completely random and ignores what other players/bots do.
 * 
 * @author pvgorp
 */
public class GameV4 {

  private enum GameResult {
    UNDECIDED, P1WON, P2WON, TIE
  }

  public static void main(String[] args) {
    String howto = "Two computer players will compete by picking {paper,rock,scissors} randomly"; // CHANGED
    System.out.println(howto);
    Item i1 = null;
    Item i2 = null;
    AutomaticPlayer player1= new AutomaticPlayerV1(); // NEW 
    AutomaticPlayer player2= new AutomaticPlayerV1();  
    GameResult result = GameResult.UNDECIDED;
    int round= 1;
    do {
      System.out.println("Round "+round++);     
      i1 = player1.play();   // CHANGED        
      i2 = player2.play();
      System.out.println("Computer player 1 selected " + i1); // CHANGED
      System.out.println("Computer player 2 selected " + i2); // CHANGED
      if (i1.beats(i2)) {
        System.out.println("Player 1 wins");    // CHANGED
        result = GameResult.P1WON;
      } else if (i2.beats(i1)) {
        System.out.println("Player 2 wins"); // CHANGED
        result = GameResult.P2WON;
      } else {
        System.out.println("Nobody wins");
        result = GameResult.TIE;
      }
    } while (result != GameResult.P1WON && result != GameResult.P2WON);
  }
}