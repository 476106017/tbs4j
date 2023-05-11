package org.example.system.game;

import jakarta.websocket.Session;
import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.turnobj.GameObj;
import org.example.system.util.Msg;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public abstract class Leader extends GameObj {

    private int speed=0;
    private transient boolean needTarget = true;
    private boolean canUseSkill = true;

    private transient List<Effect> effects = new ArrayList<>();

    public abstract String getJob();
    public abstract String getSkillName();
    public abstract String getSkillMark();
    public abstract int getSkillCost();
    public abstract String getMark();
    public abstract void setMark(String mark);


    public List<GameObj> targetable(){return new ArrayList<>();}

    public void skill(GameObj target){
        GameInfo info = ownerPlayer().getInfo();
        Session me = ownerPlayer().getSession();

        if(!isCanUseSkill()){
            Msg.warn(me, "现在无法使用主战者技能！");
            throw new RuntimeException();
        }
        if(target!=null && !targetable().contains(target)){
            Msg.warn(me, "无法指定该目标！");
            throw new RuntimeException();
        }
        info.msg(ownerPlayer().getName() + "使用了"+getName()+"的主战者技能："+getSkillName());
        setCanUseSkill(false);
    };

    public void addEffect(Effect newEffect){
        addEffect(newEffect,true);
    }

    public void addEffect(Effect newEffect,boolean only){
        newEffect.setOwnerObj(this);
        if(only && effects.stream()
            .anyMatch(e ->
                // 相同创建者、和相同效果时机，不能叠加
                e.getParent().getClass().equals(newEffect.getParent().getClass())
                    && e.getTiming().equals(newEffect.getTiming())
            )){
            info.msg("该主战者效果不能叠加！");
            return;
        }
        info.msg(newEffect.getParent().getNameWithOwner() + "为" + ownerPlayer().getName() + "提供了"+newEffect.getTiming().getName()+"效果！");
        effects.add(newEffect);
    }

    public List<Effect> getEffectsWhen(EffectTiming timing){
        return getEffects().stream()
            .filter(effect -> timing.equals(effect.getTiming()))
            .toList();

    }
    public void expireEffect(){
        // 过期主战者效果
        List<Effect> usedUpEffects = new ArrayList<>();
        getEffects()
            .forEach(effect -> {
                int canUse = effect.getCanUseTurn();
                if(canUse == 1){
                    // 用完了销毁
                    usedUpEffects.add(effect);
                    ownerPlayer().getInfo().msg(effect.getParent().getNameWithOwner() + "提供的主战者效果已消失");
                }else if (canUse > 1){
                    effect.setCanUseTurn(canUse-1);
                }
            });
        getEffects().removeAll(usedUpEffects);
    }

}
