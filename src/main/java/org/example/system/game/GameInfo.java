package org.example.system.game;

import jakarta.websocket.Session;
import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.effectobj.Fire;
import org.example.turnobj.CountCard;
import org.example.turnobj.FollowCard;
import org.example.turnobj.GameObj;
import org.example.turnobj.Skill;
import org.example.system.util.Maps;
import org.example.system.util.Msg;
import org.example.system.util.TurnWrapper;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.example.system.Database.*;

@Getter
@Setter
public class GameInfo implements Serializable {
    String room;

    boolean inSettle = false;
    int chainDeep = 3;
    boolean isReset = false;
    TurnWrapper turn;
    GameObj turnObject;
    int turnPlayer;
    int moreTurn = 0;// 追加回合
    boolean gameset = false;
    ScheduledFuture<?> rope;
    Map<FollowCard,EventType> events = new HashMap<>();
    List<Effect.EffectInstance> effectInstances = new LinkedList<>();

    List<Damage> incommingDamages = new ArrayList<>();
    public boolean hasEvent(){
        return !incommingDamages.isEmpty() || !events.isEmpty();
    }

    PlayerInfo[] playerInfos;

    List<Skill> playedSkills = new ArrayList<>();// 使用技能计数器

    public GameInfo(String room) {
        this.room = room;
        this.turn = new TurnWrapper();
        this.turnPlayer = 0;
        this.playerInfos = new PlayerInfo[2];
        this.playerInfos[0] = new PlayerInfo(this,true);
        this.playerInfos[1] = new PlayerInfo(this,false);

    }

    public void resetGame(){
        msg("游戏重启！");
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        roomSchedule.get(getRoom()).shutdown();
        roomSchedule.remove(getRoom());
        rope.cancel(true);
        this.isReset = true;
        this.turn = new TurnWrapper();
        this.turnPlayer = 0;
        Session thisSession = thisPlayer().session;
        Session oppoSession = oppositePlayer().session;
        this.playerInfos = new PlayerInfo[2];
        this.playerInfos[0] = new PlayerInfo(this,true);
        this.playerInfos[1] = new PlayerInfo(this,false);
        zeroTurn(thisSession,oppoSession);
    }

    public void msg(String msg){
        try {
            Msg.send(thisPlayer().getSession(),msg);
            Msg.send(oppositePlayer().getSession(),msg);
        } catch (Exception ignored) {}
    }
    public void msgTo(Session session, String msg){
        Msg.send(session,msg);
    }

    public void msgToThisPlayer(String msg){
        Msg.send(thisPlayer().getSession(),msg);
    }
    public void msgToOppositePlayer(String msg){
        Msg.send(oppositePlayer().getSession(),msg);
    }

    public void measureLeader(){
        if(thisPlayer().getArea().size()<=0)
            gameset(oppositePlayer());
        if(oppositePlayer().getArea().size()<=0)
            gameset(thisPlayer());
    }
    public void measureFollows(){
        // 立即结算受伤状态
        List<Damage> incommingDamagesCopy = new ArrayList<>(incommingDamages);
        incommingDamages = new ArrayList<>();
        incommingDamagesCopy.forEach(damage->{
            damage.getTo().useEffects(EffectTiming.AfterDamaged,damage);
            damage.getTo().ownerPlayer().getAreaCopy().forEach(followCard -> {
                followCard.useEffects(EffectTiming.AfterAreaFollowDamaged,damage);
                if(damage.isBreakBlock()){
                    followCard.useEffects(EffectTiming.AfterFollowBlock,damage);
                }
            });
        });

        Map<FollowCard, EventType> eventsCopy = events;
        events = new HashMap<>();
        // 再结算其他状态
        eventsCopy.forEach((card, type) -> {
            switch (type){
                case Destroy -> {
                    card.destroyed();
                }
            }
        });

        assert events.isEmpty();
    }
    public void gameset(PlayerInfo winner){
        gameset = true;
        msg("游戏结束，获胜者："+winner.getName());
        pushInfo();
        final Session winnerSession = winner.getSession();
        Msg.send(winnerSession,"alert","你赢了！");
        Msg.send(anotherPlayerBySession(winnerSession).getSession(),"alert","你输了！");

        // 释放资源
        roomGame.remove(getRoom());
        // 退出房间
        try {
            userRoom.remove(thisPlayer().getSession());
            msgToThisPlayer("离开房间成功");
            userRoom.remove(oppositePlayer().getSession());
            msgToOppositePlayer("离开房间成功");

            rope.cancel(true);
            ScheduledExecutorService ses = roomSchedule.get(getRoom());
            ses.shutdown();
            roomSchedule.remove(getRoom());
        }catch (Exception e){e.printStackTrace();}
        throw new RuntimeException("Game Set");
    }

