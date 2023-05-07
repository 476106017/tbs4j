package org.example.system.turnobj.pokemon;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.game.Effect;
import org.example.system.turnobj.FollowCard;
import org.example.system.turnobj.pokemon.skill.Denngekiha;
import org.example.system.turnobj.pokemon.skill.Hinoko;
import org.example.system.turnobj.pokemon.skill.Honoonokiba;
import org.example.system.turnobj.pokemon.skill.JuumannBoruto;
import org.example.system.util.Lists;

import java.util.List;

@Setter
@Getter
public class Hitokage extends FollowCard {
    private String name = "小火龙";
    private int speed = 65;
    private int atk = 52;
    private int hp = 390;
    private String job = "宝可梦";
    private List<String> race = Lists.ofStr();
    private String mark = """
    """;
    private String subMark = "";

    @Override
    public void init() {
        super.init();
        addSkill(Hinoko.class);
        addSkill(Honoonokiba.class);
    }

}
