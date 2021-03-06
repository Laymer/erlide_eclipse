package org.erlide.cover.views.model;

import java.io.Serializable;
import java.util.Calendar;

/**
 *
 * Data model for statatistics view
 *
 * @author Aleksandra Lipiec <aleksandra.lipiec@erlang-solutions.com>
 *
 */
public class StatsTreeModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private static StatsTreeModel model;

    private ICoverageObject root;
    private String timestamp;
    private boolean changed;

    private StatsTreeModel() {
        initialize();
        timestamp = "";
        setChanged(false);
    }

    public static synchronized StatsTreeModel getInstance() {
        if (StatsTreeModel.model == null) {
            StatsTreeModel.model = new StatsTreeModel();
        }
        return StatsTreeModel.model;
    }

    /**
     * Change model if results are restored
     *
     * @param mod
     */
    public static void changeInstance(final StatsTreeModel mod) {
        StatsTreeModel.model = mod;
    }

    public ICoverageObject getRoot() {
        return root;
    }

    /**
     * Clear the model tree
     */
    public void clear() {
        root.removeAllChildren();
        root.setLiniesCount(0);
        root.setCoverCount(0);

        String timeTmp = String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) +
                String.format("%02d",
                        Calendar.getInstance().get(Calendar.MONTH) + 1) +
                String.format("%02d",
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) +
                String.format("%02d",
                        Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) +
                String.format("%02d",
                        Calendar.getInstance().get(Calendar.MINUTE)) +
                String.format("%02d",
                        Calendar.getInstance().get(Calendar.SECOND));

        timestamp = timeTmp;

        ModuleSet.clear();
        root.setHtmlPath(null);
        setChanged(false);
    }

    /**
     * add lines for project
     *
     * @param allLines
     * @param coveredLines
     */
    public void addTotal(final int allLines, final int coveredLines) {
        final int all = root.getLinesCount() + allLines;
        final int cov = root.getCoverCount() + coveredLines;
        root.setLiniesCount(all);
        root.setCoverCount(cov);
    }

    public void setIndex(final String path) {
        root.setHtmlPath(path);
    }

    public void setRootLabel(final String name) {
        root.setLabel(name);
    }

    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Searching for specified element in a model
     *
     * @param name
     * @return
     */
    public ICoverageObject treeSearch(final String name) {
        return root.treeSearch(name);
    }

    private void initialize() {
        root = new StatsTreeObject("total", 0, 0, ObjectType.PROJECT);
    }

    public void setChanged(final boolean changed) {
        this.changed = changed;
    }

    public boolean isChanged() {
        return changed;
    }

}
