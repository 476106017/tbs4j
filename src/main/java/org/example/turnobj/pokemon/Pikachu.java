package org.example.turnobj.pokemon;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.game.Effect;
import org.example.system.game.Play;
import org.example.turnobj.FollowCard;
import org.example.turnobj.Skill;
import org.example.system.util.Lists;

import java.util.List;

@Setter
@Getter
public class Pikachu extends FollowCard {
    private String name = "皮卡丘";
    private String color = "#fff816";
    private int speed = 90;
    private int atk = 55;
    private int hp = 350;
    private int armor = -10;
    private int magicResist = 20;
    private int block = 0;
    private String job = "宝可梦";
    private List<String> race = Lists.ofStr("电");
    private String mark = """
    每回合速度+1
    """;
    private String subMark = "";

    @Override
    public void init() {
        super.init();
        addSkill(JuumannBoruto.class);
        addSkill(Denngekiha.class);

        addEffects((new Effect(this,this, EffectTiming.BeginTurn, obj->
            addSpeed(1)
        )));
    }

    @Getter
    @Setter
    public static class JuumannBoruto extends Skill {

        private String name = "十万伏特";
        private String color = "#fff816";
        private String job = "宝可梦";
        private List<String> race = Lists.ofStr("电");
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

    @Getter
    @Setter
    public static class Denngekiha extends Skill {

        private String name = "电击";
        private String color = "#fff816";
        private String job = "宝可梦";
        private List<String> race = Lists.ofStr("电");
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
}
