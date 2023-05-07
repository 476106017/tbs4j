package org.example.system.turnobj.pokemon;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.game.Effect;
import org.example.system.turnobj.FollowCard;
import org.example.system.turnobj.Skill;
import org.example.system.turnobj.pokemon.skill.Denngekiha;
import org.example.system.turnobj.pokemon.skill.JuumannBoruto;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Pikachu extends FollowCard {
    private String name = "皮卡丘";
    private int speed = 90;
    private int atk = 55;
    private int hp = 350;
    private String job = "宝可梦";
    private List<String> race = Lists.ofStr();
    private String mark = """
    每回合永久增加1点速度
    """;
    private String subMark = "";

    @Override
    public void init() {
        super.init();
        addSkill(JuumannBoruto.class);
        addSkill(Denngekiha.class);

        addEffects((new Effect(this,this, EffectTiming.BeginTurn, obj->
            addSpeed(1)
        )));
    }

}
