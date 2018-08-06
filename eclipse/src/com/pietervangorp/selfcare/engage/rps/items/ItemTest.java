package com.pietervangorp.selfcare.engage.rps.items;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ItemTest {

  final static Rock r1 = new Rock();
  final static Rock r2 = new Rock();

  final static Scissors s1 = new Scissors();
  final static Scissors s2 = new Scissors();

  final static Paper p1 = new Paper();
  final static Paper p2 = new Paper();

  @Test
  public void testRockRock() {    
    assertFalse(r1.beats(r2));//assertThat(r1.beats(r2), equalTo(false));
  }

  @Test
  public void testRockScissors() {    
    assertTrue(r1.beats(s1));//assertThat(r1.beats(s1), equalTo(true));
  }

  @Test
  public void testRockPaper() {
    //assertThat(r1.beats(p1), equalTo(false));
    assertFalse(r1.beats(p1));
  }

  @Test
  public void testScissorsScissors() {
    assertFalse(s1.beats(s1));//assertThat(s1.beats(s1), equalTo(false));
  }

  @Test
  public void testScissorsPaper() {
    assertTrue(s1.beats(p1));//assertThat(s1.beats(p1), equalTo(true));
  }

  @Test
  public void testScissorsRock() {
    assertFalse(s1.beats(r1));//assertThat(s1.beats(r1), equalTo(false));
  }

  @Test
  public void testPaperPaper() {
    assertFalse(p1.beats(p2));//assertThat(p1.beats(p2), equalTo(false));
  }

  @Test
  public void testPaperScissors() {
    assertFalse(p1.beats(s1));//assertThat(p1.beats(s1), equalTo(false));
  }

  @Test
  public void testPapeRock() {
    assertTrue(p1.beats(r1));//assertThat(p1.beats(r1), equalTo(true));
  }
}
