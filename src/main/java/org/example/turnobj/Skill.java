package org.example.turnobj;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.game.Play;

import java.util.List;

@Getter
@Setter
public abstract class Skill extends GameObj {
    int speed = 0;
    int charge = 0;
    int chargeSpeed = 100;
    transient FollowCard baseFollow;

    public abstract String getJob();
    public abstract List<String> getRace();
    public abstract String getMark();
    public abstract String getSubMark();

    private transient Play play = null;

    private boolean interrupt = false;

    public record WithTarget(Skill skill, FollowCard target){};
    public void play(FollowCard target){
        if(getRace().contains("天赋")){
            info.msgToThisPlayer("无法主动使用天赋！");
            return;
        }
        if(charge<100){
            info.msgToThisPlayer("技能尚未准备好！");
            return;
        }
        charge = 0;
        info.msg(info.getTurnObject().getName() + "使用了" + getName());

        final WithTarget withTarget = new WithTarget(this, target);
        // region 在使用卡牌造成任何影响前，先计算使用时
        ownerLeader().useEffects(EffectTiming.WhenPlay,withTarget);
        enemyLeader().useEffects(EffectTiming.WhenEnemyPlay,withTarget);
        ownerPlayer().getAreaCopy().forEach(areaCard -> areaCard.useEffects(EffectTiming.WhenPlay,withTarget));
        enemyPlayer().getAreaCopy().forEach(areaCard -> areaCard.useEffects(EffectTiming.WhenEnemyPlay,withTarget));
        // endregion 在使用卡牌造成任何影响前，先计算使用时

        if(interrupt){
            interrupt = false;
            return;
        }

        // region 发动卡牌效果
        // 没有可选择目标时不发动效果
        if(!getPlay().canTargets().get().isEmpty() && withTarget.target()==null){
            info.msg(getNameWithOwner() + "因为没有目标而无法发动效果！");
        }else {
            getPlay().effect().accept(withTarget.target());
        }
        // endregion 发动卡牌效果

        if(getInfo().isReset()){
            getInfo().setReset(false);
            getInfo().pushInfo();
            return;// 重置对局后前一对局的结束回合忽略掉
        }

        ownerPlayer().count("连续行动次数-"+getName());

        getInfo().getPlayedSkills().add(this);

        // region 计算使用后
        ownerLeader().useEffects(EffectTiming.AfterPlay,withTarget);
        enemyLeader().useEffects(EffectTiming.AfterEnemyPlay,withTarget);
        ownerPlayer().getAreaCopy().forEach(areaCard -> areaCard.useEffects(EffectTiming.AfterPlay,withTarget));
        enemyPlayer().getAreaCopy().forEach(areaCard -> areaCard.useEffects(EffectTiming.AfterEnemyPlay,withTarget));
        // endregion 计算使用后
        info.startEffect();


        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        info.endTurnOfCommand();

    }


    @Override
    public void addSpeed(int speed) {
        getBaseFollow().addSpeed(speed);
    }

    @Override
    public void addTempSpeed(int speed) {
        getBaseFollow().addTempSpeed(speed);
    }
}