    public PlayerInfo thisPlayer(){
        return playerInfos[turnPlayer];
    }
    public PlayerInfo oppositePlayer(){
        return playerInfos[1-turnPlayer];
    }
    public PlayerInfo playerBySession(Session session){
        if(playerInfos[0].session == session){
            return playerInfos[0];
        }else {
            return playerInfos[1];
        }
    }
    public PlayerInfo anotherPlayerBySession(Session session){
        if(playerInfos[0].session == session){
            return playerInfos[1];
        }else {
            return playerInfos[0];
        }
    }


    public List<FollowCard> getAreaCardsCopy(){
        List<FollowCard> _result = new ArrayList<>();
        _result.addAll(thisPlayer().getArea());
        _result.addAll(oppositePlayer().getArea());
        return _result;
    }
    public List<GameObj> getTargetableGameObj(){
        List<GameObj> _result = new ArrayList<>();
        _result.addAll(getAreaCardsCopy());
        _result.add(thisPlayer().getLeader());
        _result.add(oppositePlayer().getLeader());
        return _result;
    }

    // region turn
    public void zeroTurn(Session u0, Session u1){

        PlayerInfo p0 = thisPlayer();
        PlayerDeck playerDeck0 = userDecks.get(u0);
        p0.setSession(u0);
        p0.setName(userNames.get(u0));
        p0.setLeader(playerDeck0.getLeader(0, this));
        p0.setDeck(playerDeck0.getActiveDeckInstance(0, this));

        PlayerInfo p1 = oppositePlayer();
        PlayerDeck playerDeck1 = userDecks.get(u1);
        p1.setSession(u1);
        p1.setName(userNames.get(u1));
        p1.setLeader(playerDeck1.getLeader(1, this));
        p1.setDeck(playerDeck1.getActiveDeckInstance(1, this));

        p0.getLeader().init();
        p1.getLeader().init();

        p0.getArea().addAll(p0.getDeck());
        turn.addObject(p0.getDeck());
        p1.getArea().addAll(p1.getDeck());
        turn.addObject(p1.getDeck());

        beginGame();
    }
    public void beginGame(){
        thisPlayer().getLeader().useEffects(EffectTiming.BeginGame);
        oppositePlayer().getLeader().useEffects(EffectTiming.BeginGame);
        msg("游戏开始！");
        Msg.send(thisPlayer().getSession(),"startGame","");
        Msg.send(oppositePlayer().getSession(),"startGame","");
        roomSchedule.put(room, Executors.newScheduledThreadPool(1));// 房间里面放一个计时器

        turnObject = turn.nextObjectTurn();
        turnPlayer = turnObject.getOwner();

        startTurn();
    }

