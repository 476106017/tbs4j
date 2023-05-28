package org.example.turnobj;


import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.game.Effect;
import org.example.system.game.GameInfo;
import org.example.system.game.Leader;
import org.example.system.game.PlayerInfo;

import java.util.*;
import java.util.function.Consumer;

import static org.example.constant.CounterKey.DEFAULT;
import static org.example.system.util.TurnWrapper.TURN_DISTANCE;

@Getter
@Setter
public abstract class GameObj {

    private static int id_iter=10000; //共用的静态变量
    public final int id;

    public transient GameInfo info;

    private Map<String,Integer> counter = new HashMap<>();

    public transient int owner = 0;
    public void changeOwner(){
        owner = 1-owner;
    }
    public PlayerInfo ownerPlayer(){
        return info.getPlayerInfos()[owner];
    }
    public Leader ownerLeader(){
        return ownerPlayer().getLeader();
    }
    public PlayerInfo enemyPlayer(){
        return info.getPlayerInfos()[1-owner];
    }
    public Leader enemyLeader(){
        return enemyPlayer().getLeader();
    }

    public abstract void setName(String name);
    public abstract String getName();
    public int getTureId(){
        return id;
    }
    public String getId(){
        return getName() + "#" + getTureId()%10000;
    }

    public String getNameWithOwner(){
        if(info==null) return getId();
        return ownerPlayer().getName()+"的"+getId();
    };
    public void init(){}


    // region 效果操作
    public transient List<Effect> effects = new ArrayList<>();

    public void addEffects(Effect effect){
        effects.add(effect);
    }

    public List<Effect> getEffects(EffectTiming timing){
        return getEffects().stream().filter(effect -> effect.getTiming().equals(timing)).toList();
    }
    public List<Effect> getEffectsFrom(GameObj parent){
        return getEffects().stream().filter(effect -> effect.getParent().equals(parent)).toList();
    }
    // 不加入队列，立即生效的效果（增加回复量、伤害量、加减状态等）
    public void useEffects(EffectTiming timing, Object param){
        List<Effect> effectList = getEffects(timing);
        if(effectList.size()>0)
            ownerPlayer().count(timing.getName());
        effectList.forEach(effect -> {
            new Effect.EffectInstance(effect,param).consume();
        });
    }
    public void useEffects(EffectTiming timing){
        List<Effect> effectList = getEffects(timing);
        if(effectList.size()>0)
            ownerPlayer().count(timing.getName());
        effectList.forEach(effect -> {
            new Effect.EffectInstance(effect,null).consume();
        });
    }

    public void tempEffects(EffectTiming timing){
        List<Effect> effectList = getEffects(timing);
        if(effectList.size()>0)
            ownerPlayer().count(timing.getName());
        effectList.forEach(effect -> {
            if(effect.getCanEffect().test(null))
                info.tempEffect(new Effect.EffectInstance(effect));
        });
    }
    public void tempEffects(EffectTiming timing,Object param){
        List<Effect> effectList = getEffects(timing);
        if(effectList.size()>0)
            ownerPlayer().count(timing.getName());
        effectList.forEach(effect -> {
            if(effect.getCanEffect().test(param))
                info.tempEffect(new Effect.EffectInstance(effect,param));
        });
    }
    // endregion 效果操作

    public GameObj() {
        id_iter++;
        id = id_iter;
    }

    // region 作为回合制对象的属性

    int passage = 0;
    int waitTimeShow = 0;
    int tempSpeed = 0;
    public abstract void setSpeed(int speed);
    public abstract int getSpeed();
    public void addSpeed(int speed) {
        if(speed==0)return;
        info.msg(this.getNameWithOwner() + "获得了" + speed + "点速度");
        final int newSpeed = Math.max(getSpeed()+speed, 1);
        setSpeed(newSpeed);
    }
    public void addTempSpeed(int speed) {
        if(speed==0)return;
        info.msg(this.getNameWithOwner() + "获得了临时速度");
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
    // endregion 作为回合制对象的属性



    public void count(){
        count(DEFAULT,1);
    }
    public void count(int time){
        count(DEFAULT,time);
    }
    public Integer getCount(){
        return Optional.ofNullable(counter.get(DEFAULT)).orElse(0);
    }
    public Integer getCount(String key){
        return Optional.ofNullable(counter.get(key)).orElse(0);
    }
    public void count(String key){
        count(key,1);
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



    public void createCountCard(String name, int speed, FollowCard target, Consumer<FollowCard> exec) {
        final CountCard card = new CountCard(name, speed, target, exec);
        card.setOwner(getOwner());
        card.setInfo(getInfo());
        card.init();
        info.getTurn().addObject(card);
    }

}
