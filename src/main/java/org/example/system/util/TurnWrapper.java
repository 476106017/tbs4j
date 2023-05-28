package org.example.system.util;

import lombok.Getter;
import lombok.Setter;
import org.example.system.game.PlayerInfo;
import org.example.turnobj.GameObj;
import org.example.turnobj.GameObj;

import java.util.*;

@Getter
@Setter
public class TurnWrapper {
    public final static int TURN_DISTANCE = 10000;

    private int age = 0;

    private List<GameObj> objects = new ArrayList<>();
    public <T extends GameObj> void addObject(List<T> objects){
        objects.forEach(obj -> obj.setPassage(0));
        getObjects().addAll(objects);
    }
    public <T extends GameObj>  void addObject(T object){
        getObjects().add(object);
    }
    public <T extends GameObj>  void removeObject(T object){
        getObjects().remove(object);
    }

    record WaitQueueItem(boolean owned,int id,String name,int waitTime,int speed){}

    public List<WaitQueueItem> listWithOffsetWaitTime(PlayerInfo player){
        if(getObjects().isEmpty()){
            return new ArrayList<>();
        }
        final GameObj minWaitTurnObject = getObjects()
            .stream().min(Comparator.comparing(GameObj::waitTime)).get();
        getObjects().forEach(turnObject -> turnObject.setWaitTimeShow(turnObject.waitTime() - minWaitTurnObject.waitTime()));

        getObjects().sort(Comparator.comparing(GameObj::waitTime));
        return getObjects().stream().map(obj ->
            new WaitQueueItem(obj.ownerPlayer()==player,
                obj.getTureId(),obj.getName(),obj.getWaitTimeShow(),obj.getSpeed()))
            .toList();
    }

    public GameObj nextObjectTurn(){
        if(objects.isEmpty()) return null;

        final Optional<GameObj> readyObject = objects.stream()
            .filter(GameObj::readyForTurn)
            .max(Comparator.comparingInt(GameObj::getPassage));

        if(readyObject.isPresent()){
            return readyObject.get();
        }

        age++;
        final Optional<GameObj> possibleObject = objects.stream()
            .filter(GameObj::stepOnce)
            .max(Comparator.comparingInt(GameObj::getPassage));

        return possibleObject.orElseGet(this::nextObjectTurn);


    }


}
