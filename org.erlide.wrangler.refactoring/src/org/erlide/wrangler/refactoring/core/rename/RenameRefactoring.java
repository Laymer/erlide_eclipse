package org.erlide.wrangler.refactoring.core.rename;

import org.erlide.jinterface.rpc.RpcException;
import org.erlide.jinterface.rpc.RpcResult;
import org.erlide.wrangler.refactoring.core.RefactoringParameters;
import org.erlide.wrangler.refactoring.core.WranglerRefactoring;

import com.ericsson.otp.erlang.OtpErlangList;

public abstract class RenameRefactoring extends WranglerRefactoring {

	public RenameRefactoring(RefactoringParameters parameters) {
		super(parameters);
	}

	@Override
	public abstract String getName();

	@Override
	protected abstract RpcResult sendRPC(String filePath,
			OtpErlangList searchPath) throws RpcException;

}
