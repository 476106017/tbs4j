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
public class Hinoko extends Skill {

    private String name = "火花";
    private String job = "宝可梦";
    private List<String> race = Lists.ofStr("火");
    private String mark = """
    造成40点伤害, 有30%几率对手陷入3回合【灼伤】状态
    """;
    private String subMark = "";

    public Hinoko() {
        setPlay(new Play(
            () -> enemyPlayer().getAreaGameObj(), true,
            obj->{
                final FollowCard enemyFollow = (FollowCard) obj;
                info.damageEffect(getBaseFollow(), enemyFollow,40);
                if(Math.random()<3){
                    enemyFollow.addKeywordN("灼伤",3);
                }
            }));
    }
}
