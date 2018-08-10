package com.pietervangorp.selfcare.engage.rps.vo;

import com.pietervangorp.selfcare.engage.vo.GameSession;

import lombok.Data;

@Data
public class RockPaperScissorsGame extends GameSession {
    /**
     * represents the number of times that a rock/paper/scissor was drawn (across both players)
     */
    private int numberOfIterations=0;
    /**
     * represents the currentTimeMillis at the time the game was started
     */
    private long startTimeMS;
    /**
     * represents the currentTimeMillis at the time the game was completed/ended
     */
    private long endTimeMS; 
}
