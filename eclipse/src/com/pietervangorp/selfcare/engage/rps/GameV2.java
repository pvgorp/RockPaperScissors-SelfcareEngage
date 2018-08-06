package com.pietervangorp.selfcare.engage.rps;

import java.util.Scanner;

import com.pietervangorp.selfcare.engage.rps.items.Item;

/**
 * Very basic support for the rock/paper/scissors game: taking two player inputs
 * and indicating whether someone has one, continue doing so until you have a
 * winner.
 * 
 * @author pvgorp
 *
 */
public class GameV2 {

  private enum GameResult {
    UNDECIDED, P1WON, P2WON, TIE
  }

  public static void main(String[] args) {
    String howto = "Please enter twice one of {paper,rock,scissors} to play";
    System.out.println(howto);
    Scanner s = new Scanner(System.in);
    Item i1 = null;
    Item i2 = null;
    GameResult result = GameResult.UNDECIDED;
    int round= 1;
    do {
      System.out.println("Round "+round++);
      do {
        try {
          System.out.println("Player 1: please enter your choice");
          i1 = ItemFactory.toItem(s.next());
          System.out.println("Player 2: please enter your choice");
          i2 = ItemFactory.toItem(s.next());
        } catch (InvalidInputException e) {
          System.out.println(e);
          System.out.println(howto);
        }
      } while (i1 == null || i2 == null);
      System.out.println("Player 1 selected " + i1);
      System.out.println("Player 2 selected " + i2);
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
    s.close();
  }
}