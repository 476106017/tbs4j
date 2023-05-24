package org.example.turnobj.dota;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.game.Damage;
import org.example.system.game.DamageMulti;
import org.example.system.game.Effect;
import org.example.system.game.Play;
import org.example.system.util.Lists;
import org.example.turnobj.FollowCard;
import org.example.turnobj.Skill;

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
        攻击目标, 偷取目标速度、攻击力、护甲、魔抗、生命值上限各1点，转换为5点速度。
        """;
        private String subMark = "";

        public 能量转移() {
            setPlay(new Play(
                () -> enemyPlayer().getAreaGameObj(), true,
                obj->{
                    final FollowCard enemyFollow = (FollowCard) obj;
                    getBaseFollow().attack(enemyFollow);
                    enemyFollow.addSpeed(-1);
                    enemyFollow.addAtk(-1);
                    enemyFollow.addArmor(-1);
                    enemyFollow.addMagicResist(-1);
                    enemyFollow.addMaxHp(-1);
                    getBaseFollow().addSpeed(5);
                }));
        }
    }

}
