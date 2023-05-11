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
    transient FollowCard baseFollow;

    public abstract String getJob();
    public abstract List<String> getRace();
    public abstract String getMark();
    public abstract String getSubMark();

    private transient Play play = null;

    public void play(GameObj target){
        info.msg(info.getTurnObject().getName() + "使用了" + getName());


        // region 在使用卡牌造成任何影响前，先计算使用时
        ownerLeader().useEffects(EffectTiming.WhenPlay,this);
        enemyLeader().useEffects(EffectTiming.WhenEnemyPlay,this);
        ownerPlayer().getAreaCopy().forEach(areaCard -> areaCard.useEffects(EffectTiming.WhenPlay,this));
        enemyPlayer().getAreaCopy().forEach(areaCard -> areaCard.useEffects(EffectTiming.WhenEnemyPlay,this));
        // endregion 在使用卡牌造成任何影响前，先计算使用时


        // region 发动卡牌效果
        // 没有可选择目标时不发动效果
        if(!getPlay().canTargets().get().isEmpty() && target==null){
            info.msg(getNameWithOwner() + "因为没有目标而无法发动效果！");
        }else {
            getPlay().effect().accept(target);
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
        ownerLeader().useEffects(EffectTiming.AfterPlay,this);
        enemyLeader().useEffects(EffectTiming.AfterEnemyPlay,this);
        ownerPlayer().getAreaCopy().forEach(areaCard -> areaCard.useEffects(EffectTiming.AfterPlay,this));
        enemyPlayer().getAreaCopy().forEach(areaCard -> areaCard.useEffects(EffectTiming.AfterEnemyPlay,this));
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
