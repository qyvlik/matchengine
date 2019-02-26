package io.github.qyvlik.matchengine.server.dispatch;

import io.github.qyvlik.matchengine.server.engine.MatchEngineServer;
import io.github.qyvlik.matchengine.server.listener.OrderDBListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class Startup {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("symbolList")
    List<String> symbolList;

    @Autowired
    @Qualifier("orderDBListenerList")
    private List<OrderDBListener> orderDBListenerList;

    @Autowired
    @Qualifier("matchEngineServer")
    private MatchEngineServer matchEngineServer;

    @PostConstruct
    public void startup() {
        if (orderDBListenerList == null || orderDBListenerList.size() == 0) {
            return;
        }



        for (OrderDBListener listener : orderDBListenerList) {
            try {
                listener.startupAndSub(matchEngineServer.getLastSeqId(listener.getSymbol()));
            } catch (Exception e) {
                logger.error("listener:{} startup failure:{}",
                        listener.getSymbol(), e.getMessage());
            }
        }
    }
}
