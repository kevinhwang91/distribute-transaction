package org.jackson.puppy.tcc.transaction.dubbo.context;

import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import org.jackson.puppy.tcc.transaction.api.TransactionContext;
import org.jackson.puppy.tcc.transaction.api.TransactionContextEditor;

import java.lang.reflect.Method;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class DubboTransactionContextEditor implements TransactionContextEditor {

	public static final String TRANSACTION_CONTEXT = "TRANSACTION_CONTEXT";

	@Override
	public TransactionContext get(Object target, Method method, Object[] args) {

		String context = RpcContext.getContext().getAttachment(TRANSACTION_CONTEXT);

		if (StringUtils.isNotEmpty(context)) {
			return JSON.parseObject(context, TransactionContext.class);
		}

		return null;
	}

	@Override
	public void set(TransactionContext transactionContext, Object target, Method method, Object[] args) {

		RpcContext.getContext().setAttachment(TRANSACTION_CONTEXT, JSON.toJSONString(transactionContext));
	}

	@Override
	public void clear() {
		RpcContext.getContext().removeAttachment(TRANSACTION_CONTEXT);
	}
}
