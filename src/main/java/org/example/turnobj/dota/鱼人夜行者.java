package org.example.turnobj.dota;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.game.Damage;
import org.example.system.game.Effect;
import org.example.system.game.Play;
import org.example.system.util.Lists;
import org.example.turnobj.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class 鱼人夜行者 extends FollowCard {
    private String name = "鱼人夜行者";
    private String color = "#3300ca";
    private int speed = 105;
    private int atk = 52;
    private int hp = 549;
    private int armor = 11;
    private int magicResist = 15;
    private int block = 0;
    private String job = "DotA";
    private List<String> race = Lists.ofStr("天灾");
    @Override
    public void init() {
        super.init();
        addSkill(暗影之舞.class);
        addSkill(能量转移.class);
        addSkill(黑暗契约.class);
    }

    @Getter
    @Setter
    public static class 暗影之舞 extends Skill {

        private String name = "暗影之舞";
        private String color = "#3300ca";
        private String job = "DotA";
        private List<String> race = Lists.ofStr("天赋");
        private String mark = """
        获得【远程】（攻击无法被反击）
        """;
        private String subMark = "";

        @Override
        public void init() {
            getBaseFollow().getKeywords().add("远程");
        }
    }

    @Getter
    @Setter
    public static class 能量转移 extends Skill {

        private String name = "能量转移";
        private String color = "#3300ca";
        private String job = "DotA";
        private List<String> race = Lists.ofStr();
        private String mark = """
        攻击目标, 偷取目标速度、攻击力、护甲、魔抗、生命值上限各2点，转换为10点速度。
        任务：偷取30点速度以选择一个升级
        """;
        private String subMark = "";

        public 能量转移() {
            setPlay(new Play(
                () -> enemyPlayer().getArea(), true,
                obj->{
                    final FollowCard enemyFollow = (FollowCard) obj;
                    getBaseFollow().attack(enemyFollow);
                    enemyFollow.addSpeed(-2);
                    enemyFollow.addAtk(-2);
                    enemyFollow.addArmor(-2);
                    enemyFollow.addMagicResist(-2);
                    enemyFollow.addMaxHp(-2);
                    getBaseFollow().addSpeed(10);
                    count(10);
                    if(getCount()==30){
                        List<SkillUpgrade> upgrades = new ArrayList<>();
                        upgrades.add(createUpgrade("撒旦之邪力","获得【吸血】",()->{
                            getBaseFollow().addKeyword("吸血");
                        }));
                        upgrades.add(createUpgrade("碎颅锤",
                            "获得【攻击时有25%几率暴击并使对手获得离神】",()->{
                            getBaseFollow().addEffects(new Effect(this,this, EffectTiming.WhenAttack,
                                dmg->{
                                    if(Math.random()>0.25)return;
                                    getInfo().msg("致命一击！");

                                    Damage damage = (Damage) dmg;
                                    int damageInt = damage.getDamage();
                                    damage.setDamage(damageInt * 2);

                                    damage.getTo().addKeyword("离神");
                                }));
                        }));
                        upgrades.add(createUpgrade("蝴蝶",
                            "获得25点攻击和30点速度",()->{
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
    public static class 黑暗契约 extends Skill {

        private String name = "黑暗契约";
        private String color = "#3300ca";
        private String job = "DotA";
        private List<String> race = Lists.ofStr();
        private String mark = """
        创造一个同名效果：对自己和全体敌人造成75点伤害，并净化所有负面效果（速度：160）
        任务：如果净化了多于3个负面效果，回复150生命
        """;
        private String subMark = "";

        public 黑暗契约() {
            setPlay(new Play(
                ()-> createCountCard(getClass().getSimpleName(),160,getBaseFollow(),
                    targetFollow->{
                        List<FollowCard> targetList = enemyPlayer().getAreaCopy();
                        targetList.add(targetFollow);
                        info.damageMulti(targetFollow,targetList,75);
                        if(targetFollow.purifyNegative() >= 3){
                            targetFollow.heal(150);
                        }
                    })));
        }
    }

}
