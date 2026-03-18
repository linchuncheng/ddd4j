package com.ddd4j.cloud.data.kit;

import com.ddd4j.cloud.core.context.SpringContext;

import java.util.function.Supplier;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import lombok.experimental.UtilityClass;

// 事务工具类
@UtilityClass
public class TxKit {

    /**
     * 执行事务，默认使用 PROPAGATION_REQUIRED 事务传播行为
     *
     * @param supplier
     * @return
     * @param <T>
     */
    public <T> T exec(Supplier<T> supplier) {
        return exec(TransactionDefinition.PROPAGATION_REQUIRED, supplier);
    }

    /**
     * 执行事务
     *
     * @param propagationBehavior 事务传播行为
     * @param supplier
     * @return
     * @param <T>
     */
    public <T> T exec(int propagationBehavior, Supplier<T> supplier) {
        PlatformTransactionManager transactionManager = SpringContext.getBean(PlatformTransactionManager.class);
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BaseTx");
        def.setPropagationBehavior(propagationBehavior);
        // 开始事务
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            // 在这里执行数据库操作
            T result = supplier.get();
            // 如果一切正常，提交事务
            transactionManager.commit(status);
            return result;
        } catch (RuntimeException e) {
            // 如果发生异常，回滚事务
            transactionManager.rollback(status);
            throw e;
        } catch (Exception e) {
            // 如果发生异常，回滚事务
            transactionManager.rollback(status);
            throw new RuntimeException(e);
        }
    }
}
