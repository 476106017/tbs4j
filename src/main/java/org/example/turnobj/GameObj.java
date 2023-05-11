package org.example.turnobj;


import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.game.Effect;
import org.example.system.game.GameInfo;
import org.example.system.game.Leader;
import org.example.system.game.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class GameObj extends TurnObject {

    private static int id_iter=10000; //共用的静态变量
    public final int id;

    public transient GameInfo info;

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
        return getName() + "#" + hashCode()%10000;
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

}
