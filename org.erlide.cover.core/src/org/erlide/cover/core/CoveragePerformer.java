package org.erlide.cover.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.erlide.core.backend.BackendException;
import org.erlide.core.model.erlang.IErlModule;
import org.erlide.core.rpc.RpcException;
import org.erlide.cover.api.CoverException;
import org.erlide.cover.api.IConfiguration;
import org.erlide.cover.api.ICoveragePerformer;
import org.erlide.cover.constants.CoverConstants;
import org.erlide.cover.views.model.StatsTreeModel;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;

public class CoveragePerformer implements ICoveragePerformer {

    private static CoveragePerformer performer;

    private Collection<String> coverNodes;
    private IConfiguration config;

    private final Logger log; // logger

    private CoveragePerformer() {
        log = Activator.getDefault();
        coverNodes = new LinkedList<String>();
    }

    public static synchronized CoveragePerformer getPerformer() {
        if (performer == null) {
            performer = new CoveragePerformer();
        }
        return performer;
    }

    public synchronized void startCover(final Collection<String> nodes)
            throws CoverException {

        final StatsTreeModel model = StatsTreeModel.getInstance();
        model.clear();
        if (CoverBackend.getInstance().getAnnotationMaker() != null) {
            CoverBackend.getInstance().getAnnotationMaker()
                    .clearAllAnnotations();
        }

        for (final ICoverObserver obs : CoverBackend.getInstance()
                .getListeners()) {
            obs.eventOccured(new CoverEvent(CoverStatus.UPDATE));
        }

        boolean different = false;
        for (final String node : nodes) {
            if (!coverNodes.contains(node)) {
                different = true;
                break;
            }
        }

        if (coverNodes.isEmpty() || different) {

            coverNodes = nodes;
            log.info(CoverBackend.getInstance().getBackend().getFullNodeName());
            coverNodes.add(CoverBackend.getInstance().getBackend()
                    .getFullNodeName());

            // TODO: restarting

            final List<OtpErlangObject> names = new ArrayList<OtpErlangObject>(
                    coverNodes.size());
            for (final String name : coverNodes) {
                names.add(new OtpErlangAtom(name));
            }

            final OtpErlangList nodesList = new OtpErlangList(
                    names.toArray(new OtpErlangObject[0]));

            try {
                final OtpErlangObject res = CoverBackend
                        .getInstance()
                        .getBackend()
                        .call(CoverConstants.COVER_ERL_BACKEND,
                                CoverConstants.FUN_START, "x", nodesList);

                /*
                 * IPath location = Activator.getDefault().getStateLocation()
                 * .append(CoverConstants.REPORT_DIR); final File dir =
                 * location.toFile(); log.info(dir.getAbsolutePath());
                 * 
                 * res = CoverBackend .getInstance() .getBackend()
                 * .call(CoverConstants.COVER_ERL_BACKEND,
                 * CoverConstants.FUN_SET_REPORT_DIR, "s",
                 * dir.getAbsoluteFile());
                 */

                // TODO: check if res is ok

            } catch (final RpcException e) {
                e.printStackTrace();
                throw new CoverException(e.getMessage());
            }

        }

    }

    public synchronized void setCoverageConfiguration(final IConfiguration conf)
            throws CoverException {
        config = conf;

        StatsTreeModel.getInstance()
                .setRootLabel(config.getProject().getName());

        final IPath ppath = config.getProject().getWorkspaceProject()
                .getLocation();

        // set include files
        final List<OtpErlangObject> includes = new ArrayList<OtpErlangObject>(
                config.getModules().size());
        for (final IPath include : config.getIncludeDirs()) {
            log.info(ppath.append(include));
            includes.add(new OtpErlangList(ppath.append(include).toString()));
        }

        OtpErlangObject res;
        try {
            res = CoverBackend
                    .getInstance()
                    .getBackend()
                    .call(CoverConstants.COVER_ERL_BACKEND,
                            CoverConstants.FUN_SET_INCLUDES, "x", includes);
        } catch (final RpcException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // TODO: handle res

        recompileModules();
    }

    private void recompileModules() throws CoverException {
        OtpErlangObject res;
        final List<OtpErlangObject> paths = new ArrayList<OtpErlangObject>(
                config.getModules().size());
        for (final IErlModule module : config.getModules()) {
            if (module == null) {
                final String msg = "No such module at given project. Check your configuration";
                CoverBackend.getInstance().handleError(msg);
                throw new CoverException(msg);
            }
            log.info(module.getFilePath());
            paths.add(new OtpErlangList(module.getFilePath()));
        }

        try {
            res = CoverBackend
                    .getInstance()
                    .getBackend()
                    .call(CoverConstants.COVER_ERL_BACKEND,
                            CoverConstants.FUN_PREP, "x", paths);

            // TODO check the res
        } catch (final RpcException e) {
            e.printStackTrace();
            throw new CoverException(e.getMessage());
        }
    }

    public synchronized void analyse() throws CoverException {

        final List<OtpErlangObject> modules = new ArrayList<OtpErlangObject>(
                config.getModules().size());
        for (final IErlModule module : config.getModules()) {
            log.info(module.getModuleName());
            modules.add(new OtpErlangList(module.getModuleName()));
        }

        try {
            final OtpErlangObject res = CoverBackend
                    .getInstance()
                    .getBackend()
                    .call(CoverConstants.COVER_ERL_BACKEND,
                            CoverConstants.FUN_ANALYSE, "x", modules);

            if (res instanceof OtpErlangAtom
                    && res.toString().equals("no_file")) {
                return; // do sth more then??
            }

            /*
             * final StatsTreeModel model = StatsTreeModel.getInstance();
             * model.setIndex(res.toString().substring(1,
             * res.toString().length() - 1));
             */

        } catch (final RpcException e) {
            e.printStackTrace();
            throw new CoverException(e.getMessage());
        }
    }

    public IConfiguration getConfig() {
        return config;
    }

}
