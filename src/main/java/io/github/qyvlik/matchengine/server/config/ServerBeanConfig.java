package io.github.qyvlik.matchengine.server.config;

import io.github.qyvlik.matchengine.core.durable.service.MatchEngineDBFactory;
import io.github.qyvlik.matchengine.server.dispatch.MatchEngineRequestDispatcher;
import io.github.qyvlik.matchengine.server.dispatch.WritableExecutor;
import io.github.qyvlik.matchengine.server.durable.MatchEngineStoreService;
import io.github.qyvlik.matchengine.server.engine.MatchEngineServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerBeanConfig {

    @Value("${matchengine.dbDisk.directory}")
    private String dbDiskDirectory;

    @Value("${matchengine.dbDisk.limit}")
    private Integer dbDiskLimit;

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

}
