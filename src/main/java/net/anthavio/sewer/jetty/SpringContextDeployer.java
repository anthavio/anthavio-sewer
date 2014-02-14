package net.anthavio.sewer.jetty;

import java.util.Map;

import net.anthavio.spring.ContextHelper;

import org.eclipse.jetty.plus.jndi.Resource;
import org.eclipse.jetty.plus.jndi.Transaction;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * @author vanek
 * 
 * Jetty LifeCycle for (shared parent) spring context deployment (before WebContextu deployment) 
 * 
 * Also can bind TransactionManager/UserTransaction into JNDI
 */
public class SpringContextDeployer extends AbstractLifeCycle {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private BeanFactoryReference contextRef;

	private String locatorFactorySelector;

	private String parentContextKey;

	private String transactionManagerJndi = "TransactionManager";

	private String userTransactionJndi = "UserTransaction";

	public void setLocatorFactorySelector(String locatorFactorySelector) {
		this.locatorFactorySelector = locatorFactorySelector;
	}

	public void setParentContextKey(String parentContextKey) {
		this.parentContextKey = parentContextKey;
	}

	public void setTransactionManagerJndi(String transactionManagerJndi) {
		this.transactionManagerJndi = transactionManagerJndi;
	}

	public void setUserTransactionJndi(String userTransactionJndi) {
		this.userTransactionJndi = userTransactionJndi;
	}

	@Override
	public void doStart() throws Exception {
		if (locatorFactorySelector == null) {
			locatorFactorySelector = ContextHelper.DEFAULT_SELECTOR;
		}
		if (parentContextKey == null) {
			throw new IllegalArgumentException("parentContextKey is not specified");
		}

		log.info(parentContextKey + " context initialization started");
		long start = System.currentTimeMillis();

		BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance(locatorFactorySelector);
		contextRef = locator.useBeanFactory(parentContextKey);

		ApplicationContext context = (ApplicationContext) contextRef.getFactory();

		Map<?, ?> tmTypes = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, JtaTransactionManager.class);

		if (tmTypes.size() > 0) {
			JtaTransactionManager jtaTm = (JtaTransactionManager) tmTypes.values().iterator().next();

			log.info("Binding TransactionManager to " + transactionManagerJndi);
			new Resource(transactionManagerJndi, jtaTm.getTransactionManager());
			log.info("Binding UserTransaction to " + userTransactionJndi);
			new Transaction(jtaTm.getUserTransaction());
		} else {
			log.info("Spring JtaTransactionManager not found, cannot bind TM/TX into JNDI");
		}
		log.info(parentContextKey + " context initialization completed in " + (System.currentTimeMillis() - start) + " ms");

	}

	@Override
	public void doStop() {
		log.info(parentContextKey + " context release started");
		long start = System.currentTimeMillis();

		if (contextRef != null) {
			contextRef.release();
		}
		log.info(parentContextKey + " context release completed in " + (System.currentTimeMillis() - start) + " ms");
	}

}
