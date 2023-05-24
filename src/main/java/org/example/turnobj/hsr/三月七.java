package org.example.turnobj.hsr;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.game.Damage;
import org.example.system.game.Effect;
import org.example.system.game.Play;
import org.example.system.util.Lists;
import org.example.turnobj.FollowCard;
import org.example.turnobj.Skill;

import java.util.List;

@Setter
@Getter
public class 三月七 extends FollowCard {
    private String name = "三月七";
    private String color = "#13d0ff";
    private int speed = 101;
    private int atk = 37;
    private int hp = 410;
    private int armor = 152;
    private int magicResist = 78;
    private int block = 0;
    private String job = "崩铁";
    private List<String> race = Lists.ofStr("冰","存护");
    @Override
    public void init() {
        super.init();
        getKeywords().add("远程");
        addSkill(少女的特权.class);
        addSkill(极寒的弓矢.class);
        addSkill(可爱即是正义.class);
        addSkill(冰刻箭雨之时.class);
    }

    @Getter
    @Setter
    public static class 少女的特权 extends Skill {

        private String name = "少女的特权";
        private String color = "#13d0ff";
        private String job = "崩铁";
        private List<String> race = Lists.ofStr("天赋");
        private String mark = """
        每回合2次，当持有格挡的友方受到伤害时，三月七立即对目标进行反击
        """;
        private String subMark = "";

        @Override
        public void init() {
            getBaseFollow().addEffects((new Effect(this,this, EffectTiming.BeginTurn, ()->{
                getBaseFollow().setCount("反击次数",2);
            })));
            getBaseFollow().addEffects((new Effect(this,this, EffectTiming.AfterFollowBlock, dmg->{
                Damage damage = (Damage) dmg;
                if(damage.getFrom() instanceof FollowCard followCard && getBaseFollow().getCount("反击次数")>0){
                    try {
                        Thread.sleep(500);
                        info.pushInfo();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    info.msg("三月七的反击！");
                    getBaseFollow().attack(followCard);
                    getBaseFollow().count("反击次数",-1);
                }
            })));
        }
    }

    @Getter
    @Setter
    public static class 极寒的弓矢 extends Skill {

        private String name = "极寒的弓矢";
        private String color = "#13d0ff";
        private String job = "崩铁";
        private List<String> race = Lists.ofStr("冰");
        private String mark = """
        攻击目标, 有20%的几率使目标速度-10。
        """;
        private String subMark = "";

        public 极寒的弓矢() {
            setPlay(new Play(
                () -> enemyPlayer().getAreaGameObj(), true,
                obj->{
                    final FollowCard enemyFollow = (FollowCard) obj;
                    getBaseFollow().attack(enemyFollow);
                    if(Math.random()<0.2){
                        enemyFollow.addSpeed(-10);
                    }
                }));
        }
    }

    @Getter
    @Setter
    public static class 可爱即是正义 extends Skill {

        private String name = "可爱即是正义";
        private String color = "#13d0ff";
        private String job = "崩铁";
        private List<String> race = Lists.ofStr("冰");
        private String mark = """
        给予友方随从80点格挡
        """;
        private String subMark = "";

        public 可爱即是正义() {
            setPlay(new Play(
                () -> ownerPlayer().getAreaGameObj(), true,
                obj->{
                    final FollowCard follow = (FollowCard) obj;
                    follow.addBlock(80);
                }));
        }
    }

    @Getter
    @Setter
    public static class 冰刻箭雨之时 extends Skill {

        private String name = "冰刻箭雨之时";
        private String color = "#13d0ff";
        private String job = "崩铁";
        private List<String> race = Lists.ofStr("冰");
        private String mark = """
        敌方全体受到80点伤害，分别有50%概率-20临时速度
        """;
        private String subMark = "";

        public 冰刻箭雨之时() {
            setChargeSpeed(35);
            setPlay(new Play(
                ()->{
                    final List<FollowCard> area = enemyPlayer().getAreaCopy();
                    info.damageMulti(getBaseFollow(), area,20);
                    area.forEach(followCard -> {
                        if(followCard.atArea()){
                            if(Math.random()<0.5){
                                followCard.addTempSpeed(-20);
                            }
                        }
                    });
                }));
        }
    }

}
