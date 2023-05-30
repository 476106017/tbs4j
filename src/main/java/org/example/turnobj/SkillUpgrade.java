package org.example.turnobj;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.game.Play;
import org.example.system.util.FunctionN;

import java.util.List;
import java.util.function.Consumer;

@Getter
@Setter
public class SkillUpgrade extends GameObj {
    transient Skill baseSkill;
    public int speed;
    public String name;
    public String mark;
    public String subMark;

    private transient FunctionN func = null;
    public void play(){
        info.msg(info.getTurnObject().getName() + "选择了升级：" + getName());

        func.apply();
    }
}
