package org.enodeframework.queue;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameHelper;
import io.vertx.ext.eventbus.bridge.tcp.impl.protocol.FrameParser;
import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.common.io.ReplySocketAddress;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.common.utilities.InetUtil;
import org.enodeframework.common.utilities.ReplyMessage;
import org.enodeframework.queue.domainevent.DomainEventHandledMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author anruence@gmail.com
 * TODO 支持其他类型的服务间调用
 */
public class DefaultSendReplyService extends AbstractVerticle implements ISendReplyService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultSendReplyService.class);

    private final ISerializeService serializeService;

    private boolean started;

    private boolean stoped;

    private NetClient netClient;

    private Cache<String, Promise<NetSocket>> netSocketCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(10)
            .build();

    public DefaultSendReplyService(ISerializeService serializeService) {
        this.serializeService = serializeService;
    }

    @Override
    public void start() {
        if (!this.started) {
            this.netClient = vertx.createNetClient();
            this.started = true;
        }
    }

    @Override
    public void stop() {
        if (!this.stoped) {
            this.netClient.close();
            this.stoped = true;
        }
    }

    @Override
    public void sendCommandReply(CommandResult commandResult, ReplySocketAddress replyAddress) {
        ReplyMessage replyMessage = new ReplyMessage();
        replyMessage.setCode(CommandReturnType.CommandExecuted.getValue());
        replyMessage.setCommandResult(commandResult);
        sendReply(replyMessage, replyAddress);
    }

    @Override
    public void sendEventReply(DomainEventHandledMessage eventHandledMessage, ReplySocketAddress replyAddress) {
        ReplyMessage replyMessage = new ReplyMessage();
        replyMessage.setCode(CommandReturnType.EventHandled.getValue());
        replyMessage.setEventHandledMessage(eventHandledMessage);
        sendReply(replyMessage, replyAddress);
    }

    public void sendReply(ReplyMessage replyMessage, ReplySocketAddress replySocketAddress) {
        SocketAddress socketAddress = SocketAddress.inetSocketAddress(replySocketAddress.getPort(), replySocketAddress.getHost());
        String message = serializeService.serialize(replyMessage);
        String address = InetUtil.toUri(replySocketAddress);
        String replyAddress = String.format("%s.%s", "client", address);
        Promise<NetSocket> promise = netSocketCache.getIfPresent(address);
        if (promise == null) {
            promise = Promise.promise();
            netSocketCache.put(address, promise);
            netClient.connect(socketAddress, promise);
        }
        promise.future().onFailure(throwable -> {
            netSocketCache.invalidate(address);
            logger.error("connect occurs unexpected error, msg: {}", message, throwable);
        }).onSuccess(socket -> {
            socket.exceptionHandler(throwable -> {
                netSocketCache.invalidate(address);
                socket.close();
                logger.error("socket occurs unexpected error, msg: {}", message, throwable);
            });
            socket.closeHandler(x -> {
                netSocketCache.invalidate(address);
                logger.error("socket closed, indicatedServerName: {},writeHandlerID: {}", socket.indicatedServerName(), socket.writeHandlerID());
            });
            socket.handler(new FrameParser(parse -> {
                if (parse.succeeded()) {
                    logger.info("receive server req: {}, res: {}", message, parse);
                }
            }));
            socket.endHandler(v -> {
                netSocketCache.invalidate(address);
            });
            FrameHelper.sendFrame("send", address, replyAddress, new JsonObject(message), socket);
        });
    }
}
