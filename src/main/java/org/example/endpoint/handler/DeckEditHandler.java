package org.example.endpoint.handler;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.example.constant.DeckPreset;
import org.example.system.Database;
import org.example.system.game.Leader;
import org.example.system.game.PlayerDeck;
import org.example.turnobj.FollowCard;
import org.example.turnobj.ThePlayer;
import org.example.system.util.Msg;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.example.system.Database.userDecks;

@Service
@Slf4j
public class DeckEditHandler {

    public void deck(Session client) throws IOException, EncodeException {
        Msg.send(client,"myDeck",userDecks.get(client).describe());
    }

    public void setdeck(Session client, String data) throws IOException, EncodeException {

        if(Strings.isBlank(data)){
            Msg.warn(client,"牌组无效");
            return;
        }
        final List<String> cardNames = Arrays.stream(data.split("#")).filter(s -> !s.trim().equals("")).toList();

        if(cardNames.size()>7){
            Msg.warn(client,"队伍最多有7个怪兽！");
            return;
        }
        List<Class<? extends FollowCard>> newDeck = new ArrayList<>();
        cardNames.forEach(name->{
            final Class<? extends FollowCard> aClass = Database.nameToCardClass.get(name);
            if(aClass!=null) newDeck.add(aClass);
        });
        if(newDeck.size()<1){
            Msg.warn(client,"队伍至少有1个怪兽！");
            return;
        }
        final PlayerDeck playerDeck = userDecks.get(client);
        playerDeck.setActiveDeck(newDeck);
        Msg.send(client,"myDeck", playerDeck.describe());
    }


    public void usedeck(Session client, String data) throws IOException, EncodeException {

        final Map<String, List<Class<? extends FollowCard>>> decks = DeckPreset.decks;
        if(Strings.isBlank(data)){
            // region 如果不输入选择牌组，则返回全部牌组
            Msg.send(client,"presetDeck",DeckPreset.describe());
            return;
            // endregion 如果不输入选择牌组，则返回全部牌组
        }
        List<Class<? extends FollowCard>> deck = decks.get(data);
        Class<? extends Leader> leader = DeckPreset.deckLeader.get(data);
        if(deck==null){
            Msg.send(client,"不存在的牌组名字");
            return;
        }
        if(leader==null){
            leader = ThePlayer.class;
        }

        PlayerDeck playerDeck = userDecks.get(client);
        if(playerDeck==null){
            Msg.send(client,"不存在的用户，请刷新页面重新登录");
            return;
        }

        List<Class<? extends FollowCard>> activeDeck = playerDeck.getActiveDeck();
        activeDeck.clear();
        activeDeck.addAll(deck);
        playerDeck.setLeaderClass(leader);

        Msg.send(client, "成功使用预设牌组：" + data);
        Msg.send(client,"myDeck",userDecks.get(client).describe());
    }

}
