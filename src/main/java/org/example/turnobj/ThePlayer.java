package org.example.turnobj;

import lombok.Getter;
import lombok.Setter;
import org.example.system.game.Leader;

import java.util.ArrayList;
import java.util.List;

import static org.example.constant.CounterKey.EP_NUM;


@Getter
@Setter
public class ThePlayer extends Leader {

    private String name = "玩家";
    private String job = "中立";

    private String skillName = "进化";
    private String skillMark =  """
        使一个我方随从获得+2/+2
        """;
    private int skillCost = 0;

    private String mark = "最基础的玩家";

    @Override
    public void init() {
        if (ownerPlayer().isInitative()) {
            ownerPlayer().count(EP_NUM,2);
        }else {
            ownerPlayer().count(EP_NUM,3);
        }
    }

    @Override
    public List<GameObj> targetable() {
        if(ownerPlayer().getCount(EP_NUM) == 0){
            return new ArrayList<>();
        }

        return ownerPlayer().getAreaGameObj();
    }

    @Override
    public void skill(GameObj target) {
        super.skill(target);

        FollowCard follow = (FollowCard) target;
        follow.addStatus(2,2);

        ownerPlayer().count(EP_NUM,-1);
        getInfo().msgToThisPlayer("你还剩下"+ownerPlayer().getCount(EP_NUM)+"个进化点");

    }

}
