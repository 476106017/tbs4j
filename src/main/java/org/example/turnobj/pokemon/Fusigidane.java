package org.example.turnobj.pokemon;

import lombok.Getter;
import lombok.Setter;
import org.example.system.game.Damage;
import org.example.system.game.DamageMulti;
import org.example.system.game.Play;
import org.example.turnobj.FollowCard;
import org.example.turnobj.Skill;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Fusigidane extends FollowCard {
    private String name = "妙蛙种子";
    private int speed = 45;
    private int atk = 49;
    private int hp = 450;
    private String job = "宝可梦";
    private List<String> race = Lists.ofStr("草","毒");
    private String mark = """
    """;
    private String subMark = "";

    @Override
    public void init() {
        super.init();
        addSkill(Tsurunomuti.class);
        addSkill(Happakattaa.class);
        addSkill(Sooraabiimu.class);
    }

    @Getter
    @Setter
    public static class Tsurunomuti extends Skill {

        private String name = "藤鞭";
        private String job = "宝可梦";
        private List<String> race = Lists.ofStr("草");
        private String mark = """
        攻击目标造成伤害。
        """;
        private String subMark = "";

        public Tsurunomuti() {
            setPlay(new Play(
                () -> enemyPlayer().getAreaGameObj(), true,
                obj->{
                    final FollowCard enemyFollow = (FollowCard) obj;
                    getBaseFollow().attack(enemyFollow);
                }));
        }
    }


    @Getter
    @Setter
    public static class Happakattaa extends Skill {

        private String name = "飞叶快刀";
        private String job = "宝可梦";
        private List<String> race = Lists.ofStr("草");
        private String mark = """
        -10临时速度，然后攻击目标造成伤害。
        该攻击有20%几率暴击，
        """;
        private String subMark = "";

        public Happakattaa() {
            setPlay(new Play(
                () -> enemyPlayer().getAreaGameObj(), true,
                obj->{
                    addTempSpeed(-10);
                    final FollowCard enemyFollow = (FollowCard) obj;
                    if(Math.random()<0.2){
                        final Damage damage = new Damage(getBaseFollow(), enemyFollow);
                        damage.multi(2f);
                        new DamageMulti(getInfo(),List.of(damage, new Damage(enemyFollow,getBaseFollow()))).apply();
                    }else {
                        getBaseFollow().attack(enemyFollow);
                    }
                }));
        }
    }

    @Getter
    @Setter
    public static class Sooraabiimu extends Skill {

        private String name = "日光束";
        private String job = "宝可梦";
        private List<String> race = Lists.ofStr("草");
        private String mark = """
        第１回合收集满满的日光，第２回合发射光束进行攻击，造成120点伤害。
        """;
        private String subMark = "";

        private boolean ready = false;

        public Sooraabiimu() {
            setPlay(new Play(
                () -> {
                    if(ready)
                        return enemyPlayer().getAreaGameObj();
                    else
                        return new ArrayList<>();
                }, false,
                obj->{
                    if(!ready){
                        info.msg(getNameWithOwner()+"吸收了光！");
                    }else {
                        final FollowCard enemyFollow = (FollowCard) obj;
                        info.damageEffect(getBaseFollow(), enemyFollow,120);
                    }
                    ready = !ready;
                }));
        }
    }

}
