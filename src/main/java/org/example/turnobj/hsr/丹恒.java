package org.example.turnobj.hsr;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.game.Damage;
import org.example.system.game.DamageMulti;
import org.example.system.game.Effect;
import org.example.system.game.Play;
import org.example.system.util.Lists;
import org.example.turnobj.FollowCard;
import org.example.turnobj.Skill;

import java.util.List;

@Setter
@Getter
public class 丹恒 extends FollowCard {
    private String name = "丹恒";
    private String color = "#35ff93";
    private int speed = 110;
    private int atk = 74;
    private int hp = 282;
    private int armor = 54;
    private int magicResist = 32;
    private int block = 0;
    private String job = "崩铁";
    private List<String> race = Lists.ofStr("风","巡猎");
    @Override
    public void init() {
        super.init();
        addSkill(寸长寸强.class);
        addSkill(云骑枪术疾雨.class);
    }

    @Getter
    @Setter
    public static class 寸长寸强 extends Skill {

        private String name = "寸长寸强";
        private String color = "#35ff93";
        private String job = "崩铁";
        private List<String> race = Lists.ofStr("天赋");
        private String mark = """
        当丹恒成为我方技能的施放目标时，获得一层【穿透】。
        """;
        private String subMark = "";

        @Override
        public void init() {
            getBaseFollow().addEffects((new Effect(this,this, EffectTiming.EndTurn, ()->{
                getBaseFollow().removeKeyword("穿透");
            })));
            getBaseFollow().addEffects((new Effect(this,this, EffectTiming.WhenPlay, swt->{
                Skill.WithTarget withTarget = (Skill.WithTarget) swt;
                if(withTarget.target() == getBaseFollow()){
                    getBaseFollow().addKeyword("穿透");
                }
            })));
        }
    }

    @Getter
    @Setter
    public static class 云骑枪术疾雨 extends Skill {

        private String name = "云骑枪术·疾雨";
        private String color = "#35ff93";
        private String job = "崩铁";
        private List<String> race = Lists.ofStr("风");
        private String mark = """
        攻击目标, 有50%的几率造成暴击并使目标降低12%临时速度。
        """;
        private String subMark = "";

        public 云骑枪术疾雨() {
            setPlay(new Play(
                () -> enemyPlayer().getAreaGameObj(), true,
                obj->{
                    final FollowCard enemyFollow = (FollowCard) obj;
                    if(Math.random()<0.2) {
                        final Damage damage = new Damage(getBaseFollow(), enemyFollow);
                        damage.multi(2f);
                        new DamageMulti(getInfo(), List.of(damage, new Damage(enemyFollow, getBaseFollow()))).apply();

                        final double speed = enemyFollow.getSpeed() * 0.12;
                        enemyFollow.addTempSpeed(-(int)speed);
                    }else {
                        getBaseFollow().attack(enemyFollow);
                    }
                }));
        }
    }

    @Getter
    @Setter
    public static class 洞天幻化长梦一觉 extends Skill {

        private String name = "洞天幻化，长梦一觉";
        private String color = "#35ff93";
        private String job = "崩铁";
        private List<String> race = Lists.ofStr("风");
        private String mark = """
        攻击目标, 有50%的几率造成暴击。如果目标临时速度被降低，将造成3倍暴击。
        """;
        private String subMark = "";

        public 洞天幻化长梦一觉() {
            setChargeSpeed(50);
            setPlay(new Play(
                () -> enemyPlayer().getAreaGameObj(), true,
                obj->{
                    final FollowCard enemyFollow = (FollowCard) obj;
                    if(Math.random()<0.5) {
                        final Damage damage = new Damage(getBaseFollow(), enemyFollow);
                        damage.multi(enemyFollow.getTempSpeed()<0?3f:2f);
                        new DamageMulti(getInfo(), List.of(damage, new Damage(enemyFollow, getBaseFollow()))).apply();

                        final double speed = enemyFollow.getSpeed() * 0.12;
                        enemyFollow.addTempSpeed(-(int)speed);
                    }else {
                        getBaseFollow().attack(enemyFollow);
                    }
                }));
        }
    }
}
