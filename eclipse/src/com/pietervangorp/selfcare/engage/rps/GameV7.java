package com.pietervangorp.selfcare.engage.rps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import com.google.gson.Gson;
import com.pietervangorp.selfcare.engage.rps.ai.AutomaticPlayer;
import com.pietervangorp.selfcare.engage.rps.ai.AutomaticPlayerV2;
import com.pietervangorp.selfcare.engage.rps.items.Item;
import com.pietervangorp.selfcare.engage.vo.Settings;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reasonably realistic rock/paper/scissors game: taking one player input 
 * and comparing it against a random computer pick until one of both wins.
 * 
 * Opposed to V3, in this variant the human is playing against the AutomaticPlayerV2,
 * so the human can predict the behavior of the bot (since that one makes predictable counter-attacks)
 * 
 * @author pvgorp
 *
 */
public class GameV7 {

  private static final Logger logger = Logger.getLogger(GameV7.class.getName());
  
  private Settings settings;
  
  public GameV7() {
      try {
          settings = new Gson().fromJson(
                  new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("Settings.json"))),
                  Settings.class);
          logger.info("API base: "+settings.getSelfcareApiBaseURL());
          //cacheFiles(configurationModel.getColumnMappings());
      } catch (Exception e) {
          logger.log(Level.SEVERE, "Other error", e);
      }
  }
  
  public void doGame() {
      String howto = "Please enter once one of {paper,rock,scissors} to play"; 
      System.out.println(howto);
      Scanner s = new Scanner(System.in);
      Item i1 = null;
      Item i2 = null;
      AutomaticPlayer player2= new AutomaticPlayerV2(); // ONLY DIFFERENCE TO V3 
      GameResult result = GameResult.UNDECIDED;
      int round= 1;
      do {
        System.out.println("Round "+round++);
        do {
          try {
            System.out.println("Player 1: please enter your choice");
            i1 = ItemFactory.toItem(s.next());          
            i2 = player2.play(); 
            player2.considerOpponentItem(i1); // NEW
          } catch (InvalidInputException e) {
            System.out.println(e);
            System.out.println(howto);
          }
        } while (i1 == null || i2 == null);
        System.out.println("You selected " + i1); 
        System.out.println("Your computer opponent selected " + i2); 
        if (i1.beats(i2)) {
          System.out.println("You win");  
          result = GameResult.P1WON;
        } else if (i2.beats(i1)) {
          System.out.println("You loose");
          result = GameResult.P2WON;
        } else {
          System.out.println("Nobody wins");
          result = GameResult.TIE;
        }
      } while (result != GameResult.P1WON && result != GameResult.P2WON);
      s.close();
  }
  
  private enum GameResult {
    UNDECIDED, P1WON, P2WON, TIE
  }

  public static void main(String[] args) {
    GameV7 game= new GameV7();
    game.doGame();
  }
}