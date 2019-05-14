package com.enode.infrastructure.impl;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessageHandler;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractDenormalizer implements IMessageHandler {
    protected final QueryRunner queryRunner;

    public AbstractDenormalizer(DataSource denormalizerDatasource) {
        queryRunner = new QueryRunner(denormalizerDatasource);
    }

    protected CompletableFuture<AsyncTaskResult> executeAsync(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                queryRunner.update(sql, params);
                return AsyncTaskResult.Success;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
