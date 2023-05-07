package org.example.system.util;

import lombok.Getter;
import lombok.Setter;
import org.example.system.game.PlayerInfo;
import org.example.system.turnobj.FollowCard;
import org.example.system.turnobj.TurnObject;
import org.example.system.turnobj.pokemon.Pikachu;

import java.util.*;

@Getter
@Setter
public class TurnWrapper {
    public final static int TURN_DISTANCE = 10000;

    private int age = 0;

    private List<FollowCard> objects = new ArrayList<>();
    public void addObject(List<FollowCard> objects){
        getObjects().addAll(objects);
    }
    public void addObject(FollowCard object){
        getObjects().add(object);
    }

    record WaitQueueItem(boolean owned,int id,String name,int waitTime,int speed){}

    public List<WaitQueueItem> listWithOffsetWaitTime(PlayerInfo player){
        if(getObjects().isEmpty()){
            return new ArrayList<>();
        }
        final FollowCard minWaitTurnObject = getObjects()
            .stream().min(Comparator.comparing(FollowCard::waitTime)).get();
        getObjects().forEach(turnObject -> turnObject.setWaitTimeShow(turnObject.waitTime() - minWaitTurnObject.waitTime()));

        getObjects().sort(Comparator.comparing(FollowCard::waitTime));
        return getObjects().stream().map(followCard ->
            new WaitQueueItem(followCard.ownerPlayer()==player,
                followCard.getTureId(),followCard.getName(),followCard.getWaitTimeShow(),followCard.getSpeed()))
            .toList();
    }

    public FollowCard nextObjectTurn(){
        if(objects.isEmpty()) return null;

        final Optional<FollowCard> readyObject = objects.stream()
            .filter(FollowCard::readyForTurn)
            .max(Comparator.comparingInt(FollowCard::getPassage));

        if(readyObject.isPresent()){
            return readyObject.get();
        }

        age++;
        final Optional<FollowCard> possibleObject = objects.stream()
            .filter(FollowCard::stepOnce)
            .max(Comparator.comparingInt(FollowCard::getPassage));

        return possibleObject.orElseGet(this::nextObjectTurn);


    }


}
