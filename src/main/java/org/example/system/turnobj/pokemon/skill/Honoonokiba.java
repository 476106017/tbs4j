package org.example.system.turnobj.pokemon.skill;

import lombok.Getter;
import lombok.Setter;
import org.example.system.game.Play;
import org.example.system.turnobj.FollowCard;
import org.example.system.turnobj.Skill;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class Honoonokiba extends Skill {

    private String name = "火焰牙";
    private String job = "宝可梦";
    private List<String> race = Lists.ofStr("火");
    private String mark = """
    攻击目标, 有10%的几率使目标陷入3回合【灼伤】状态。
    如果目标回合倒计时多于50，则有10%的几率使目标陷入1回合【离神】状态。
    """;
    private String subMark = "";

    public Honoonokiba() {
        setPlay(new Play(
            () -> enemyPlayer().getAreaGameObj(), true,
            obj->{
                final FollowCard enemyFollow = (FollowCard) obj;
                getBaseFollow().attack(enemyFollow);
                if(Math.random()<0.1){
                    enemyFollow.addKeywordN("灼伤",3);
                }
                if(enemyFollow.getWaitTimeShow()>=50 && Math.random()<1){
                    enemyFollow.addKeyword("离神");
                }
            }));
    }
}
