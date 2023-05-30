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

@Setter
@Getter
public class 斧王 extends FollowCard {
    private String name = "斧王";
    private String color = "#d20000";
    private int speed = 90;
    private int atk = 50;
    private int hp = 625;
    private int armor = 18;
    private int magicResist = 25;
    private int block = 0;
    private String job = "DotA";
    private List<String> race = Lists.ofStr("天灾");
    @Override
    public void init() {
        super.init();
        addSkill(反击螺旋.class);
        addSkill(攻击.class);
        addSkill(狂战士的怒吼.class);
        addSkill(战斗饥渴.class);
        addSkill(淘汰之刃.class);
    }

    @Getter
    @Setter
    public static class 攻击 extends Skill {

        private String name = "攻击";
        private String color = "#d20000";
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
                    if(getCount()==3){

                        List<SkillUpgrade> upgrades = new ArrayList<>();
                        upgrades.add(createUpgrade("撒旦之邪力","获得【吸血】",()->{
                            getBaseFollow().addKeyword("吸血");
                        }));
                        upgrades.add(createUpgrade("先锋盾",
                            "60%几率格挡60点攻击伤害",()->{
                                getBaseFollow().addEffects(new Effect(this,this, EffectTiming.BeforeDamaged,
                                    dmg->{
                                        if(Math.random()<0.6){
                                            final Damage damage = (Damage) dmg;
                                            damage.addDamage(-60);
                                        }
                                    }));
                            }));
                        upgrades.add(createUpgrade("魔龙之心",
                            "回合开始时恢复70点生命值",()->{
                                getBaseFollow().addEffects(new Effect(this,this, EffectTiming.BeginTurn,
                                    dmg->{
                                        getBaseFollow().heal(70);
                                    }));
                            }));
                        ownerPlayer().discoverCard(upgrades);
                    }
                }));
        }
    }


    @Getter
    @Setter
    public static class 反击螺旋 extends Skill {

        private String name = "反击螺旋";
        private String color = "#d20000";
        private String job = "DotA";
        private List<String> race = Lists.ofStr("天赋");
        private String mark = """
        受到攻击（或反击）时，有20%几率对敌方全体造成150点伤害
        """;
        private String subMark = "";

        @Override
        public void init() {
            getBaseFollow().addEffects(new Effect(this,this,EffectTiming.AfterDamaged,obj->{
                Damage damage = (Damage) obj;
                if(damage.isFromAtk() && Math.random()<0.2){
                    getInfo().msg("反击螺旋！");
                    info.damageMulti(getBaseFollow(),enemyPlayer().getAreaCopy(),150 );
                }
            }));
        }
    }
    @Getter
    @Setter
    public static class 狂战士的怒吼 extends Skill {

        private String name = "狂战士的怒吼";
        private String color = "#d20000";
        private String job = "DotA";
        private List<String> race = Lists.ofStr();
        private String mark = """
        斧王嘲讽附近的敌方单位，强迫他们攻击自己，同时获得40点临时护甲。
        """;
        private String subMark = "";

        private boolean add = false;

        @Override
        public void init() {
            setChargeSpeed(50);
            getBaseFollow().addEffects(new Effect(this,this,EffectTiming.BeginTurn,obj->{
                if(add){
                    getBaseFollow().addArmor(-40);
                    add = false;
                }
            }));
            setPlay(new Play(
                () ->{
                    getBaseFollow().addArmor(40);
                    add = true;
                    enemyPlayer().getAreaCopy().forEach(followCard -> {
                        if(getBaseFollow().atArea() && followCard.atArea()){
                            followCard.attack(getBaseFollow());
                        }
                    });
                }));
        }
    }
    @Getter
    @Setter
    public static class 战斗饥渴 extends Skill {

        private String name = "战斗饥渴";
        private String color = "#d20000";
        private String job = "DotA";
        private List<String> race = Lists.ofStr();
        private String mark = """
        给与目标3层【灼伤】，并且偷取12点临时速度
        """;
        private String subMark = "";

        @Override
        public void init() {
            setChargeSpeed(30);
            setPlay(new Play(
                () -> enemyPlayer().getArea(), true,
                obj->{
                    obj.addKeywordN("灼伤",3);
                    obj.addTempSpeed(-12);
                    getBaseFollow().addTempSpeed(12);
                }));
        }
    }

    @Getter
    @Setter
    public static class 淘汰之刃 extends Skill {

        private String name = "淘汰之刃";
        private String color = "#d20000";
        private String job = "DotA";
        private List<String> race = Lists.ofStr();
        private String mark = """
        瞬间斩杀生命值少于250的敌人，然后刷新该技能；
        否则对目标造成150点伤害
        """;
        private String subMark = "";

        @Override
        public void init() {
            setChargeSpeed(20);
            setPlay(new Play(
                () -> enemyPlayer().getArea(), true,
                obj->{

                    if(obj.getHp()<250){
                        getBaseFollow().destroy(obj);
                        setCharge(100);
                    }else {
                        info.damageEffect(getBaseFollow(),obj,150);
                    }
                }));
        }
    }

}
