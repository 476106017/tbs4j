package org.example.endpoint.handler;

import com.google.gson.Gson;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.example.system.game.GameInfo;
import org.example.system.game.Leader;
import org.example.system.game.Play;
import org.example.system.game.PlayerInfo;
import org.example.system.turnobj.FollowCard;
import org.example.system.turnobj.GameObj;
import org.example.system.turnobj.Skill;
import org.example.system.util.Lists;
import org.example.system.util.Maps;
import org.example.system.util.Msg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;

import static org.example.system.Database.*;

@Service
@Slf4j
public class GameHandler {

    @Autowired
    Gson gson;


    /* 出牌 */

    public void play(Session client, String msg) {
        // region 获取游戏对象
        String name = userNames.get(client);

        String room = userRoom.get(client);
        if (room == null) return;
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();
        final FollowCard turnObject = info.getTurnObject();
        // endregion

        boolean myTurn = client.equals(player.getSession());
        if (!myTurn) {
            player = info.oppositePlayer();
        }

        if (msg.isBlank()) {
            Msg.warn(client, "打出卡牌：play <手牌序号> <目标id> s<抉择序号>；");
            return;
        }

        String[] split = msg.split("\\s+");

        Integer indexI;
        try {
            indexI = Integer.valueOf(split[0]);
            split[0] = "";
        } catch (Exception e) {
            indexI = -1;
        }
        if (indexI <= 0 || indexI > turnObject.getSkills().size()) {
            Msg.warn(client, "输入手牌序号错误:" + split[0]);
            return;
        }

        Skill card = turnObject.getSkills().get(indexI - 1);

        if (!myTurn) {
            Msg.warn(client, "当前不是你的回合！");
            return;
        }

        // 已选好要出的card
        Play play = card.getPlay();

        // region 获取选择目标
        GameObj target = null;
        final List<GameObj> canTargets = play.canTargets().get();
        try {
            // 获取选择对象
            Optional<GameObj> targetOpt = canTargets
                .stream().filter(gameObj -> gameObj.id == Integer.parseInt(split[1])).findFirst();
            if (targetOpt.isPresent()) {
                target = targetOpt.get();
            }
        } catch (Exception e) {
        }
        // endregion 获取选择目标

        if ((target == null && canTargets.isEmpty())
            || (target != null && !canTargets.isEmpty()))
            card.play(target);
        else {
            if(!canTargets.isEmpty()){
                Msg.send(client, "请指定目标！");
                Msg.send(client,"target",
                    Maps.newMap("pref",msg,"targets", canTargets));
            }else {

                Msg.warn(client, "无法打出这张卡牌！");
            }
        }
    }


    public void skill(Session client, String msg) {
        // region 获取游戏对象
        String name = userNames.get(client);
        String room = userRoom.get(client);
        if (room == null) return;
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();
        PlayerInfo enemy = info.oppositePlayer();
        // endregion

        if (!client.equals(player.getSession())) {
            Msg.warn(client, "当前不是你的回合！");
            return;
        }

        Leader leader = player.getLeader();
        List<GameObj> targetable = leader.targetable();
        if (msg.isBlank()) {// 没有输入指定对象
            if (leader.isNeedTarget()) {
                if (targetable.isEmpty()) {
                    Msg.warn(client, "现在无法使用主战者技能！");
                } else {
                    // 指定目标
                    Msg.send(client, "skill", targetable);
                    Msg.warn(client, "请指定目标");
                }
            } else {
                leader.skill(null);
                info.pushInfo();
            }
        } else {// 输入了指定对象
            if (!leader.isNeedTarget()) {
                Msg.warn(client, "不可指定目标！");
                return;
            }
            GameObj target = null;
            try {
                int indexId = Integer.parseInt(msg);
                // 获取选择对象
                target = targetable.stream().filter(gameObj -> gameObj.id == indexId).findFirst().get();
            } catch (Exception e) {
                Msg.warn(client, "指定目标错误！");
                return;
            }
            leader.skill(target);
            info.pushInfo();
        }
    }
}

