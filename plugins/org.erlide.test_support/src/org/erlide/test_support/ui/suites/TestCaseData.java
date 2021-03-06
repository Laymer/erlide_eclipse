package org.erlide.test_support.ui.suites;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.erlide.util.ErlLogger;
import org.erlide.util.erlang.OtpBindings;
import org.erlide.util.erlang.OtpErlang;
import org.erlide.util.erlang.OtpParserException;

import com.ericsson.otp.erlang.OtpErlangException;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.google.common.collect.Lists;

public class TestCaseData {

    enum TestState {
        // order is important!
        //@formatter:off
        NOT_RUN,
        SUCCESS,
        SKIPPED,
        RUNNING,
        FAILED
        //@formatter:on
    }

    public class FailLocations {

        private final Collection<FailLocation> locations;

        public FailLocations(final Collection<OtpErlangObject> locs) {
            locations = Lists.newArrayList();
            for (final OtpErlangObject item : locs) {
                locations.add(new FailLocation(item));
            }
            // first element points to ourselves, ignore it
            locations.remove(locations.iterator().next());
        }

        public Collection<FailLocation> getLocations() {
            return locations;
        }

        public boolean isEmpty() {
            return locations.isEmpty();
        }

    }

    public class FailLocation {

        private final OtpErlangObject location;

        public FailLocation(final OtpErlangObject location) {
            this.location = location;
        }

        @Override
        public String toString() {
            return location.toString();
        }

    }

    public class FailReason {
        private final Collection<FailStackItem> items;
        private final String reason;

        public FailReason(final String reason, final Collection<OtpErlangObject> stack) {
            this.reason = reason;
            items = Lists.newArrayList();
            for (final OtpErlangObject item : stack) {
                items.add(new FailStackItem(item));
            }
        }

        public Collection<FailStackItem> getStackItems() {
            return items;
        }

        public String getReason() {
            return reason;
        }

        public FailStackItem getFirstStackItem() {
            if (items.isEmpty()) {
                try {
                    return new FailStackItem(
                            OtpErlang.getTermParser().parse("{unknown,unknown,0}"));
                } catch (final OtpParserException e) {
                    // ignored
                }
            }
            return items.iterator().next();
        }
    }

    public class FailStackItem {

        private final OtpErlangObject item;
        private String m;
        private String f;
        private OtpErlangObject a;

        public FailStackItem(final OtpErlangObject item) {
            this.item = item;
        }

        @Override
        public String toString() {
            try {
                final OtpBindings b = OtpErlang.match("{M:a, F:a, A}", item);
                m = b.getQuotedAtom("M");
                f = b.getQuotedAtom("F");
                a = b.get("A");
                final String aa = a.toString();
                final String args;
                if (a instanceof OtpErlangLong) {
                    args = " / " + aa;
                } else {
                    final String aas = aa.length() > 2 ? aa.substring(1, aa.length() - 2)
                            : "";
                    args = " ( " + aas + " )";
                }
                return m + " : " + f + args;
            } catch (final Exception e) {
                ErlLogger.warn(e);
            }
            return item.toString();
        }

        public String getModule() {
            return m;
        }

        public String getFunction() {
            return f;
        }

    }

    private final String suite;
    private final String testcase;
    private TestState state;
    private FailReason failReason;
    private FailLocations failLocations;
    private OtpErlangObject skipComment;

    public TestCaseData(final String mod, final String fun) {
        suite = mod;
        testcase = fun;
        state = TestState.NOT_RUN;
    }

    @Override
    public String toString() {
        return suite + ":" + testcase;
    }

    public void setRunning() {
        state = TestState.RUNNING;
    }

    public void setSuccesful() {
        state = TestState.SUCCESS;
    }

    public void setFailed(final OtpErlangObject reason,
            final Collection<OtpErlangObject> locations) {
        state = TestState.FAILED;
        failReason = parseReason(reason);
        failLocations = new FailLocations(locations);
    }

    static final List<OtpErlangObject> NO_STACK = new ArrayList<>();

    private FailReason parseReason(final OtpErlangObject reason) {
        OtpBindings b;
        try {
            b = OtpErlang.match("{Cause, Stack}", reason);
            if (b == null) {
                return new FailReason("internal error: " + reason.toString(), TestCaseData.NO_STACK);
            }
            final Collection<OtpErlangObject> stack = b.getList("Stack");
            return new FailReason(b.get("Cause").toString(), stack);
        } catch (final OtpParserException e) {
            ErlLogger.warn(e);
        } catch (final OtpErlangException e) {
            ErlLogger.warn(e);
        }
        return null;
    }

    public String getModule() {
        return suite;
    }

    public String getFunction() {
        return testcase;
    }

    public TestState getState() {
        return state;
    }

    public FailLocations getFailLocations() {
        return failLocations;
    }

    public FailReason getFailStack() {
        return failReason;
    }

    public void setSkipped(final OtpErlangObject comment) {
        state = TestState.SKIPPED;
        skipComment = comment;
    }

    public OtpErlangObject getSkipComment() {
        return skipComment;
    }
}
