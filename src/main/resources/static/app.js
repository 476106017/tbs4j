var distinctArr = function(arr){
    return Object.entries(
        arr.reduce((count, el) => ((count[el] = ++count[el] || 1), count), {})
      ).map(([el, count]) => `${el}${count > 1 ? count.toString() : ""}`);
}
var turnObjHtml = function(turnObj,first){
    return `
        <div class="${first?'inTurn':''}"><span class="${turnObj.owned?'my':'enemy'}"></span>
        <p>${turnObj.name}<span class="badge text-bg-secondary" ${first?"hidden":""}>${turnObj.waitTime}</span></p></div>
    `
}
var cardHtml = function(card){
    return `
        <div class="card col-sm-6 col-md-4 col-lg-2 id-${card.id} ${card.TYPE} ${card.canAttack?'canAttack':''} ${card.canDash?'canDash':''}"
            style="background-color: ${card.color}">
            <img src="https://c-1316363893.cos.ap-nanjing.myqcloud.com/${encodeURIComponent(card.name)}.png" alt="" class="image" onerror="this.hidden='hidden'">
            <div class="name">${card.name}</div>
            ${card.race.length>0?'<p class="race">'+card.race.join(' ')+'</p>':""}
            <div class="description" title="${dictShow(card.counter)}">
                
                <p ${card.TYPE=="FOLLOW" || card.charge==100 ?"hidden":""}>
                    （充能中：${card.charge}/100）
                </p>
                <p>${card.keywords?'<b class="keyword">'+distinctArr(card.keywords).join(' ')+'</b>\n':""}
                    ${card.TYPE=="FOLLOW"?'速度：'+card.speed+'&nbsp;&nbsp;&nbsp;&nbsp; 攻击：'+card.atk + '\n护甲：'+ card.armor + '&nbsp;&nbsp;&nbsp;&nbsp; 魔抗：'+card.magicResist:card.mark}
                </p>
                <div class="job">${card.job}</div>
            </div>
            <div ${card.TYPE!="FOLLOW"?"hidden":""}>
                <div class="health-bar">
                    <div class="health-bar-block" style="width: ${card.block/card.maxHp*100}%;"></div>
                    <div class="health-bar-inner" style="width: ${card.hp/card.maxHp*100}%;"></div>
                    <div class="health-bar-text">${card.hp}/${card.maxHp}</div>
                </div>
            </div>
        </div>
    `
}
var dictShow = function(obj){
    let show = "";
    for (let key in obj) {
        if(key.indexOf("_")<0 && obj[key]>0){
            show = show+key+":"+obj[key]+"\n";
        }
    }
    return show;
}
// 进入某个模式（选择/攻击）后用这个
var initBoard = function(){
    $('#enemy-info').removeClass("selected");
    $('#enemy-info').unbind();
    $('#my-info').removeClass("selected");
    $('#my-info').unbind();
    $(".end-button").html("结束<br/>回合");
    $(".end-button").css("background","radial-gradient(blue, #2f4f4f9f)");
    drawBoard();
}
var clearBoard = function(){
    $('#enemy-battlefield').empty();
    $('#my-battlefield').empty();
    $('#my-hand').empty();
    $('#my-info').empty();
    $('.board').unbind();
}
var drawBoard = function(){
    clearBoard();

    boardInfo.enemy.area.forEach(card => {
        $('#enemy-battlefield').append(cardHtml(card));
    });
    boardInfo.me.area.forEach(card => {
        $('#my-battlefield').append(cardHtml(card));
    });
    $('#my-battlefield .card').unbind()

    let turnFollow = boardInfo.turn[0]
    boardInfo.me.area.forEach(card => {
        if(card.id==turnFollow.id){
            $(".id-"+card.id).addClass('canAttack');
            card.skills.forEach(skill => {
                $('#my-hand').append(cardHtml(skill));
            });
        }
    });
    boardInfo.enemy.area.forEach(card => {
        if(card.id==turnFollow.id){
            $(".id-"+card.id).addClass('canAttack');
        }
    });

    let nextTurnFollow = boardInfo.turn[1]
    boardInfo.me.area.forEach(card => {
        if(card.id==nextTurnFollow.id){
            $(".id-"+card.id).addClass('canDash');
        }
    });
    boardInfo.enemy.area.forEach(card => {
        if(card.id==nextTurnFollow.id){
            $(".id-"+card.id).addClass('canDash');
        }
    });

    $('#my-hand .card').unbind().click(function(){
        let select = $(this).index()+1;

        drawBoard();// 先还原棋盘
        setTimeout("websocket.send('play::"+select+"')",500);
    })

    boardInfo.turn.forEach((obj,index) => {
        $('#my-info').append(turnObjHtml(obj,index==0));
    })

}



var interval;		//定时器变量

function  mnyAlert(type,msg,time=2000){
    //判断页面中是否有#mny-width的dom元素，有的话将其去除
    if($('#mny-width').length > 0){
        $('#mny-width').remove();
    }
    // 先将其插入到body下
    if(type == '1'){
        $('header').append(`
        <div id="mny-width" class="alert alert-success mny-alert-position" role="alert">
            `+msg+`
        </div>
        `);
    }else if(type == '2'){
        $('header').append(`
        <div id="mny-width" class="alert alert-danger mny-alert-position" role="alert">
            `+msg+`
        </div>
        `);
    }

    //计算长度
    const mny_width = $('#mny-width').innerWidth() + 2;
    //向元素中添加内嵌样式
    $('#mny-width').css('marginLeft','-'+mny_width/2+'px');
    // console.log(time);
    //清除已存在的定时器
    clearInterval(interval)
    //将元素定时去除
    interval = window.setInterval(function () {
        $('#mny-width').remove();
    }, time);
}

