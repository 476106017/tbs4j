package org.example.turnobj.dota;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.game.Effect;
import org.example.system.game.Play;
import org.example.system.util.Lists;
import org.example.turnobj.FollowCard;
import org.example.turnobj.Skill;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Setter
@Getter
public class 敌法师 extends FollowCard {
    private String name = "敌法师";
    private String color = "#0004ff";
    private int speed = 107;
    private int atk = 50;
    private int hp = 530;
    private int armor = 21;
    private int magicResist = 20;
    private int block = 0;
    private String job = "DotA";
    private List<String> race = Lists.ofStr("近卫");
    @Override
    public void init() {
        super.init();
        addSkill(法术护盾.class);
        addSkill(法力损毁.class);
        addSkill(法力虚空.class);
    }

    @Getter
    @Setter
    public static class 法术护盾 extends Skill {

        private String name = "法术护盾";
        private String color = "#0004ff";
        private String job = "DotA";
        private List<String> race = Lists.ofStr("天赋");
        private String mark = """
        增加100魔抗
        """;
        private String subMark = "";

        @Override
        public void init() {
            getBaseFollow().addMagicResist(100);
        }
    }
    @Getter
    @Setter
    public static class 法力损毁 extends Skill {

        private String name = "法力损毁";
        private String color = "#0004ff";
        private String job = "DotA";
        private List<String> race = Lists.ofStr();
        private String mark = """
        攻击目标, 消去目标所有可充能技能的40充能，并造成等量的法术伤害
        任务：消去100充能以改为偷取目标充能
        """;
        private String subMark = "";

        @Override
        public void init() {
            setPlay(new Play(
                () -> enemyPlayer().getArea(), true,
                obj->{
                    getBaseFollow().attack(obj);
                    AtomicInteger charge = new AtomicInteger();
                    obj.getSkills().forEach(skill -> {
                        if(skill.getChargeSpeed()<100){
                            final int chargeI = Math.min(skill.getCharge(), 40);
                            skill.setCharge(skill.getCharge() - chargeI);
                            charge.addAndGet(chargeI);
                        }
                    });
                    info.damageEffect(getBaseFollow(),obj,charge.get());
                    count(charge.get());

                    if(getCount()>=100){
                        getBaseFollow().getSkills().forEach(skill -> {
                            if(skill.getChargeSpeed()<100){
                                skill.setCharge(skill.getCharge() + charge.get());
                            }
                        });
                    }
                }));
        }
    }

    @Getter
    @Setter
    public static class 法力虚空 extends Skill {

        private String name = "法力虚空";
        private String color = "#0004ff";
        private String job = "DotA";
        private List<String> race = Lists.ofStr();
        private String mark = """
        对目标及其附近单位同时造成【目标总缺失充能数】等量的伤害
        当目标缺失充能数>=100时，赋予目标【法力流失】
        """;
        private String subMark = "";

        public 法力虚空() {
            setChargeSpeed(50);
            setPlay(new Play(
                () -> enemyPlayer().getArea(), true,
                obj->{
                    AtomicInteger charge = new AtomicInteger();
                    obj.getSkills().forEach(skill -> {
                        if(skill.getCharge()<100){
                            charge.addAndGet(100 - skill.getCharge());
                        }
                    });
                    if(charge.get()>=100){
                        obj.addKeyword("法力流失");
                    }

                    final List<FollowCard> enemyArea = enemyPlayer().getArea();
                    final int index = enemyArea.indexOf(obj);
                    FollowCard targetL = index==0 ? null:enemyArea.get(index-1);
                    FollowCard targetR = index==enemyArea.size()-1 ? null:enemyArea.get(index+1);

                    info.damageMulti(getBaseFollow(), Lists.of(targetL,obj,targetR),charge.get());

                }));
        }
    }

}
