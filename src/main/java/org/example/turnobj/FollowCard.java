package org.example.turnobj;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.CardType;
import org.example.constant.EffectTiming;
import org.example.system.game.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.example.constant.CounterKey.DEATH_PREFIX;
import static org.example.constant.CounterKey.DEFAULT;

@Getter
@Setter
public abstract class FollowCard extends GameObj {

    public final CardType TYPE = CardType.FOLLOW;
    private transient String color = "#000";
    private transient int atk = 0;
    private transient int hp = 0;
    private transient int armor = 0;
    private transient int magicResist = 0;
    private transient int block = 0;
    private int maxHp = 0;
    private List<Skill> skills = new ArrayList<>();
    public <T extends Skill> void addSkill(Class<T> skill){
        try {
            T card = skill.getDeclaredConstructor().newInstance();
            try {
                info.msg(getNameWithOwner()+"创造了"+card.getId());
            }catch (Exception ignored){}
            card.setOwner(getOwner());
            card.setInfo(getInfo());
            card.setBaseFollow(this);
            card.init();
            getSkills().add(card);

            if(card.getRace().contains("天赋")){
                card.getEffects().forEach(this::addEffects);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String,Integer> counter = new HashMap<>();

    private List<String> keywords = new ArrayList<>();

    private transient Damage incommingDamage = null;

    private transient int leaveIndex = -1;// 离场时所在的下标
    // 准备破坏
    public transient GameObj destroyedBy = null;

    public void setDestroyedBy(GameObj destroyedBy) {
        if(info.addEvent(this, EventType.Destroy))
            this.destroyedBy = destroyedBy;
    }


    public abstract String getJob();
    public abstract List<String> getRace();

    @Override
    public void init() {
        setMaxHp(getHp());
    }

    public void setIncommingDamage(Damage incommingDamage) {
//        info.msg(incommingDamage.getFrom().getId()+"对"+incommingDamage.getTo().getId()+"造成的伤害效果已被记录");
        info.getIncommingDamages().add(incommingDamage);
    }

    public void attack(FollowCard another){
        info.damageAttacking(this,another);
    }

    public boolean atArea(){
        return ownerPlayer().getArea().contains(this);
    }

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

    public String getKeywordStr(){
        Map<String, Long> count = keywords.stream()
            .collect(Collectors.groupingBy(k->k, Collectors.counting()));
        List<String> keywordWithCount = new ArrayList<>();
        count.forEach(((k,v)->{
            if(v.intValue()==1)
                keywordWithCount.add(k);
            else
                keywordWithCount.add(k+"("+v+")");
        }));
        return String.join(" ", keywordWithCount);
    }
    public void addKeyword(String k){
        info.msg(getNameWithOwner()+"获得了【"+k+"】");
        getKeywords().add(k);
        try {
            Thread.sleep(500);
            info.pushInfo();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void addKeywordN(String k,int n){
        info.msg(getNameWithOwner()+"获得了"+n+"层【"+k+"】");
        for (int i = 0; i < n; i++) {
            getKeywords().add(k);
        }
        try {
            Thread.sleep(500);
            info.pushInfo();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void addKeywords(List<String> ks){
        if(ks.isEmpty())return;
        info.msg(getNameWithOwner()+"获得了【"+ String.join("】【", ks) +"】");
        getKeywords().addAll(ks);
    }
    public boolean hasKeyword(String k){
        return getKeywords().contains(k);
    }
    public int countKeyword(String k){
        return (int) getKeywords().stream().filter(p->p.equals(k)).count();
    }

    public void removeKeywords(List<String> ks){
        ks.forEach(this::removeKeyword);
    }
    public void removeKeyword(String k){
        getKeywords().stream()
            .filter(keyword -> keyword.equals(k))
            .findFirst()
            .ifPresent(s -> {
                getKeywords().remove(s);
                if(hasKeyword(s))
                    info.msg(getNameWithOwner()+"失去了1层【"+ k +"】");
                else
                    info.msg(getNameWithOwner()+"失去了【"+ k +"】");
            });
    }
    public void removeKeyword(String k,int n){
        List<String> keys = getKeywords().stream()
            .filter(keyword -> keyword.equals(k)).toList();
        if(keys.size()>0){
            int min = Math.min(n, keys.size());
            for (int i = 0; i < min; i++) {
                getKeywords().remove(keys.get(i));
            }
            info.msg(getNameWithOwner()+"失去了"+min+"层【"+ k +"】");
            try {
                Thread.sleep(500);
                info.pushInfo();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void removeKeywordAll(String k){
        List<String> keys = getKeywords().stream()
            .filter(keyword -> keyword.equals(k)).toList();
        if(keys.size()>0){
            info.msg(getNameWithOwner()+"失去了"+keys.size()+"层【"+ k +"】");
            getKeywords().removeAll(keys);
        }
    }

    public void destroy(FollowCard card){destroy(List.of(card));}
    public int destroy(List<FollowCard> cards){
        List<FollowCard> cardsCopy = new ArrayList<>(cards);
        return (int) cardsCopy.stream().filter(card->card.destroyedBy(this)).count();
    }
    public boolean destroyed(){
        return destroyedBy!=null && destroyedBy(destroyedBy);
    }

    public boolean destroyedBy(GameObj from){
        if(!atArea())return false;

        if(hasKeyword("无法破坏")) {
            info.msg(getNameWithOwner() + "无法破坏！");
            return false;
        }
        if(hasKeyword("魔法免疫")){
            getInfo().msg(getNameWithOwner() + "免疫了本次破坏！");
            return true;
        }
        if(hasKeyword("魔法护盾")){
            getInfo().msg(getNameWithOwner() + "的魔法护盾抵消了本次破坏！");
            removeKeyword("魔法护盾");
            return true;
        }
        info.msg(getNameWithOwner() + "被"+from.getNameWithOwner()+"破坏！");
        death();
        from.tempEffects(EffectTiming.WhenKill,this);
        return true;
    }

    public void death(){
        if(!atArea())return;
        info.msg(getNameWithOwner()+"死亡！");

        List<FollowCard> area = ownerPlayer().getArea();
        setLeaveIndex(area.indexOf(this));
        area.remove(this);
        useEffects(EffectTiming.WhenNoLongerAtArea);

        // 从回合对象中删除该随从
        getInfo().getTurn().getObjects().remove(this);

        try {
            Thread.sleep(500);
            info.pushInfo();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(hasKeyword("游魂")){
            info.msg("墓地拒绝了【游魂】，无法发动死亡相关效果！");
            return;
        }

        // 注能
        ownerPlayer().count(DEATH_PREFIX+getName());
        tempEffects(EffectTiming.Leaving);
        tempEffects(EffectTiming.DeathRattle);

        // 复生时，保留装备，不进墓地，原地重新召唤
        if(hasKeyword("复生")){
            removeKeyword("复生");
            setHp(1);
            ownerPlayer().summon(this);
            try {
                Thread.sleep(500);
                info.pushInfo();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        ownerPlayer().addGraveyard(this);
    }

    public void heal(int hp){
        if(!atArea())return;
        if(hasKeyword("无法回复")){
            info.msg(this.getNameWithOwner()+"无法回复生命值！（剩余"+this.getHp()+"点生命值）");
            return;
        }
        if(hp>0) {
            int oldHp = getHp();
            setHp(Math.min(getMaxHp(), getHp() + hp));
            info.msg(this.getNameWithOwner() + "回复" + (getHp()-oldHp) + "点（剩余" + this.getHp() + "点生命值）");
        }else {
            info.msg(this.getNameWithOwner() + "没有回复生命值（剩余" + this.getHp() + "点生命值）");
        }
    }
    public void purify(){
        if(!atArea())return;
        info.msg(this.getNameWithOwner()+"被沉默！");
        getKeywords().clear();
        List<Effect> noLongerAtArea = new ArrayList<>(getEffects(EffectTiming.WhenNoLongerAtArea));
        getEffects().clear();
        noLongerAtArea.forEach(effect -> effect.getEffect().accept(null));
    }
    public void addStatus(int atk, int hp){
        if(atk==0 && hp==0)return;
        // region 构造消息
        StringBuilder sb = new StringBuilder();
        sb.append(this.getNameWithOwner()).append("获得了");
        if(atk>0)sb.append("+");
        sb.append(atk).append("/");
        if(hp>0)sb.append("+");
        sb.append(hp);
        info.msg(sb.toString());
        // endregion 构造消息

        int finalAtk = this.getAtk()+atk;
        int finalHp = this.getHp()+hp;
        int finalMaxHp = this.getMaxHp()+hp;
        setAtk(Math.max(0,finalAtk));
        setHp(finalHp);
        setMaxHp(finalMaxHp);
        try {
            Thread.sleep(500);
            info.pushInfo();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(getHp()<=0){
            death();
        }
    }

    public void addBlock(int n){
        setBlock(Math.max(0,getBlock() + n));
    }
    public void addAtk(int n){
        setAtk(Math.max(0,getAtk() + n));
    }
    public void addArmor(int n){
        setArmor(getArmor() + n);
    }
    public void addMagicResist(int n){
        setMagicResist(getMagicResist() + n);
    }

    public void addMaxHp(int hpMax){
        setMaxHp(getMaxHp() + hpMax);
    }
    @Override
    public String toString() {
        return "\t"+getName()+"\t生命："+getHp()+"\t攻击力："+getAtk()+"\t速度："+getSpeed()
            + "\t距离下回合："+getWaitTimeShow();
    }
}