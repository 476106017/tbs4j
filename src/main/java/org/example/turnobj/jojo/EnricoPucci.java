package org.example.turnobj.jojo;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.game.Damage;
import org.example.system.game.DamageMulti;
import org.example.system.game.Effect;
import org.example.system.game.Play;
import org.example.system.util.Lists;
import org.example.system.util.MyMath;
import org.example.turnobj.FollowCard;
import org.example.turnobj.Skill;

import java.math.BigInteger;
import java.util.List;

@Setter
@Getter
public class EnricoPucci extends FollowCard {
    private String name = "普奇神父·天堂制造";
    private int speed = 127;
    private int atk = 53;
    private int hp = 751;
    private String job = "JOJO";
    private List<String> race = Lists.ofStr("替身使者");
    private String mark = """
    每回合速度变成下一个质数
    """;
    private String subMark = "";

    @Override
    public void init() {
        super.init();
        addSkill(CosmicExpansion.class);

        addEffects((new Effect(this,this, EffectTiming.BeginTurn, obj->{
            final int speed1 = getSpeed();
            final BigInteger bigInteger = new BigInteger(String.valueOf(speed1));
            final BigInteger speed2 = bigInteger.nextProbablePrime();
            setSpeed(speed2.intValue());
        })));
    }

    @Getter
    @Setter
    public static class CosmicExpansion extends Skill {

        private String name = "宇宙膨胀";
        private String job = "JOJO";
        private List<String> race = Lists.ofStr();
        private String mark = """
        速度翻倍，并忘记其他技能！
        之后如果速度多于1000，则重启对局，并且获得【剧毒】和技能【替身攻击】
        """;
        private String subMark = "";

        public CosmicExpansion() {
            setPlay(new Play(
                () -> {
                    getBaseFollow().addSpeed(
                        getBaseFollow().getSpeed());
                    getBaseFollow().getSkills().clear();
                    getBaseFollow().getSkills().add(this);
                    if(getBaseFollow().getSpeed()>=1000){
                        info.msg("最后说一次，时间要加速了!");
                        info.resetGame();
                        final List<FollowCard> pucci = ownerPlayer().getAreaBy(p -> p instanceof EnricoPucci);
                        pucci.forEach(p->{
                            p.addKeyword("剧毒");
                            p.addSkill(StandAttack.class);
                        });
                    }
                }));
        }
    }

    @Getter
    @Setter
    public static class StandAttack extends Skill {

        private String name = "替身攻击";
        private String job = "JOJO";
        private List<String> race = Lists.ofStr();
        private String mark = """
        攻击目标造成伤害
        """;
        private String subMark = "";

        public StandAttack() {
            setPlay(new Play(
                () -> enemyPlayer().getArea(), true,
                obj->{
                    getBaseFollow().attack((FollowCard) obj);
                }));
        }
    }
}
