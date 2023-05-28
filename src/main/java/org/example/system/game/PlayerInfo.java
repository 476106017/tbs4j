package org.example.system.game;

import jakarta.websocket.Session;
import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.util.Lists;
import org.example.turnobj.FollowCard;
import org.example.turnobj.GameObj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Setter
public class PlayerInfo implements Serializable {
    transient GameInfo info;

    String name;
    transient Session session;
    boolean initative;// 先攻
    boolean shortRope = false;
    int areaMax = 4;
    List<FollowCard> area = new ArrayList<>();
    transient List<FollowCard> graveyard = new ArrayList<>();
    Integer graveyardCount = 0;// 当墓地消耗时，只消耗计数，不消耗真实卡牌

    transient List<FollowCard> deck = new ArrayList<>();

    Map<String,Integer> counter = new ConcurrentHashMap<>();// 计数器
    Leader leader;

    public void setLeader(Leader leader) {
        this.leader = leader;
        info.msg(getName()+"的主战者变成了"+leader.getName());
    }

    public PlayerInfo(GameInfo info,boolean initative) {
        this.info = info;
        this.initative = initative;
    }

    public PlayerInfo getEnemy(){
        return info.anotherPlayerBySession(getSession());
    }

    public Integer getCount(String key){
        return Optional.ofNullable(counter.get(key)).orElse(0);
    }
    public void count(String key){
        count(key,1);
    }
    public void clearCount(String key){
        counter.remove(key);
    }
    public void clearCountLike(String keyLike){
        counter.keySet().stream().filter(k->k.contains(keyLike)).forEach(k->{
            counter.remove(k);
        });
    }
    public void count(String key,int time){
        counter.merge(key, time, (a, b) -> Math.max(0,Integer.sum(a, b)));
    }

    // 卡牌的快照。用来循环（原本卡牌List可以随便删）
    public List<FollowCard> getAreaCopy(){
        return new ArrayList<>(getArea());
    }
    public List<GameObj> getAreaGameObj(){
        return getArea().stream()
            .map(areaCard -> (GameObj)areaCard)
            .toList();
    }

    public List<FollowCard> getAreaBy(Predicate<FollowCard> p){
        return getArea().stream().filter(p).toList();
    }

    public FollowCard getAreaRandomFollow(){
        return Lists.randOf(getArea());
    }

    public void addArea(FollowCard areaCard){
        addArea(List.of(areaCard));
    }
    public void addArea(List<FollowCard> cards){
        int i = cards.size() + area.size() - areaMax;
        List<FollowCard> exileCards = new ArrayList<>();
        if(i>0){
            exileCards = cards.subList(cards.size() - i, cards.size());
            info.msg(getName()+"的战场放不下了，多出的"+i+"张牌从游戏中除外！");
        }
        getArea().addAll(cards);
        getArea().removeAll(exileCards);
        info.tempEffectBatch(cards,EffectTiming.WhenAtArea);
    }
    public void addGraveyard(FollowCard card){
        addGraveyard(List.of(card));
    }
    public void addGraveyard(List<FollowCard> cards){
        String cardNames = cards.stream().map(FollowCard::getName).collect(Collectors.joining("、"));
        if(cards.size()<10)
            info.msgTo(getSession(),cardNames + "加入了墓地");
        else
            info.msgTo(getSession(),cards.size() + "张牌加入了墓地");
        info.msgTo(getEnemy().getSession(),cards.size() + "张牌加入了对手墓地");
        graveyardCount+=cards.size();
        graveyard.addAll(cards);
    }


    public void summon(FollowCard summonedCard){
        summon(List.of(summonedCard));
    }

    public void summon(List<FollowCard> summonedCards){
        info.msg(getName() + "召唤了" + summonedCards.stream().map(FollowCard::getId).collect(Collectors.joining("、")));
        addArea(summonedCards);
        info.useEffectBatch(summonedCards, EffectTiming.Entering);

        List<FollowCard> areaCards = getAreaBy(areaCard -> !summonedCards.contains(areaCard));
        info.useEffectBatch(areaCards,EffectTiming.WhenSummon,summonedCards);
        getLeader().useEffects(EffectTiming.WhenSummon,summonedCards);

        List<FollowCard> enemyAreaCards = getEnemy().getAreaBy(areaCard -> !summonedCards.contains(areaCard));
        info.useEffectBatch(enemyAreaCards,EffectTiming.WhenEnemySummon,summonedCards);
        getLeader().useEffects(EffectTiming.WhenEnemySummon,summonedCards);

        info.turn.addObject(summonedCards);
    }
}
