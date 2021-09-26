package org.saipal.fmisutil.util;

import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class ManualTransaction {
	@Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager) ;
}