    public void startTurn(){
        beforeTurn();

        if(thisPlayer().isShortRope()){
            rope = roomSchedule.get(getRoom()).schedule(this::endTurnOfTimeout, 30, TimeUnit.SECONDS);
            msg("倒计时30秒！");
        }else{
            rope = roomSchedule.get(getRoom()).schedule(this::endTurnOfTimeout, 300, TimeUnit.SECONDS);
            msg("倒计时300秒！");
        }

        if(getTurnObject() instanceof FollowCard followCard){
            followCard.removeKeyword("混乱");
            followCard.removeKeyword("离神");
            followCard.removeKeyword("法力流失");

            if(followCard.hasKeyword("混乱")){
                if(Math.random()<0.3333){
                    msg(followCard.getNameWithOwner()+"陷入混乱！");
                    damageEffect(followCard,followCard,40);
                    endTurnOfCommand();
                    return;
                }
            }
            if(followCard.hasKeyword("离神")){
                msg(turnObject.getNameWithOwner()+"跳过回合！");
                endTurnOfCommand();
                return;
            }
        }

        pushInfo();
        msg(turnObject.getNameWithOwner()+"的回合");


        if(getTurnObject() instanceof CountCard countCard){
            msg(turnObject.getNameWithOwner()+"触发了效果！");
            countCard.getExec().accept(countCard.getTarget());
            try {
                Thread.sleep(200);
                pushInfo();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            endTurnOfCommand();
        }
    }

    public void endTurnOfTimeout(){
        thisPlayer().setShortRope(true);
        endTurn();
    }
    public void endTurnOfCommand(){
        thisPlayer().setShortRope(false);
        rope.cancel(true);
        endTurn();
    }

    public void endTurn(){
        if(thisPlayer().getDiscoverNum() == -1){
            thisPlayer().getDiscoverThread().run();// 就用run，不要异步
            thisPlayer().setDiscoverNum(0);
        }
        getTurnObject().endTurn();
        msg(turnObject.getNameWithOwner()+"的回合结束");

        if(getTurnObject() instanceof FollowCard followCard){

            if(followCard.hasKeyword("灼伤")){
                damageEffect(new Fire(),followCard,followCard.getHp()/16);
                followCard.removeKeyword("灼伤");
            }

            // 发动回合结束效果
            if(followCard.atArea()){
                followCard.useEffects(EffectTiming.EndTurn);
            }
        }else if(getTurnObject() instanceof CountCard countCard){
            getTurn().removeObject(countCard);
        }

        // 发动主战者效果
        Leader leader = thisPlayer().getLeader();
        leader.useEffects(EffectTiming.EndTurn);
        leader.expireEffect();

        Leader enemyLeader = oppositePlayer().getLeader();
        enemyLeader.useEffects(EffectTiming.EnemyEndTurn);
        enemyLeader.expireEffect();

        // 是否有追加回合
        if(getTurnObject() instanceof FollowCard  followCard && followCard.atArea()
            && moreTurn>0){
            moreTurn--;
            msg("回合继续！");
        }else {
            // 从回合模型找到下一个对象
            final int oldTurnPlayer = turnPlayer;
            turnObject = turn.nextObjectTurn();
            // 还原临时速度
            turnObject.addSpeed(-turnObject.getTempSpeed());
            turnObject.setTempSpeed(0);

            turnPlayer = turnObject.getOwner();
            if(oldTurnPlayer!=turnPlayer){
                oppositePlayer().clearCountLike("连续行动次数-");
            }
        }
        startTurn();
    }

    public void beforeTurn(){

        oppositePlayer().getAreaCopy().forEach(enemyAreaCard -> {
            if(!enemyAreaCard.atArea())return;
            enemyAreaCard.useEffects(EffectTiming.EnemyBeginTurn);
        });
        thisPlayer().getAreaCopy().forEach(areaCard -> {
            if(!areaCard.atArea())return;
            areaCard.useEffects(EffectTiming.BeginTurn);
        });

        if(turnObject instanceof FollowCard followCard){
            followCard.getSkills().forEach(skill -> {
                final int charge = skill.getCharge();
                if(charge < 100){
                    if(skill.getChargeSpeed()<100 && followCard.hasKeyword("法力流失")){
                        msg(skill.getBaseFollow().getNameWithOwner() + "遭遇了法力流失！");
                    }else {
                        skill.setCharge(Math.min(100,charge + skill.getChargeSpeed()));
                    }
                }
            });
        }
    }

    public void addMoreTurn(){
        moreTurn++;
    }
    // endregion turn

    public boolean addEvent(FollowCard card,EventType type){
        EventType oldType = events.get(card);
        if(oldType != null){
            return false;
        }
//        msg(card.getNameWithOwner() + "的" + type.getName() + "状态已加入队列");
        events.put(card,type);
        return true;
    }
    // 结算效果
    public void startEffect(){

        if(inSettle)return;
        inSettle = true;
//        msg("——————开始结算——————");

        consumeEffectChain(chainDeep);
        // 计算主战者死亡状况
        measureLeader();
        inSettle = false;
    }
    public void consumeEffectChain(int deep){
//        msg("——————开始触发事件——————");
        measureFollows();
//        msg("——————开始触发效果——————");
        consumeEffect();
//        msg("——————停止触发效果——————");

        if(hasEvent()){
            if(deep==0){
                msg("停止连锁！本次死亡结算后不触发任何效果");
                measureFollows();
                effectInstances.clear();
                events.clear();
                return;
            }
//            msg("——————事件连锁（"+deep+"）——————");
            consumeEffectChain(deep - 1);
        }
    }
    public void consumeEffect(){
        if(effectInstances.isEmpty()) return;
        effectInstances.sort((o1, o2) -> {
            for (EffectTiming value : EffectTiming.values()) {
                if(value.equals(o1.getEffect().getTiming()))
                    return -1;
                else if(value.equals(o2.getEffect().getTiming()))
                    return 1;
            }
            return 0;
        });

        List<Effect.EffectInstance> instances = new ArrayList<>(effectInstances);

        instances.forEach(Effect.EffectInstance::consume);

        effectInstances.clear();
    }
    public void tempEffect(Effect.EffectInstance instance){
        Effect effect = instance.getEffect();
        effectInstances.add(instance);
//        msg(effect.getOwnerObj().getNameWithOwner()+"的【"+effect.getTiming().getName()+"】效果已加入队列" +
//            "（队列现在有" + effectInstances.size() + "个效果）");
    }
    public void tempEffectBatch(List<FollowCard> objs, EffectTiming timing,Object param){
        objs.forEach(obj -> obj.tempEffects(timing,param));
    }
    public void tempEffectBatch(List<FollowCard> objs, EffectTiming timing){
        objs.forEach(obj -> obj.tempEffects(timing));
    }
    public void useEffectBatch(List<FollowCard> objs, EffectTiming timing,Object param){
        tempEffectBatch(objs,timing,param);
        startEffect();
    }
    public void useEffectBatch(List<FollowCard> objs, EffectTiming timing){
        tempEffectBatch(objs,timing);
        startEffect();
    }

    public void damageMulti(FollowCard from,List<FollowCard> objs, int damage){
        List<Damage> damages = objs.stream().filter(Objects::nonNull).map(obj -> new Damage(from, obj, damage)).toList();
        new DamageMulti(this,damages).apply();
    }
    public void damageAttacking(FollowCard from, FollowCard to){
        if(!from.hasKeyword("远程") && !to.hasKeyword("眩晕"))
            new DamageMulti(this,List.of(new Damage(from,to), new Damage(to,from))).apply();
        else
            new DamageMulti(this,List.of(new Damage(from,to))).apply();
    }
    public void damageEffect(GameObj from,FollowCard to, int damage){
        new DamageMulti(this,List.of(new Damage(from,to,damage))).apply();
    }
    public void pushInfo(){
        final PlayerInfo thisPlayer = thisPlayer();
        final PlayerInfo oppositePlayer = oppositePlayer();

        Msg.send(thisPlayer.getSession(),"battleInfo",
            Maps.newMap("me", thisPlayer,"enemy", oppositePlayer, "turn", turn.listWithOffsetWaitTime(thisPlayer)));
        Msg.send(oppositePlayer.getSession(),"battleInfo",
            Maps.newMap("me", oppositePlayer,"enemy", thisPlayer, "turn", turn.listWithOffsetWaitTime(oppositePlayer)));
    }

}
