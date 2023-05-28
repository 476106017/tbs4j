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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public class 艾丝妲 extends FollowCard {
    private String name = "艾丝妲";

    private String color = "#ff3939";
    private int speed = 106;
    private int atk = 39;
    private int hp = 139;
    private int armor = 122;
    private int magicResist = 63;
    private int block = 0;
    private String job = "崩铁";
    private List<String> race = Lists.ofStr("火","同谐");
    @Override
    public void init() {
        super.init();
        getKeywords().add("远程");
        addSkill(光谱射线.class);
        addSkill(流星群落.class);
        addSkill(星空祝言.class);
    }

    @Getter
    @Setter
    public static class 光谱射线 extends Skill {

        private String name = "光谱射线";
        private String color = "#ff3939";
        private String job = "崩铁";
        private List<String> race = Lists.ofStr("火");
        private String mark = """
        攻击目标, 有50%几率对手陷入3回合【灼伤】状态
        """;
        private String subMark = "";

        @Override
        public void init() {
            setPlay(new Play(
                () -> enemyPlayer().getArea(), true,
                obj->{
                    getBaseFollow().attack(obj);
                    if(Math.random()<0.5){
                        obj.addKeywordN("灼伤",3);
                    }
                }));
        }
    }

    @Getter
    @Setter
    public static class 流星群落 extends Skill {

        private String name = "流星群落";
        private String color = "#ff3939";
        private String job = "崩铁";
        private List<String> race = Lists.ofStr("火");
        private String mark = """
        对目标造成80点伤害，并随机弹射4次，每次弹射伤害减半
        任务：如果敌方多于3名随从遭到此技能伤害，我方全体攻击+10
        """;
        private String subMark = "";

        public 流星群落() {
            setChargeSpeed(50);
            setPlay(new Play(
                () -> enemyPlayer().getArea(), true,
                obj->{
                    info.damageEffect(getBaseFollow(), obj,80);
                    final FollowCard areaRandomFollow1 = enemyPlayer().getAreaRandomFollow();
                    info.damageEffect(getBaseFollow(), areaRandomFollow1,40);
                    final FollowCard areaRandomFollow2 = enemyPlayer().getAreaRandomFollow();
                    info.damageEffect(getBaseFollow(), areaRandomFollow2,20);
                    final FollowCard areaRandomFollow3 = enemyPlayer().getAreaRandomFollow();
                    info.damageEffect(getBaseFollow(), areaRandomFollow3,10);
                    final FollowCard areaRandomFollow4 = enemyPlayer().getAreaRandomFollow();
                    info.damageEffect(getBaseFollow(), areaRandomFollow4,5);
                    final Set<FollowCard> hashSet = new HashSet<>();
                    hashSet.add(obj);
                    hashSet.add(areaRandomFollow1);
                    hashSet.add(areaRandomFollow2);
                    hashSet.add(areaRandomFollow3);
                    hashSet.add(areaRandomFollow4);
                    if(hashSet.size()>=3){
                        ownerPlayer().getArea().forEach(followCard -> followCard.addAtk(10));
                    }
                }));
        }
    }

    @Getter
    @Setter
    public static class 星空祝言 extends Skill {

        private String name = "星空祝言";
        private String color = "#ff3939";
        private String job = "崩铁";
        private List<String> race = Lists.ofStr("火");
        private String mark = """
        使我方全体提高43临时速度
        """;
        private String subMark = "";

        public 星空祝言() {
            setChargeSpeed(35);
            setPlay(new Play(
                () ->{
                    ownerPlayer().getArea().forEach(followCard -> followCard.addTempSpeed(43));
                }));
        }
    }

}
