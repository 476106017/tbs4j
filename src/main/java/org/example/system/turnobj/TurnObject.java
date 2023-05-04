package org.example.system.turnobj;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static org.example.system.util.TurnWrapper.TURN_DISTANCE;

@Getter
@Setter
@ToString
public abstract class TurnObject {
    String name = "回合制对象";
    int speed = 100;
    int passage = 0;
    int waitTimeShow = 0;

    public boolean readyForTurn(){
        return passage/TURN_DISTANCE > 0;
    }
    public boolean stepOnce(){
        passage+=getSpeed();
        return readyForTurn();
    }

    public void endTurn(){
        passage-=TURN_DISTANCE;
    }

    public int waitTime (){
        final int distanceToEnding = TURN_DISTANCE - passage;
        return distanceToEnding/speed;
    }
}