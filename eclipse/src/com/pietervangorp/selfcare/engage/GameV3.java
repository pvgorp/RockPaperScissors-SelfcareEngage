package com.pietervangorp.selfcare.engage;

import java.util.Scanner;

import com.pietervangorp.selfcare.engage.ai.AutomaticPlayer;
import com.pietervangorp.selfcare.engage.ai.AutomaticPlayerV1;
import com.pietervangorp.selfcare.engage.items.*;

/**
 * Reasonably realistic rock/paper/scissors game: taking one player input 
 * and comparing it against a random computer pick until one of both wins.
 * 
 * @author pvgorp
 *
 */
public class GameV3 {

  private enum GameResult {
    UNDECIDED, P1WON, P2WON, TIE
  }

  public static void main(String[] args) {
    String howto = "Please enter once one of {paper,rock,scissors} to play"; // CHANGED
    System.out.println(howto);
    Scanner s = new Scanner(System.in);
    Item i1 = null;
    Item i2 = null;
    AutomaticPlayer player2= new AutomaticPlayerV1(); // NEW 
    GameResult result = GameResult.UNDECIDED;
    int round= 1;
    do {
      System.out.println("Round "+round++);
      do {
        try {
          System.out.println("Player 1: please enter your choice");
          i1 = ItemFactory.toItem(s.next());          
          i2 = player2.play(); // CHANGED
        } catch (InvalidInputException e) {
          System.out.println(e);
          System.out.println(howto);
        }
      } while (i1 == null || i2 == null);
      System.out.println("You selected " + i1); // CHANGED
      System.out.println("Your computer opponent selected " + i2); // CHANGED
      if (i1.beats(i2)) {
        System.out.println("You win");  // CHANGED
        result = GameResult.P1WON;
      } else if (i2.beats(i1)) {
        System.out.println("You loose");
        result = GameResult.P2WON;
      } else {
        System.out.println("Nobody wins");
        result = GameResult.TIE;
      }
    } while (result != GameResult.P1WON && result != GameResult.P2WON);
  }
}