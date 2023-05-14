package org.example.turnobj.pokemon;

import lombok.Getter;
import lombok.Setter;
import org.example.system.game.Damage;
import org.example.system.game.DamageMulti;
import org.example.system.game.Play;
import org.example.turnobj.FollowCard;
import org.example.turnobj.Skill;
import org.example.system.util.Lists;

import java.util.List;

@Setter
@Getter
public class Zenigame extends FollowCard {
    private String name = "杰尼龟";
    private String color = "#1679ff";
    private int speed = 43;
    private int atk = 48;
    private int hp = 440;
    private int armor = 60;
    private int magicResist = 40;
    private int block = 120;
    private String job = "宝可梦";
    private List<String> race = Lists.ofStr("水");
    private String mark = """
    """;
    private String subMark = "";

    @Override
    public void init() {
        super.init();
        addSkill(Mizudeppou.class);
        addSkill(Mizunohodou.class);
    }

    @Getter
    @Setter
    public static class Mizudeppou extends Skill {

        private String name = "水枪";
        private String color = "#1679ff";
        private String job = "宝可梦";
        private List<String> race = Lists.ofStr("水");
        private String mark = """
        攻击目标造成伤害，10%概率暴击。
        """;
        private String subMark = "";

        public Mizudeppou() {
            setPlay(new Play(
                () -> enemyPlayer().getAreaGameObj(), true,
                obj->{
                    final FollowCard enemyFollow = (FollowCard) obj;
                    if(Math.random()<0.1){
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
    public static class Mizunohodou extends Skill {

        private String name = "水之波动";
        private String color = "#1679ff";
        private String job = "宝可梦";
        private List<String> race = Lists.ofStr("水");
        private String mark = """
        攻击目标, 有20%的几率使目标陷入1回合【混乱】状态。
        """;
        private String subMark = "";

        public Mizunohodou() {
            setPlay(new Play(
                () -> enemyPlayer().getAreaGameObj(), true,
                obj->{
                    final FollowCard enemyFollow = (FollowCard) obj;
                    getBaseFollow().attack(enemyFollow);
                    if(Math.random()<0.2){
                        enemyFollow.addKeywordN("混乱",1);
                    }
                }));
        }
    }

    @Getter
    @Setter
    public static class Karanikomoru extends Skill {

        private String name = "缩入壳中";
        private String color = "#1679ff";
        private String job = "宝可梦";
        private List<String> race = Lists.ofStr("水");
        private String mark = """
        给予友方随从120点格挡，将其10点速度转换为护甲。
        10%的概率使自己获得10点护甲
        """;
        private String subMark = "";

        public Karanikomoru() {
            setPlay(new Play(
                () -> ownerPlayer().getAreaGameObj(), true,
                obj->{
                    final FollowCard followCard = (FollowCard) obj;
                    if(Math.random()<0.1){
                        getBaseFollow().addArmor(10);
                    }else {
                        followCard.addBlock(120);
                        followCard.addSpeed(-10);
                        followCard.addArmor(10);
                    }
                }));
        }
    }
}
