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
public class Denngekiha extends Skill {

    private String name = "电击";
    private String job = "宝可梦";
    private List<String> race = Lists.ofStr("电气");
    private String mark = """
    造成40点伤害, 永久增加2点速度。
    """;
    private String subMark = "";

    public Denngekiha() {
        setPlay(new Play(
            () -> enemyPlayer().getAreaGameObj(), true,
            obj->{
                final FollowCard enemyFollow = (FollowCard) obj;
                info.damageEffect(getBaseFollow(), enemyFollow,40);
                addSpeed(2);
            }));
    }
}
