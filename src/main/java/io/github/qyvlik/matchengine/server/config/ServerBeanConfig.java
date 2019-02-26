package io.github.qyvlik.matchengine.server.config;

import com.google.common.collect.Lists;
import io.github.qyvlik.matchengine.core.durable.service.MatchEngineDBFactory;
import io.github.qyvlik.matchengine.server.dispatch.MatchEngineRequestDispatcher;
import io.github.qyvlik.matchengine.server.dispatch.OrderCommandExecutor;
import io.github.qyvlik.matchengine.server.dispatch.WritableExecutor;
import io.github.qyvlik.matchengine.server.durable.MatchEngineStoreService;
import io.github.qyvlik.matchengine.server.engine.MatchEngineServer;
import io.github.qyvlik.matchengine.server.listener.IOrderCommandExecutor;
import io.github.qyvlik.matchengine.server.listener.OrderDBListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ServerBeanConfig {

    @Value("${matchengine.dbDisk.directory}")
    private String dbDiskDirectory;

    @Value("${matchengine.dbDisk.limit}")
    private Integer dbDiskLimit;

    @Value("${matchengine.orderdb.host}")
    private String orderDBHost;

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
            @Qualifier("matchEngineDBFactory") MatchEngineDBFactory matchEngineDBFactory,
            @Qualifier("symbolList") List<String> symbolList) {
        MatchEngineStoreService matchEngineStoreService = new MatchEngineStoreService(matchEngineDBFactory);

        for (String symbol : symbolList) {
            matchEngineStoreService.createSymbol(symbol);
        }

        return matchEngineStoreService;
    }

    @Bean("matchEngineServer")
    public MatchEngineServer matchEngineServer(
            @Qualifier("matchEngineStoreService") MatchEngineStoreService matchEngineStoreService) {
        return new MatchEngineServer(matchEngineStoreService);
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

    @Bean("orderCommandExecutor")
    public OrderCommandExecutor orderCommandExecutor(
            @Qualifier("matchEngineRequestDispatcher") MatchEngineRequestDispatcher matchEngineRequestDispatcher) {
        return new OrderCommandExecutor(matchEngineRequestDispatcher);
    }

    @Bean("orderDBListenerList")
    public List<OrderDBListener> orderDBListenerList(
            @Qualifier("orderCommandExecutor") IOrderCommandExecutor IOrderCommandExecutor,
            @Qualifier("symbolList") List<String> symbolList) {
        List<OrderDBListener> listeners = Lists.newLinkedList();
        if (symbolList == null || symbolList.size() == 0) {
            return listeners;
        }
        for (String symbol : symbolList) {
            listeners.add(new OrderDBListener(orderDBHost, symbol, IOrderCommandExecutor));
        }
        return listeners;
    }

}
