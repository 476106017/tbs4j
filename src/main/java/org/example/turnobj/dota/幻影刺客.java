package org.example.turnobj.dota;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.game.Damage;
import org.example.system.game.Effect;
import org.example.system.game.Play;
import org.example.system.util.Lists;
import org.example.turnobj.FollowCard;
import org.example.turnobj.Skill;
import org.example.turnobj.SkillUpgrade;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Setter
@Getter
public class 幻影刺客 extends FollowCard {
    private String name = "幻影刺客";
    private String color = "#777777";
    private int speed = 103;
    private int atk = 48;
    private int hp = 473;
    private int armor = 23;
    private int magicResist = 17;
    private int block = 0;
    private String job = "DotA";
    private List<String> race = Lists.ofStr("天灾");
    @Override
    public void init() {
        super.init();
        addSkill(恩赐解脱.class);
        addSkill(攻击.class);
        addSkill(闪烁偷袭.class);
        addSkill(窒息之刃.class);
    }

    @Getter
    @Setter
    public static class 恩赐解脱 extends Skill {

        private String name = "恩赐解脱";
        private String color = "#777777";
        private String job = "DotA";
        private List<String> race = Lists.ofStr("天赋");
        private String mark = """
        攻击时有15%几率造成4.5倍暴击,
        攻击时有5%几率造成10倍暴击
        """;
        private String subMark = "";

        @Override
        public void init() {
            getBaseFollow().addEffects(new Effect(this,this,EffectTiming.WhenAttack,obj->{
                if(Math.random()<0.15){
                    getInfo().msg("恩赐解脱！");
                    Damage damage = (Damage) obj;
                    damage.multi( 4.5f);
                }
                if(Math.random()<0.05){
                    getInfo().msg("Bingo！");
                    Damage damage = (Damage) obj;
                    damage.multi( 10f);
                }
            }));
        }
    }
    @Getter
    @Setter
    public static class 攻击 extends Skill {

        private String name = "攻击";
        private String color = "#777777";
        private String job = "DotA";
        private List<String> race = Lists.ofStr();
        private String mark = """
        攻击敌方目标
        任务：攻击3次以选择一个升级
        """;
        private String subMark = "";

        @Override
        public void init() {
            setPlay(new Play(
                () -> enemyPlayer().getArea(), true,
                obj->{
                    getBaseFollow().attack(obj);

                    count();
                    if(getCount()==3) {

                        List<SkillUpgrade> upgrades = new ArrayList<>();
                        upgrades.add(createUpgrade("撒旦之邪力", "获得【吸血】", () -> {
                            getBaseFollow().addKeyword("吸血");
                        }));
                        upgrades.add(createUpgrade("圣剑",
                            "攻击力+100，但死亡时被随机敌人捡到", () -> {
                                getBaseFollow().addAtk(100);
                                getBaseFollow().addEffects(new Effect(this, this, EffectTiming.DeathRattle,
                                    dmg -> {
                                        enemyPlayer().getAreaRandomFollow().addAtk(100);
                                    }));
                            }));
                        upgrades.add(createUpgrade("蝴蝶",
                            "获得25点攻击和30点速度", () -> {
                                getBaseFollow().addAtk(25);
                                getBaseFollow().addSpeed(30);
                            }));
                        ownerPlayer().discoverCard(upgrades);
                    }
                }));
        }
    }

    @Getter
    @Setter
    public static class 闪烁偷袭 extends Skill {

        private String name = "闪烁偷袭";
        private String color = "#777777";
        private String job = "DotA";
        private List<String> race = Lists.ofStr();
        private String mark = """
        攻击目标，创造一个同名效果：再次攻击目标（速度：200）
        """;
        private String subMark = "";

        @Override
        public void init() {
            setChargeSpeed(50);
            setPlay(new Play(
                () -> enemyPlayer().getArea(), true,
                obj->{
                    getBaseFollow().attack(obj);
                    createCountCard(getClass().getSimpleName(),200,obj,
                        targetFollow-> getBaseFollow().attack(obj));
                }));
        }
    }

    @Getter
    @Setter
    public static class 窒息之刃 extends Skill {

        private String name = "窒息之刃";
        private String color = "#777777";
        private String job = "DotA";
        private List<String> race = Lists.ofStr();
        private String mark = """
        对目标造成25点伤害，并减少50%临时速度
        任务：使用此技能击杀目标，伤害翻倍
        """;
        private String subMark = """
        对目标造成{}点伤害，并减少50%临时速度
        任务：使用此技能击杀目标，伤害翻倍
        """;

        private int dmg = 25;

        public 窒息之刃() {
            setChargeSpeed(45);
            setPlay(new Play(
                () -> enemyPlayer().getArea(), true,
                obj->{
                    info.damageEffect(getBaseFollow(),obj,dmg);

                    if(!obj.atArea()){
                        dmg *= 2;
                        setMark(getSubMark().replaceAll("\\{}", String.valueOf(dmg)));
                        return;
                    }

                    final double speed = obj.getSpeed() * 0.5;
                    obj.addTempSpeed(-(int)speed);
                }));
        }
    }

}
