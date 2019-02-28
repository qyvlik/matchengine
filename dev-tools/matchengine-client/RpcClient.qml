import QtWebSockets 1.0

WebSocket {
    id: socket

    readonly property var rpcCallback:({});
    readonly property var channelCallback:({});

    function callRpcMethod(method, params, callback) {
        var id = (new Date()).getTime();

        var req = {
            id: id,
            method: method,
            params: params
        }

//        callback = callback || function(res) {
//            console.log(JSON.stringify(res));
//        };

        rpcCallback[id] = callback;

        socket.sendTextMessage(JSON.stringify(req));
    }

    function subChannel(channel, params, subscribe, callback) {
        callback = callback || function(res) {
            console.log(JSON.stringify(res));
        };

        var req = {
            channel: channel,
            params: params,
            subscribe: subscribe
        }

        channelCallback[channel] = callback;

        socket.sendTextMessage(JSON.stringify(req));
    }

    onTextMessageReceived: {

        var obj = JSON.parse(message);

        if (typeof obj.channel !== 'undefined') {

            var channelCB = channelCallback[obj.channel];
            if (typeof channelCB !== 'undefined') {
                channelCB(obj);
            } else {
                // console.error("have not channel:" + obj.channel + " callback")
            }

        }

        if (typeof obj.id !== 'undefined') {
            var rpcCB = rpcCallback[obj.id];
            if (typeof rpcCB !== 'undefined') {
                rpcCB(obj);

//                rpcCallback[obj.id] = undefined;

            } else {
                // console.error("have not id:" + obj.id + " callback, method: "  + obj.method)
            }
        }
    }
}
