package org.example.system.util;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.system.turnobj.Follow;
import org.example.system.turnobj.TurnObject;

import java.util.*;

@Getter
@Setter
public class TurnWrapper {
    public final static int TURN_DISTANCE = 10000;

    private int age = 0;

    private List<TurnObject> objects = new ArrayList<>();

    public static void main(String[] args) {
        Follow object = new Follow();
        object.setName("喷火龙");
        object.initHp(100);
        object.setAtk(12);
        object.setSpeed(105);
        Follow object2 = new Follow();
        object2.setName("皮卡丘");
        object2.initHp(70);
        object2.setAtk(7);
        object2.setSpeed(120);

        TurnWrapper turnWrapper = new TurnWrapper();

        turnWrapper.addObject(object);
        turnWrapper.addObject(object2);

        while (true){

            System.out.println(turnWrapper.listWithOffsetWaitTime());

            final TurnObject turnObject = turnWrapper.nextObjectTurn();

            // 互殴
            if(turnObject==object){
                object.attack(object2);
            }else {
                object2.attack(object);
            }

            turnObject.endTurn();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void addObject(TurnObject object){
        getObjects().add(object);
    }

    public List<TurnObject> listWithOffsetWaitTime(){
        if(getObjects().isEmpty()){
            return new ArrayList<>();
        }
        final TurnObject minWaitTurnObject = getObjects()
            .stream().min(Comparator.comparing(TurnObject::waitTime)).get();
        getObjects().forEach(turnObject -> turnObject.setWaitTimeShow(turnObject.waitTime() - minWaitTurnObject.waitTime()));

        getObjects().sort(Comparator.comparing(TurnObject::waitTime));
        return getObjects();
    }

    public TurnObject nextObjectTurn(){
        if(objects.isEmpty()) return null;

        final Optional<TurnObject> readyObject = objects.stream()
            .filter(TurnObject::readyForTurn)
            .max(Comparator.comparingInt(TurnObject::getPassage));

        if(readyObject.isPresent()){
            return readyObject.get();
        }

        age++;
        final Optional<TurnObject> possibleObject = objects.stream()
            .filter(TurnObject::stepOnce)
            .max(Comparator.comparingInt(TurnObject::getPassage));

        return possibleObject.orElseGet(this::nextObjectTurn);


    }


    @Getter
    @Setter
    public static class TestObject extends TurnObject{

    }

}
