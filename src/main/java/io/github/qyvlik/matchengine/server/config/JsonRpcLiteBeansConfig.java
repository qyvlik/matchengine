package io.github.qyvlik.matchengine.server.config;

import io.github.qyvlik.jsonrpclite.core.handle.WebSocketDispatch;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketFilter;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.concurrent.RpcExecutor;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.rpcinvoker.RpcDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class JsonRpcLiteBeansConfig {


    @Bean("readerExecutor")
    public Executor readerExecutor() {
        return Executors.newCachedThreadPool();
    }

    @Bean("webSocketSessionContainer")
    public WebSocketSessionContainer webSocketSessionContainer() {
        return new WebSocketSessionContainer(2000, 20000);
    }

    @Bean(value = "rpcDispatcher", initMethod = "initInvoker")
    public RpcDispatcher rpcDispatcher(
            @Autowired ApplicationContext applicationContext) {
        return new RpcDispatcher(applicationContext);
    }

    @Bean("rpcExecutor")
    public RpcExecutor rpcExecutor(
            @Qualifier("readerExecutor") Executor readerExecutor) {
        return new RpcExecutor() {
            @Override
            public Executor defaultExecutor() {
                return readerExecutor;
            }

            @Override
            public Executor getByRequest(WebSocketSession session, RequestObject requestObject) {
                return null;
            }
        };
    }

    @Bean("readerDispatch")
    public WebSocketDispatch readerDispatch(@Qualifier("webSocketSessionContainer") WebSocketSessionContainer webSocketSessionContainer,
                                          @Qualifier("rpcDispatcher") RpcDispatcher rpcDispatcher,
                                          @Qualifier("rpcExecutor") RpcExecutor rpcExecutor) {
        WebSocketDispatch webSocketDispatch = new WebSocketDispatch();

        webSocketDispatch.setGroup("matchengine");
        webSocketDispatch.setRpcExecutor(rpcExecutor);
        webSocketDispatch.setRpcDispatcher(rpcDispatcher);
        webSocketDispatch.setWebSocketSessionContainer(webSocketSessionContainer);
        webSocketDispatch.addFilterList(null);

        return webSocketDispatch;
    }
}
