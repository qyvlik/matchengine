package io.github.qyvlik.matchengine.server.listener;

public interface OrderCommandExecutor {
    void exec(OrderCommand command);
}
