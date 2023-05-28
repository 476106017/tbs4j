package org.example.turnobj;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.CardType;
import org.example.constant.EffectTiming;
import org.example.system.game.Effect;
import org.example.system.game.Play;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.example.constant.CounterKey.DEFAULT;

@Getter
@Setter
public class CountCard extends GameObj {
    private String name;
    private int speed;
    private boolean disposable;// 一次性的

    public final CardType TYPE = CardType.SPELL;
    private transient Consumer<FollowCard> exec;
    private transient FollowCard target;

    private Map<String,Integer> counter = new HashMap<>();

    public CountCard(String name,int speed,FollowCard target,Consumer<FollowCard> exec) {
        this.name = name;
        this.speed = speed;
        this.exec = exec;
        this.target = target;
        this.disposable = true;
    }

    public void count(){
        count(DEFAULT,1);
    }
    public void count(int time){
        count(DEFAULT,time);
    }
    public void count(String key){
        count(key,1);
    }
    public Integer getCount(){
        return Optional.ofNullable(counter.get(DEFAULT)).orElse(0);
    }
    public Integer getCount(String key){
        return Optional.ofNullable(counter.get(key)).orElse(0);
    }
    public void clearCount(){
        counter.remove(DEFAULT);
    }
    public void clearCount(String key){
        counter.remove(key);
    }
    public void setCount(String key,int time){
        counter.put(key, time);
    }
    public void count(String key,int time){
        counter.merge(key, time, Integer::sum);
    }

    public void destroy(FollowCard card){destroy(List.of(card));}
    public int destroy(List<FollowCard> cards){
        List<FollowCard> cardsCopy = new ArrayList<>(cards);
        return (int) cardsCopy.stream().filter(card->card.destroyedBy(this)).count();
    }
}