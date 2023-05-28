package org.example.turnobj.pokemon;

import lombok.Getter;
import lombok.Setter;
import org.example.system.game.Play;
import org.example.turnobj.FollowCard;
import org.example.turnobj.Skill;
import org.example.system.util.Lists;

import java.util.List;

@Setter
@Getter
public class Hitokage extends FollowCard {
    private String name = "小火龙";
    private String color = "#ff1616";
    private int speed = 65;
    private int atk = 52;
    private int hp = 390;
    private int armor = 20;
    private int magicResist = 10;
    private int block = 10;
    private String job = "宝可梦";
    private List<String> race = Lists.ofStr("火");

    @Override
    public void init() {
        super.init();
        addSkill(Hinoko.class);
        addSkill(Honoonokiba.class);
    }

    @Getter
    @Setter
    public static class Hinoko extends Skill {

        private String name = "火花";
        private String color = "#ff1616";
        private String job = "宝可梦";
        private List<String> race = Lists.ofStr("火");
        private String mark = """
        造成40点伤害, 有30%几率对手陷入3回合【灼伤】状态
        """;
        private String subMark = "";

        public Hinoko() {
            setPlay(new Play(
                () -> enemyPlayer().getArea(), true,
                obj->{
                    final FollowCard enemyFollow = (FollowCard) obj;
                    info.damageEffect(getBaseFollow(), enemyFollow,40);
                    if(Math.random()<0.3){
                        enemyFollow.addKeywordN("灼伤",3);
                    }
                }));
        }
    }


    @Getter
    @Setter
    public static class Honoonokiba extends Skill {

        private String name = "火焰牙";
        private String color = "#ff1616";
        private String job = "宝可梦";
        private List<String> race = Lists.ofStr("火");
        private String mark = """
        攻击目标, 有10%的几率使目标陷入3回合【灼伤】状态。
        如果目标回合倒计时多于50，则有30%的几率使目标陷入1回合【离神】状态。
        """;
        private String subMark = "";

        public Honoonokiba() {
            setPlay(new Play(
                () -> enemyPlayer().getArea(), true,
                obj->{
                    final FollowCard enemyFollow = (FollowCard) obj;
                    getBaseFollow().attack(enemyFollow);
                    if(Math.random()<0.1){
                        enemyFollow.addKeywordN("灼伤",3);
                    }
                    if(enemyFollow.getWaitTimeShow()>=50 && Math.random()<0.3){
                        enemyFollow.addKeyword("离神");
                    }
                }));
        }
    }

}
