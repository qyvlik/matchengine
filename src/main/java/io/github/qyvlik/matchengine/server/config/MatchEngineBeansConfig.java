package io.github.qyvlik.matchengine.server.config;

import com.google.common.collect.Lists;
import io.github.qyvlik.matchengine.core.durable.service.MatchEngineDBFactory;
import io.github.qyvlik.matchengine.core.matcher.request.CreateSymbolRequest;
import io.github.qyvlik.matchengine.server.dispatch.MatchEngineRequestDispatcher;
import io.github.qyvlik.matchengine.server.dispatch.SimpleOrderCommandExecutor;
import io.github.qyvlik.matchengine.server.dispatch.WritableExecutor;
import io.github.qyvlik.matchengine.server.durable.MatchEngineStoreService;
import io.github.qyvlik.matchengine.server.engine.MatchEngineServer;
import io.github.qyvlik.matchengine.server.listener.OrderCommandExecutor;
import io.github.qyvlik.matchengine.server.listener.OrderDBListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MatchEngineBeansConfig {

    @Value("${matchengine.dbDisk.directory}")
    private String dbDiskDirectory;

    @Value("${matchengine.dbDisk.limit}")
    private Integer dbDiskLimit;

    @Value("${matchengine.orderdb.host}")
    private String orderDBHost;


    @Value("${matchengine.backup.executetimes}")
    private Long backupExecuteTimes;

    @Bean("symbolList")
    @ConfigurationProperties(prefix = "matchengine.symbols")
    public List<String> symbolList() {
        return Lists.newLinkedList();
    }

    @Bean("matchEngineDBFactory")
    public MatchEngineDBFactory matchEngineDBFactory() {
        return new MatchEngineDBFactory(dbDiskDirectory, dbDiskLimit);
    }

    @Bean("matchEngineStoreService")
    public MatchEngineStoreService matchEngineStoreService(
            @Qualifier("matchEngineDBFactory") MatchEngineDBFactory matchEngineDBFactory) {
        return new MatchEngineStoreService(matchEngineDBFactory);
    }

    @Bean("matchEngineServer")
    public MatchEngineServer matchEngineServer(
            @Qualifier("matchEngineStoreService") MatchEngineStoreService matchEngineStoreService,
            @Qualifier("symbolList") List<String> symbolList) {
        MatchEngineServer matchEngineServer = new MatchEngineServer(matchEngineStoreService, backupExecuteTimes);

        for (String symbol : symbolList) {
            matchEngineServer.createSymbol(new CreateSymbolRequest(symbol));
        }

        return matchEngineServer;
    }

    @Bean("writableExecutor")
    public WritableExecutor writableExecutor() {
        return new WritableExecutor();
    }

    @Bean("matchEngineRequestDispatcher")
    public MatchEngineRequestDispatcher matchEngineRequestDispatcher(
            @Qualifier("matchEngineServer") MatchEngineServer matchEngineServer,
            @Qualifier("writableExecutor") WritableExecutor writableExecutor) {
        return new MatchEngineRequestDispatcher(matchEngineServer, writableExecutor);
    }

    @Bean("simpleOrderCommandExecutor")
    public SimpleOrderCommandExecutor simpleOrderCommandExecutor(
            @Qualifier("matchEngineRequestDispatcher") MatchEngineRequestDispatcher matchEngineRequestDispatcher) {
        return new SimpleOrderCommandExecutor(matchEngineRequestDispatcher);
    }

    @Bean("orderDBListenerList")
    public List<OrderDBListener> orderDBListenerList(
            @Qualifier("simpleOrderCommandExecutor") OrderCommandExecutor orderCommandExecutor,
            @Qualifier("symbolList") List<String> symbolList) {
        List<OrderDBListener> listeners = Lists.newLinkedList();
        if (symbolList == null || symbolList.size() == 0) {
            return listeners;
        }
        for (String symbol : symbolList) {
            listeners.add(new OrderDBListener(orderDBHost, symbol, orderCommandExecutor));
        }
        return listeners;
    }
}