function endTurn(){
    $(".end-button").html("对方<br/>回合");
    $(".end-button").css("background","radial-gradient(red, #2f4f4f9f)");

    setTimeout("websocket.send('end')",500);
}

function showMsg(){
    $('#msg-log-div').toggle();
}
function showKeywords(){
    $('#keywords').html("");
    keywords.forEach(keyword => {
        $('#keywords').append('<button type="button" class="btn btn-outline-dark" title='+keyword.desc+' data-dismiss="modal">'+keyword.key+'</button>');
    });
    $('#keywords-modal').modal('show');
}

var myDeck;
function editDeck(){
    let newDeck = prompt("输入牌组构成（推荐编辑好后粘贴过来）：",myDeck);
    setTimeout("websocket.send('setdeck::"+newDeck+"')",500);
}
// var userName = prompt("请问牌友如何称呼？");
userName = "Player"+Math.floor(Math.random()*1000000);

var boardInfo;// 用于重绘棋盘
var targetMsg;// 需要指定时，把指令存起来
var targets;// 可指定的卡牌

if ($.trim(userName)) {
    if(window.location.host.indexOf("card4j") <= 0)
        // 本地运行
        websocket = new WebSocket("ws://localhost:18082/api/"+userName);
    else
        websocket = new WebSocket("ws://www.card4j.top:18082/api/"+userName);

    $("username").html(userName);
    console.log("征集有趣的自定义卡牌、主战者、玩法、卡面。联系方式：（Bilibili）漆黑Ganker");
    console.log("如果你是软件开发人员，欢迎你贡献代码！项目地址：https://github.com/476106017/ccg4j");

    websocket.onerror = function () {
        console.log("连接错误");
    }
    websocket.onopen = function () {
        // alert("连接成功！");
        websocket.send("deck");
    };
    //      收到消息的回调方法
    websocket.onmessage = function (msg) {
        let data = JSON.parse(msg.data);
        let obj = data.data;
        console.log(data);

        switch(data.channel){
            case "msg":
                mnyAlert(1,obj);
                $('#msg-log-div').prepend(obj+'<br/>');
                break;
            case "warn":
                mnyAlert(2,obj);
                $('#msg-log-div').prepend(obj+'<br/>');
                break;
            case "alert":
                alert(obj);
                break;
            case "myDeck":
                $('#card-gridview').html("");
                myDeck = "";
                obj.deck.forEach(card => {
                    $('#card-gridview').append(cardHtml(card));
                    myDeck += card.name;
                    myDeck += "#";
                });
                // websocket.send('joinRoom');// test
                break;
            case "presetDeck":
                $('#deck-preset').html("");
                obj.forEach(deck => {
                    $('#deck-preset').append('<button type="button" class="btn btn-outline-dark" data-dismiss="modal" onclick="websocket.send(\'usedeck::'+deck.name+'\');">'+deck.name+'</button>');
                });
                $('#deck-preset-modal').modal('show');
                break;
            case "waitRoom":
                $('#roomCode').html(obj);
                $('#wait-room-modal').modal('show');
                break;
            case "startGame":
                $('#senjou-modal').modal('show');
                break;
            case "enemyTurn":
                $(".end-button").html("对方<br/>回合");
                $(".end-button").css("background","radial-gradient(red, #2f4f4f9f)");
                break;
            case "yourTurn":
                $(".end-button").html("结束<br/>回合");
                $(".end-button").css("background","radial-gradient(blue, #2f4f4f9f)");
                break;
            case "battleInfo":
                boardInfo = obj;
                drawBoard();
                break;
            case "clearBoard":
                clearBoard();
                break;
            case "skill":
                $(".end-button").html("技能<br/>目标");
                $(".end-button").css("background","radial-gradient(grey, #2f4f4f9f)");
                $('#my-hand .card').unbind();
                targets = obj;// 加载待选择项
                $('#my-battlefield .card').unbind();// 禁止攻击事件

                $('#my-info-detail .skill').addClass("selected");
                $('#my-info-detail .skill').unbind().click(()=>{
                    initBoard();// 还原棋盘
                });

                targets.forEach(obj=>{
                    $(".id-"+obj.id).addClass("selected");
                    $(".id-"+obj.id).unbind().click(()=>{
                        // 选择结束
                        initBoard();// 先还原棋盘
                        setTimeout("websocket.send('skill::"+obj.id+"')",500);

                    });
                })
                break;
            case "target":
                $(".end-button").html("效果<br/>目标");
                $(".end-button").css("background","radial-gradient(grey, #2f4f4f9f)");
                $('#my-hand .card').unbind();
                targetMsg = obj.pref+' ';
                targets = obj.targets;// 加载待选择项
                $('#my-battlefield .card').unbind();// 禁止攻击事件

                targets.forEach(obj=>{
                    $(".id-"+obj.id).addClass("selected");
                    $(".id-"+obj.id).unbind().click(()=>{
                        targetMsg+=obj.id;
                        // 选择结束
                        initBoard();// 先还原棋盘
                        setTimeout("websocket.send('play::"+targetMsg+"')",500);
                    });
                    $('.board').unbind().click(()=>{
                        initBoard();// 还原棋盘
                    });
                })
                break;
        }
    };
    //      连接关闭的回调方法
    websocket.onclose = function () {
        // alert("已断开和服务器的连接，请刷新页面！");
    };


}