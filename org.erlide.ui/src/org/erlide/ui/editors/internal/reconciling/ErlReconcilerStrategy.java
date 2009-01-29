/*******************************************************************************
 * Copyright (c) 2005 Vlad Dumitrescu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vlad Dumitrescu
 *******************************************************************************/
package org.erlide.ui.editors.internal.reconciling;

// import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.erlide.core.erlang.IErlModule;
import org.erlide.runtime.ErlLogger;
import org.erlide.ui.editors.erl.ErlangEditor;
import org.erlide.ui.util.ErlModelUtils;

public class ErlReconcilerStrategy implements IErlReconcilingStrategy,
		IReconcilingStrategyExtension {

	private IErlModule fModule;
	private final ErlangEditor fEditor;
	// private IDocument fDoc;
	private IProgressMonitor mon;

	// private boolean initialInsert;

	public ErlReconcilerStrategy(final ErlangEditor editor) {
		fEditor = editor;
	}

	public void setDocument(final IDocument document) {
		if (fEditor == null) {
			return;
		}
		// fDoc = document;
	}

	public void reconcile(final DirtyRegion dirtyRegion, final IRegion subRegion) {
		ErlLogger.error("reconcile called");
	}

	// @SuppressWarnings("boxing")
	// private OtpErlangObject mkReconcileMsg(final String string,
	// final DirtyRegion dirtyRegion, final IRegion subRegion) {
	// OtpErlangObject msg = new OtpErlangString(
	// "reconcile (message build failed)");
	// try {
	// String text = dirtyRegion.getText();
	// if (text == null) {
	// text = "";
	// }
	// msg = ErlUtils.format("{~a, {~i, ~i, ~a, ~b}, {~i, ~i}}", string,
	// dirtyRegion.getOffset(), dirtyRegion.getLength(),
	// dirtyRegion.getType(), text, subRegion.getOffset(),
	// subRegion.getLength());
	// } catch (final ParserException e) {
	// e.printStackTrace();
	// } catch (final RpcException e) {
	// e.printStackTrace();
	// }
	// return msg;
	// }

	public void reconcile(final IRegion partition) {
		ErlLogger.error("reconcile called");
	}

	public void initialReconcile() {
		// initialInsert = true;
		fModule = ErlModelUtils.getModule(fEditor);
		ErlLogger.debug("## initial reconcile "
				+ (fModule != null ? fModule.getName() : ""));
		if (fModule != null) {
			fModule.initialReconcile();
		}
		// notify(new OtpErlangAtom("initialReconcile"));
	}

	// private void notify(final OtpErlangObject msg) {
	// if (System.getProperty("erlide.reconcile.debug") != null) {
	// ErlLogger.debug("RECONCILE %s", msg.toString());
	// }
	// ErlangCore.getBackendManager().getIdeBackend().send("erlide_code_db",
	// msg);
	// }

	public void setProgressMonitor(final IProgressMonitor monitor) {
		mon = monitor;
	}

	public void uninstall() {
		if (fModule != null) {
			fModule.finalReconcile();
		}
	}

	public void chunkReconciled() {
		if (fModule != null) {
			fModule.postReconcile(mon);
		}
	}

	public void reconcile(final ErlDirtyRegion r) {
		if (fModule != null) {
			fModule.reconcileText(r.getOffset(), r.getLength(), r.getText(),
					mon);
		}

	}

}