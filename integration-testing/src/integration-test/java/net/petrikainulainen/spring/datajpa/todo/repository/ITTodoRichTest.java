package net.petrikainulainen.spring.datajpa.todo.repository;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import net.petrikainulainen.spring.datajpa.config.PersistenceContext;
import net.petrikainulainen.spring.datajpa.todo.model.Todo;
import net.petrikainulainen.spring.datajpa.todo.model.TodoRich;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Siarhei Shchahratsou <s.siarhei@gmail.com>
 * @since 07.12.2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PersistenceContext.class})
//@ContextConfiguration(locations = {"classpath:exampleApplicationContext-persistence.xml"})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup("todoData.xml")
public class ITTodoRichTest {

    @Autowired
    private TodoRepository repository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private class Executor implements Runnable {
        protected final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

        private final PlatformTransactionManager ptm;
        private final Long todoId;
        private final TodoRepository repository;
        private final TodoUpdater updater;
        private final TransactionTemplate transactionTemplate;
        private final long sleepBeforeCommit;

        public Executor(Long todoId, PlatformTransactionManager ptm, TodoRepository repository, TodoUpdater updater, long ms, String name) {
            this.todoId = todoId;
            this.ptm = ptm;
            this.repository = repository;
            this.updater = updater;
            this.sleepBeforeCommit = ms;
            this.transactionTemplate = new TransactionTemplate(ptm, new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
            Thread.currentThread().setName(name);
        }

        @Override
        public void run() {
            logger.info("Running");
            try {
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        logger.info("FIND BY ID");
                        TodoRich todo = (TodoRich) repository.findOne(todoId);
                        try {
                            TimeUnit.MILLISECONDS.sleep(sleepBeforeCommit);
                            logger.info("UPDATE");
                            updater.update(todo);
                            TimeUnit.MILLISECONDS.sleep(sleepBeforeCommit);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                    }
                });
            } catch (Exception e) {
                logger.warn("Exception has occurred", e);
            } finally {
                logger.info("END OF TX");
            }
        }
    }

    private interface TodoUpdater {
        void update(TodoRich todo);
    }

    @Test
    public void concurrentUpdates() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        final TodoUpdater upd = new TodoUpdater() {
            @Override
            public void update(TodoRich todo) {
                todo.getUpdatedRef().setName("upd name");
                todo.setDescription("updated description");
            }
        };

        final TodoUpdater crt = new TodoUpdater() {
            @Override
            public void update(TodoRich todo) {
                todo.getCreatedRef().setName("crt name");
            }
        };
        final Long todoId = 1L;
        executorService.execute(new Executor(todoId, transactionManager, repository, upd, 100, "updT"));
        executorService.execute(new Executor(todoId, transactionManager, repository, crt, 2000, "crtT"));

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executorService.shutdownNow();

        Todo todo = repository.findOne(todoId);
        assertNotNull(todo);
        assertThat(todo, CoreMatchers.instanceOf(TodoRich.class));
        assertEquals("upd1", TodoRich.class.cast(todo).getUpdatedRef().getName());
        assertEquals("crt name", TodoRich.class.cast(todo).getCreatedRef().getName());
    }
}
