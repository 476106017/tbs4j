package org.example.turnobj;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.CardType;
import org.example.constant.EffectTiming;
import org.example.system.game.Damage;
import org.example.system.game.Effect;
import org.example.system.game.EventType;

import java.util.*;
import java.util.stream.Collectors;

import static org.example.constant.CounterKey.DEATH_PREFIX;
import static org.example.constant.CounterKey.DEFAULT;

@Getter
@Setter
public abstract class SpellCard extends GameObj {

    public final CardType TYPE = CardType.SPELL;
    private transient String color = "#000";
    private List<Skill> skills = new ArrayList<>();
    public <T extends Skill> void addSkill(Class<T> skill){
        try {
            T card = skill.getDeclaredConstructor().newInstance();
            try {
                info.msg(getNameWithOwner()+"创造了"+card.getId());
            }catch (Exception ignored){}
            card.setOwner(getOwner());
            card.setInfo(getInfo());
            card.setBaseFollow(null);
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
    public abstract String getJob();
    public abstract List<String> getRace();

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

    public void purify(){
        info.msg(this.getNameWithOwner()+"被净化！");
        getKeywords().clear();
        List<Effect> noLongerAtArea = new ArrayList<>(getEffects(EffectTiming.WhenNoLongerAtArea));
        getEffects().clear();
        noLongerAtArea.forEach(effect -> effect.getEffect().accept(null));
    }
}