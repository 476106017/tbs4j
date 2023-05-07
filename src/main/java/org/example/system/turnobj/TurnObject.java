package org.example.system.turnobj;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static org.example.system.util.TurnWrapper.TURN_DISTANCE;

@Getter
@Setter
@ToString
public abstract class TurnObject {
    int passage = 0;
    int waitTimeShow = 0;
    int tempSpeed = 0;
    public abstract void setSpeed(int speed);
    public abstract int getSpeed();
    public void addSpeed(int speed) {
        final int newSpeed = Math.max(getSpeed()+speed, 1);
        setSpeed(newSpeed);
    }
    public void addTempSpeed(int speed) {
        if(getSpeed()+speed <= 0){
            setTempSpeed(getTempSpeed()+1-getSpeed());
            setSpeed(1);
        }else {
            setTempSpeed(getTempSpeed()+speed);
            addSpeed(speed);
        }
    }

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
        return distanceToEnding/getSpeed();
    }
}