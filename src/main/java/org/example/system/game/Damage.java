package org.example.system.game;

import lombok.Getter;
import lombok.Setter;
import org.example.turnobj.FollowCard;
import org.example.turnobj.GameObj;

import java.util.List;

import static org.example.constant.CounterKey.BLOCK;
import static org.example.constant.CounterKey.STRENGTH;

@Getter
@Setter
public class Damage{
    GameObj from;
    FollowCard to;
    int damage;
    boolean isCounter = false;
    boolean isFromAtk = false;
    boolean miss = false;

    public Damage(GameObj from, FollowCard to, int damage) {
        this.from = from;
        this.to = to;
        this.damage = damage;
    }
    public Damage(FollowCard from, FollowCard to) {
        this.from = from;
        this.to = to;
        this.damage = from.getAtk();
        this.isFromAtk = true;
    }

    public void addDamage(int add){
        damage = Math.max(0,damage+add);
    }

    public GameObj another(GameObj obj){
        if(getFrom() == obj)
            return getTo();
        return getFrom();
    }

    public boolean avoid(){
        final PlayerInfo player = to.ownerPlayer();
        if (isFromAtk()) {
            if (isMiss()) {
                player.getInfo().msg(to.getNameWithOwner() + "闪避了本次攻击伤害！");
                return true;
            }
        }
        if (to.hasKeyword("圣盾")) {
            to.getInfo().msg(to.getNameWithOwner() + "的圣盾抵消了本次伤害！");
            to.removeKeyword("圣盾");
            return true;
        }
        if (!isFromAtk()) {
            if (to.hasKeyword("魔法免疫")) {
                to.getInfo().msg(to.getNameWithOwner() + "免疫了本次效果伤害！");
                return true;
            }
            if (to.hasKeyword("魔法护盾")) {
                to.getInfo().msg(to.getNameWithOwner() + "的魔法护盾抵消了本次效果伤害！");
                to.removeKeyword("魔法护盾");
                return true;
            }
        }
        return false;
    }

    public void multi(float f){
        final int damage2 = (int) (getDamage() * f);
        setDamage(damage2);
    }

    public void reduce(){
        Integer strength = 0;
        try{
            strength = from.ownerPlayer().getCount(STRENGTH);
        }catch (Exception ignored){}
        if(strength>0){
            to.getInfo().msg(from.ownerLeader().getNameWithOwner() + "的力量使本次伤害增加" + strength);
            addDamage(strength);
        }
        if(to.atArea()) {
            if (!isFromAtk && to.hasKeyword("效果伤害免疫")) {
                setDamage(0);
                to.getInfo().msg(to.getNameWithOwner() + "免疫了效果伤害！");
            } else if(!to.hasKeyword("穿透")) {
                // 没有穿透效果，计算减免
                float reduce = 0;
                if (isFromAtk())
                    reduce = (float)to.getArmor()/(to.getArmor()+100);
                else
                    reduce = (float)to.getMagicResist()/(to.getMagicResist()+100);

                setDamage((int)(getDamage() * (1-reduce)));

                int parry = to.getBlock();
                if(getDamage()>0 && parry>0){
                    int parryReduce = Math.min(getDamage(), parry);
                    setDamage(getDamage() - parryReduce);
                    to.setBlock(parry - parryReduce);
                    to.getInfo().msg(to.getNameWithOwner() + "格挡了" + parryReduce + "点伤害（还剩"+to.countKeyword("格挡")+"点格挡）");
                }
            }
        }
    }


    public void apply(){
        new DamageMulti(to.getInfo(), List.of(this)).apply();
    }
}
