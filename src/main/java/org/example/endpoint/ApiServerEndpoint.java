package org.example.endpoint;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.logging.log4j.util.Strings;
import org.example.endpoint.handler.ChatHandler;
import org.example.endpoint.handler.GameHandler;
import org.example.endpoint.handler.MatchHandler;
import org.example.system.GsonConfig;
import org.example.system.WebSocketConfig;
import org.example.system.WebSocketConfigurator;
import org.example.system.game.GameInfo;
import org.example.system.game.PlayerDeck;
import org.example.system.game.PlayerInfo;
import org.example.system.turnobj.ThePlayer;
import org.example.system.turnobj.pokemon.Hitokage;
import org.example.system.turnobj.pokemon.Pikachu;
import org.example.system.util.Msg;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.example.system.Database.*;

@ServerEndpoint(value = "/api/{name}",
    encoders = {GsonConfig.MyEncoder.class},
    configurator = WebSocketConfigurator.CustomSpringConfigurator.class)
@Service
@DependsOn({"chatHandler","gameHandler","matchHandler"})
public class ApiServerEndpoint {
    @Autowired ChatHandler chatHandler;
    @Autowired GameHandler gameHandler;
    @Autowired MatchHandler matchHandler;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        // handle open event
        final String name = session.getPathParameters().get("name");
        if(Strings.isBlank(name) || userNames.containsValue(name)){
            Msg.send(session,"用户名无法使用！");
            session.close();
            return;
        }
        session.getUserProperties().put("name",name);
        userNames.put(session,name);

        // region
        PlayerDeck playerDeck = new PlayerDeck();

        playerDeck.getActiveDeck().add(Pikachu.class);
        playerDeck.getActiveDeck().add(Hitokage.class);
        playerDeck.setLeaderClass(ThePlayer.class);
        userDecks.put(session, playerDeck);
        // endregion
        Msg.send(session,name + "登录成功！");
        final int size = userNames.size();
        WebSocketConfig.broadcast("【全体】有玩家登陆了游戏！当前在线："+ size +"人");
    }

    @OnClose
    public void onClose(Session session) {
        // handle close event
        String name = userNames.get(session);
        userNames.remove(session);
        final int size = userNames.size();
        WebSocketConfig.broadcast("【全体】有玩家退出了游戏！当前在线："+ size +"人");
        String room = userRoom.get(session);
        if(room==null)return;

        GameInfo info = roomGame.get(room);
        if(info!=null){
            PlayerInfo player = info.playerBySession(session);
            PlayerInfo enemy = info.anotherPlayerBySession(session);
            info.msg(player.getName() + "已断开连接！");
            info.gameset(enemy);
            return;
        }
        // 释放资源
        roomGame.remove(room);
        userRoom.remove(session);
        if(session==waitUser || room.equals(waitRoom) ){
            waitRoom = "";
            waitUser = null;
            WebSocketConfig.broadcast("【全体】匹配中的玩家已经退出了！");
        }
    }

    @OnMessage
    public void onMessage(String msg, Session session) {
        // handle message event
        final String[] split = msg.trim().split("::");
        try {

            String param;
            if(split.length<2 || Strings.isBlank(split[1]))
                param = "";
            else param = split[1];

            switch (split[0]){
                case "joinRoom" -> matchHandler.joinRoom(session);
                case "leave" -> matchHandler.leave(session);

                case "chat" -> chatHandler.chat(session, param);
                case "play" -> gameHandler.play(session, param);

                default -> Msg.send(session,"不存在的指令！");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @OnError
    public void onError(Throwable t) {
        // handle error event
    }


}