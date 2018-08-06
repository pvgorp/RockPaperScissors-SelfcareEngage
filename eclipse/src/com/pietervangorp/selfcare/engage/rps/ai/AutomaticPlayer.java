package com.pietervangorp.selfcare.engage.rps.ai;

import com.pietervangorp.selfcare.engage.rps.items.Item;

public interface AutomaticPlayer {
  public Item play();
  
  public void considerOpponentItem(Item itemOfOther);
}
