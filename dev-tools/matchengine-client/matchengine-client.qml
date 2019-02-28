import QtQuick 2.8
import QtQuick.Window 2.2

import QtWebSockets 1.0
import QtQuick.Controls 2.2
import QtQuick.Layouts 1.3

ApplicationWindow {
    width: 360
    height: 680
    visible: true
    title: qsTr("dapp-bet")

    header: ToolBar {
        RowLayout {
            anchors.fill: parent
            ToolButton {
                text: "connect"
                onClicked: {
                    writeClient.active = !writeClient.active;
                }
            }
        }
    }

    ColumnLayout {
        anchors.fill: parent
        anchors.margins: 10

        Button {
            Layout.fillWidth: true
            text: "pub.ping"
            onClicked: {
                pubPing(function(res){
                    console.log("pubPing:" + JSON.stringify(res));
                });
            }
        }

        Button {
            Layout.fillWidth: true
            text: "symbol.list"
            onClicked: {
                getSymbolList(function(res){
                    console.log("symbol.list:" + JSON.stringify(res));
                });
            }
        }

        Button {
            Layout.fillWidth: true
            text: "order.action.latest"
            onClicked: {
                getOrderActionLastSeqId('btc-usdt',function(res){
                    console.log("order.action.latest:" + JSON.stringify(res));
                });
            }
        }

        RowLayout {
            Layout.fillWidth: true

            spacing: 5

            TextField {
                id: orderIdInput
                Layout.fillWidth: true
                placeholderText: "please input orderId"
            }

            Button {
                Layout.fillWidth: true
                text: "order.by.id"
                onClicked: {
                    getOrderByOrderId('btc-usdt', orderIdInput.text, function(res){
                        console.log("order.by.id:" + JSON.stringify(res));
                    });
                }
            }

        }

        RowLayout {
            Layout.fillWidth: true

            spacing: 5

            TextField {
                id: seqIdInput
                Layout.fillWidth: true
                placeholderText: "please input seqId"
            }

            Button {
                Layout.fillWidth: true
                text: "order.by.seq"
                onClicked: {
                    getOrderBySeqId('btc-usdt', Number(seqIdInput.text), function(res){
                        console.log("order.by.seq:" + JSON.stringify(res));
                    });
                }
            }

        }

        Item {
            Layout.fillHeight: true
        }
    }

    function pubPing(callback) {
        writeClient.callRpcMethod("pub.ping", [], callback);
    }

    function getSymbolList(callback) {
        writeClient.callRpcMethod("symbol.list", [], callback);
    }

    function getOrderActionLastSeqId(symbol, callback) {
        writeClient.callRpcMethod("order.action.latest", [symbol], callback);
    }

    function getOrderByOrderId(symbol, orderId, callback) {
        writeClient.callRpcMethod("order.by.id", [symbol, orderId], callback);
    }

    function getOrderBySeqId(symbol, seqId, callback) {
        writeClient.callRpcMethod("order.by.seq", [symbol, seqId], callback);
    }

    RpcClient {
        id: writeClient
        url: "ws://localhost:11881/matchengine"

        onErrorStringChanged: {
            console.error("writeClient:", errorString)
        }
    }
}
