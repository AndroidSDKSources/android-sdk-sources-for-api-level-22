/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package jsr166;

import junit.framework.*;

public class ThreadTest extends JSR166TestCase {

    static class MyHandler implements Thread.UncaughtExceptionHandler {
        public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * getUncaughtExceptionHandler returns ThreadGroup unless set,
     * otherwise returning value of last setUncaughtExceptionHandler.
     */
    public void testGetAndSetUncaughtExceptionHandler() {
        // these must be done all at once to avoid state
        // dependencies across tests
        Thread current = Thread.currentThread();
        ThreadGroup tg = current.getThreadGroup();
        MyHandler eh = new MyHandler();
        assertEquals(tg, current.getUncaughtExceptionHandler());
        current.setUncaughtExceptionHandler(eh);
        assertEquals(eh, current.getUncaughtExceptionHandler());
        current.setUncaughtExceptionHandler(null);
        assertEquals(tg, current.getUncaughtExceptionHandler());
    }

    /**
     * getDefaultUncaughtExceptionHandler returns value of last
     * setDefaultUncaughtExceptionHandler.
     */
    public void testGetAndSetDefaultUncaughtExceptionHandler() {
        // BEGIN android-remove (when running as cts the RuntimeInit will 
        // set a default handler)
        // assertEquals(null, Thread.getDefaultUncaughtExceptionHandler());
        // END android-remove

        // failure due to securityException is OK.
        // Would be nice to explicitly test both ways, but cannot yet.
        try {
            Thread current = Thread.currentThread();
            ThreadGroup tg = current.getThreadGroup();
            MyHandler eh = new MyHandler();
            Thread.setDefaultUncaughtExceptionHandler(eh);
            assertEquals(eh, Thread.getDefaultUncaughtExceptionHandler());
            Thread.setDefaultUncaughtExceptionHandler(null);
        }
        catch (SecurityException ok) {
        }
        assertEquals(null, Thread.getDefaultUncaughtExceptionHandler());
    }

    // How to test actually using UEH within junit?

}
