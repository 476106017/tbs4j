package org.example.turnobj.hsr;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.game.Effect;
import org.example.system.game.Play;
import org.example.system.util.Lists;
import org.example.turnobj.FollowCard;
import org.example.turnobj.GameObj;
import org.example.turnobj.Skill;

import java.util.List;

@Setter
@Getter
public class 开拓者 extends FollowCard {
    private String name = "开拓者";
    private String color = "#b6aeae";
    private int speed = 100;
    private int atk = 84;
    private int hp = 163;
    private int armor = 122;
    private int magicResist = 65;
    private int block = 0;
    private String job = "崩铁";
    private List<String> race = Lists.ofStr("物理","毁灭");

    @Override
    public void init() {
        super.init();
        addSkill(牵制盗垒.class);
        addSkill(再见安打.class);
        addSkill(安息全垒打.class);
    }

    @Getter
    @Setter
    public static class 牵制盗垒 extends Skill {

        private String name = "牵制盗垒";
        private String color = "#b6aeae";
        private String job = "崩铁";
        private List<String> race = Lists.ofStr("天赋");
        private String mark = """
        击碎格挡时，攻击力增加15%
        """;
        private String subMark = "";

        public 牵制盗垒() {
            addEffects((new Effect(this,this, EffectTiming.WhenBreakBlock, obj->
                getBaseFollow().addAtk((int)(getBaseFollow().getAtk() * 0.15))
            )));
        }
    }

    @Getter
    @Setter
    public static class 再见安打 extends Skill {

        private String name = "再见安打";
        private String color = "#b6aeae";
        private String job = "崩铁";
        private List<String> race = Lists.ofStr("物理");
        private String mark = """
        攻击目标, 有10%的几率使目标护甲-10。
        """;
        private String subMark = "";

        public 再见安打() {
            setPlay(new Play(
                () -> enemyPlayer().getArea(), true,
                obj->{
                    final FollowCard enemyFollow = (FollowCard) obj;
                    getBaseFollow().attack(enemyFollow);
                    if(Math.random()<0.1){
                        enemyFollow.addArmor(-10);
                    }
                }));
        }
    }

    @Getter
    @Setter
    public static class 安息全垒打 extends Skill {

        private String name = "安息全垒打";
        private String color = "#b6aeae";
        private String job = "崩铁";
        private List<String> race = Lists.ofStr("物理");
        private String mark = """
        攻击目标, 对相邻目标造成70%伤害
        """;
        private String subMark = "";

        public 安息全垒打() {
            setChargeSpeed(35);
            setPlay(new Play(
                () -> enemyPlayer().getArea(), true,
                enemyFollow->{
                    final List<FollowCard> enemyArea = enemyPlayer().getArea();
                    final int index = enemyArea.indexOf(enemyFollow);
                    FollowCard targetL = index==0 ? null:enemyArea.get(index-1);
                    FollowCard targetR = index==enemyArea.size()-1 ? null:enemyArea.get(index+1);

                    getBaseFollow().attack(enemyFollow);

                    final int dmg = (int) (getBaseFollow().getAtk() * 0.7);

                    info.damageMulti(getBaseFollow(), Lists.of(targetL,targetR),dmg);
                }));
        }
    }

}
