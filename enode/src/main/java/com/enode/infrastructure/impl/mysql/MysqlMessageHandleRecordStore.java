package com.enode.infrastructure.impl.mysql;

import com.enode.common.io.AsyncTaskResult;
import com.enode.common.io.AsyncTaskStatus;
import com.enode.common.logging.ENodeLogger;
import com.enode.common.utilities.Ensure;
import com.enode.configurations.OptionSetting;
import com.enode.infrastructure.IMessageHandleRecordStore;
import com.enode.infrastructure.MessageHandleRecord;
import com.enode.infrastructure.ThreeMessageHandleRecord;
import com.enode.infrastructure.TwoMessageHandleRecord;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class MysqlMessageHandleRecordStore implements IMessageHandleRecordStore {

    private static final Logger logger = ENodeLogger.getLog();

    private final QueryRunner queryRunner;
    private final String oneMessageTableName;
    private final String oneMessageTableUniqueIndexName;
    private final String twoMessageTableName;
    private final String twoMessageTableUniqueIndexName;
    private final String threeMessageTableName;
    private final String threeMessageTableUniqueIndexName;

    public MysqlMessageHandleRecordStore(DataSource ds, OptionSetting optionSetting) {
        Ensure.notNull(ds, "ds");
        Ensure.notNull(optionSetting, "optionSetting");

        queryRunner = new QueryRunner(ds);
        oneMessageTableName = optionSetting.getOptionValue("OneMessageTableName");
        oneMessageTableUniqueIndexName = optionSetting.getOptionValue("OneMessageTableUniqueIndexName");
        twoMessageTableName = optionSetting.getOptionValue("TwoMessageTableName");
        twoMessageTableUniqueIndexName = optionSetting.getOptionValue("TwoMessageTableUniqueIndexName");
        threeMessageTableName = optionSetting.getOptionValue("ThreeMessageTableName");
        threeMessageTableUniqueIndexName = optionSetting.getOptionValue("ThreeMessageTableUniqueIndexName");

        Ensure.notNull(oneMessageTableName, "oneMessageTableName");
        Ensure.notNull(oneMessageTableUniqueIndexName, "oneMessageTableUniqueIndexName");
        Ensure.notNull(twoMessageTableName, "twoMessageTableName");
        Ensure.notNull(twoMessageTableUniqueIndexName, "twoMessageTableUniqueIndexName");
        Ensure.notNull(threeMessageTableName, "threeMessageTableName");
        Ensure.notNull(threeMessageTableUniqueIndexName, "threeMessageTableUniqueIndexName");
    }

    @Override
    public CompletableFuture<AsyncTaskResult> addRecordAsync(MessageHandleRecord record) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                queryRunner.update(String.format("INSERT INTO %s(MessageId,HandlerTypeName,MessageTypeName,AggregateRootTypeName,AggregateRootId,Version,CreatedOn) VALUES(?,?,?,?,?,?,?)", oneMessageTableName),
                        record.getMessageId(),
                        record.getHandlerTypeName(),
                        record.getMessageTypeName(),
                        record.getAggregateRootTypeName(),
                        record.getAggregateRootId(),
                        record.getVersion(),
                        record.getCreatedOn());

                return AsyncTaskResult.Success;
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1062 && ex.getMessage().contains(oneMessageTableUniqueIndexName)) {
                    return AsyncTaskResult.Success;
                }
                logger.error("Insert message handle record has sql exception.", ex);
                return new AsyncTaskResult(AsyncTaskStatus.IOException, ex.getMessage());
            } catch (Exception ex) {
                logger.error("Insert message handle record has unknown exception.", ex);
                return new AsyncTaskResult(AsyncTaskStatus.Failed, ex.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<AsyncTaskResult> addRecordAsync(TwoMessageHandleRecord record) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                queryRunner.update(String.format("INSERT INTO %s(MessageId1,MessageId2,HandlerTypeName,Message1TypeName,Message2TypeName,AggregateRootTypeName,AggregateRootId,Version,CreatedOn) VALUES(?,?,?,?,?,?,?,?,?)", twoMessageTableName),
                        record.getMessageId1(),
                        record.getMessageId2(),
                        record.getHandlerTypeName(),
                        record.getMessage1TypeName(),
                        record.getMessage2TypeName(),
                        record.getAggregateRootTypeName(),
                        record.getAggregateRootId(),
                        record.getVersion(),
                        record.getCreatedOn());

                return AsyncTaskResult.Success;
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1062 && ex.getMessage().contains(twoMessageTableUniqueIndexName)) {
                    return AsyncTaskResult.Success;
                }
                logger.error("Insert two-message handle record has sql exception.", ex);
                return new AsyncTaskResult(AsyncTaskStatus.IOException, ex.getMessage());
            } catch (Exception ex) {
                logger.error("Insert two-message handle record has unknown exception.", ex);
                return new AsyncTaskResult(AsyncTaskStatus.Failed, ex.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<AsyncTaskResult> addRecordAsync(ThreeMessageHandleRecord record) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                queryRunner.update(String.format("INSERT INTO %s(MessageId1,MessageId2,MessageId3,HandlerTypeName,Message1TypeName,Message2TypeName,Message3TypeName,AggregateRootTypeName,AggregateRootId,Version,CreatedOn) VALUES(?,?,?,?,?,?,?,?,?,?,?)", threeMessageTableName),
                        record.getMessageId1(),
                        record.getMessageId2(),
                        record.getMessageId3(),
                        record.getHandlerTypeName(),
                        record.getMessage1TypeName(),
                        record.getMessage2TypeName(),
                        record.getMessage3TypeName(),
                        record.getAggregateRootTypeName(),
                        record.getAggregateRootId(),
                        record.getVersion(),
                        record.getCreatedOn());

                return AsyncTaskResult.Success;
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1062 && ex.getMessage().contains(threeMessageTableUniqueIndexName)) {
                    return AsyncTaskResult.Success;
                }
                logger.error("Insert three-message handle record has sql exception.", ex);
                return new AsyncTaskResult(AsyncTaskStatus.IOException, ex.getMessage());
            } catch (Exception ex) {
                logger.error("Insert three-message handle record has unknown exception.", ex);
                return new AsyncTaskResult(AsyncTaskStatus.Failed, ex.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<AsyncTaskResult<Boolean>> isRecordExistAsync(String messageId, String handlerTypeName, String aggregateRootTypeName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Object countObj = (int) (long) queryRunner.query(String.format("SELECT COUNT(*) FROM %s WHERE MessageId=? AND HandlerTypeName=?", oneMessageTableName),
                        new ScalarHandler<>(),
                        messageId, handlerTypeName);

                int count = (countObj == null ? 0 : ((Number) countObj).intValue());

                return new AsyncTaskResult<>(AsyncTaskStatus.Success, count > 0);
            } catch (SQLException ex) {
                logger.error("Get message handle record has sql exception.", ex);
                return new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage());
            } catch (Exception ex) {
                logger.error("Get message handle record has unknown exception.", ex);
                return new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<AsyncTaskResult<Boolean>> isRecordExistAsync(String messageId1, String messageId2, String handlerTypeName, String aggregateRootTypeName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Object countObj = queryRunner.query(String.format("SELECT COUNT(*) FROM %s WHERE MessageId1=? AND MessageId2=? and HandlerTypeName=?", twoMessageTableName),
                        new ScalarHandler<>(),
                        messageId1,
                        messageId2,
                        handlerTypeName);

                int count = (countObj == null ? 0 : ((Number) countObj).intValue());

                return new AsyncTaskResult<>(AsyncTaskStatus.Success, count > 0);
            } catch (SQLException ex) {
                logger.error("Get two-message handle record has sql exception.", ex);
                return new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage());
            } catch (Exception ex) {
                logger.error("Get two-message handle record has unknown exception.", ex);
                return new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<AsyncTaskResult<Boolean>> isRecordExistAsync(String messageId1, String messageId2, String messageId3, String handlerTypeName, String aggregateRootTypeName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Object countObj = (int) (long) queryRunner.query(String.format("SELECT COUNT(*) FROM %s WHERE MessageId1=? AND MessageId2=? AND MessageId3=? AND HandlerTypeName=?", threeMessageTableName),
                        new ScalarHandler<>(),
                        messageId1,
                        messageId2,
                        messageId3,
                        handlerTypeName);

                int count = (countObj == null ? 0 : ((Number) countObj).intValue());

                return new AsyncTaskResult<>(AsyncTaskStatus.Success, count > 0);
            } catch (SQLException ex) {
                logger.error("Get three-message handle record has sql exception.", ex);
                return new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage());
            } catch (Exception ex) {
                logger.error("Get three-message handle record has unknown exception.", ex);
                return new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage());
            }
        });
    }
}
