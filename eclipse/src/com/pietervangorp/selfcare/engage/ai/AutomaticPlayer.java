package com.pietervangorp.selfcare.engage.ai;

import com.pietervangorp.selfcare.engage.items.Item;

public interface AutomaticPlayer {
  public Item play();
  
  public void considerOpponentItem(Item itemOfOther);
}
