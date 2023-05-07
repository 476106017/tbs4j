package org.example.system.turnobj.pokemon.skill;

import lombok.Getter;
import lombok.Setter;
import org.example.system.game.Play;
import org.example.system.turnobj.FollowCard;
import org.example.system.turnobj.Skill;
import org.example.system.util.Lists;
import org.example.system.util.MyMath;

import java.util.List;

@Getter
@Setter
public class JuumannBoruto extends Skill {

    private String name = "十万伏特";
    private String job = "宝可梦";
    private List<String> race = Lists.ofStr("电气");
    private String mark = """
    向对手发出强力电击，造成90点伤害
    10%几率让对手陷入麻痹状态，减少50%速度1回合。
    """;
    private String subMark = "";

    public JuumannBoruto() {
        setPlay(new Play(
            () -> enemyPlayer().getAreaGameObj(), true,
            obj->{
                final FollowCard enemyFollow = (FollowCard) obj;
                info.damageEffect(getBaseFollow(), enemyFollow,90);
                if(Math.random()<0.1){
                    final int speed = enemyFollow.getSpeed();
                    enemyFollow.addTempSpeed((int) (-0.5*speed));
                    info.msg(enemyFollow.getNameWithOwner()+"陷入了麻痹状态！");
                }
            }));
    }
}
