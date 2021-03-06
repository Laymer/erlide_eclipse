/*******************************************************************************
 * Copyright (c) 2010 György Orosz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     György Orosz - initial API and implementation
 ******************************************************************************/
package org.erlide.wrangler.refactoring.codeinspection;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.erlide.util.ErlLogger;
import org.erlide.wrangler.refactoring.backend.internal.WranglerBackendManager;
import org.erlide.wrangler.refactoring.exception.WranglerException;
import org.erlide.wrangler.refactoring.selection.IErlSelection;
import org.erlide.wrangler.refactoring.util.GlobalParameters;

import com.abstratt.graphviz.GraphViz;
import com.ericsson.otp.erlang.OtpErlangBoolean;

/**
 * This class is for handling commands which are from wrangler code inspection
 * functionality, and result a graph.
 *
 * @author Gyorgy Orosz
 *
 */
public class GraphResultingInspectionHandler extends AbstractHandler {
    protected static final String CYCLYC_VIEW_ID = "cyclic";
    protected static final String FUNCTION_CALL_GRAPH_VIEW_ID = "functioncallgraph";
    protected static final String MODULE_GRAPH_VIEW_ID = "modulegraph";
    protected static final String IMPROPER_DEPENDECIES_VIEW_ID = "improperdependecies";

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final String actionId = event.getCommand().getId();
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .getActiveEditor().setFocus();
        try {
            GlobalParameters.setSelection(PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage().getSelection());
        } catch (final WranglerException e1) {
            MessageDialog.openError(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    "Error", e1.getMessage());
            return null;
        }
        try {
            final File tmpFile = File.createTempFile("wrangler_graph_", ".dot");
            tmpFile.deleteOnExit();

            final IErlSelection wranglerSelection = GlobalParameters
                    .getWranglerSelection();

            if ("org.erlide.wrangler.refactoring.codeinspection.cyclicdependencies".equals(actionId)) {
                final Boolean answer = MessageDialog.openQuestion(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        "Labels", "Label edges with function names called?");
                runInspection("Cyclic module dependency", GraphResultingInspectionHandler.CYCLYC_VIEW_ID,
                        "There is no cyclic dependent modules in the project!", tmpFile,
                        "cyclic_dependent_modules", "ssx", tmpFile.getAbsolutePath(),
                        wranglerSelection.getSearchPath(), new OtpErlangBoolean(answer));
            } else if ("org.erlide.wrangler.refactoring.codeinspection.generatefunctioncallgraph".equals(actionId)) {
                runInspection("Function callgraph", GraphResultingInspectionHandler.FUNCTION_CALL_GRAPH_VIEW_ID,
                        "There is no dependent functions in the module!", tmpFile,
                        "gen_function_callgraph", "sss", tmpFile.getAbsolutePath(),
                        wranglerSelection.getFilePath(),
                        wranglerSelection.getSearchPath());

            } else if ("org.erlide.wrangler.refactoring.codeinspection.generatemodulegraph".equals(actionId)) {
                final Boolean answer = MessageDialog.openQuestion(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        "Labels", "Label edges with function names called?");
                runInspection("Module dependency graph", GraphResultingInspectionHandler.MODULE_GRAPH_VIEW_ID,
                        "There is no dependent modules in the project!", tmpFile,
                        "gen_module_graph", "ssx", tmpFile.getAbsolutePath(),
                        wranglerSelection.getSearchPath(), new OtpErlangBoolean(answer));

            } else if ("org.erlide.wrangler.refactoring.codeinspection.improperdependecies".equals(actionId)) {
                runInspection("Improper module dependencies",
                        GraphResultingInspectionHandler.IMPROPER_DEPENDECIES_VIEW_ID,
                        "There is no improper module dependecies!", tmpFile,
                        "improper_inter_module_calls", "ss", tmpFile.getAbsolutePath(),
                        wranglerSelection.getSearchPath());

            }

        } catch (final Exception e) {
            ErlLogger.error(e);
        }
        return event;
    }

    /**
     * Run code inspection function, and shows the result in the workbench
     *
     * @param viewtTitle
     *            title of the view
     * @param noResultMessage
     *            if there is no result, this message will be displayed
     * @param tmpFile
     *            temp .dot file
     * @param functionName
     *            function to call
     * @param signature
     *            parameters signature
     * @param parameters
     *            function parameters
     *
     */
    protected void runInspection(final String viewtTitle, final String secondaryID,
            final String noResultMessage, final File tmpFile, final String functionName,
            final String signature, final Object... parameters) {
        try {
            CodeInspectionViewsManager.hideView(CodeInspectionViewsManager.GRAPH_VIEW,
                    secondaryID);
            final Boolean b = WranglerBackendManager.getRefactoringBackend()
                    .callSimpleInspection(functionName, signature, parameters);
            if (b) {
                try (final FileInputStream fis = new FileInputStream(tmpFile)) {
                    if (fis.available() > 0) {
                        byte[] data = GraphViz.load(fis, "png", 0, 0);
                        ImageData imageData = new ImageData(new ByteArrayInputStream(data));
                        Image img = new Image(Display.getCurrent(), imageData);
                        CodeInspectionViewsManager.showDotImage(img, viewtTitle,
                                secondaryID, tmpFile);
                    } else {
                        MessageDialog.openInformation(
                                GlobalParameters.getEditor().getSite().getShell(),
                                viewtTitle, noResultMessage);
                    }
                }
            } else {
                MessageDialog.openError(GlobalParameters.getEditor().getSite().getShell(),
                        "Internal error", "Internal error occured. Please report it!");
            }
        } catch (final IOException e) {
            ErlLogger.error(e);
        } catch (final CoreException e) {
            ErlLogger.error(e);
        } catch (final Exception e) {
            ErlLogger.error(e);
        }

    }
}
