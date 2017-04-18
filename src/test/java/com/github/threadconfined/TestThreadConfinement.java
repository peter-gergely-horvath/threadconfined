package com.github.threadconfined;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;



public class TestThreadConfinement {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    @Test
    public void testAccessFromSameThread() {

        HashSet<String> guardedHashSet = ThreadConfinement.threadConfined(new HashSet<String>());

        guardedHashSet.add("Foobar");

        Assert.assertTrue(guardedHashSet.contains("Foobar"));
    }

    @Test(expectedExceptions=ThreadConfinementViolationException.class)
    public void testAccessFromAnotherThread() {

        try {
            final HashSet<String> guardedHashSet = ThreadConfinement.threadConfined(new HashSet<String>());

            guardedHashSet.add("Foobar");

            Future<Boolean> future = EXECUTOR.submit(() -> guardedHashSet.contains("Foobar"));

            Boolean result = future.get();

            Assert.assertTrue(result);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw launderThrowable(e.getCause());
        }
    }

    @AfterClass
    public static void afterClass() {

        List<Runnable> tasksNeverCommencedExecution = EXECUTOR.shutdownNow();
        if (!tasksNeverCommencedExecution.isEmpty()) {
            throw new IllegalStateException(
                    String.format(
                            "%s tasks not commenced yet retrieved from ExecutorService",
                            tasksNeverCommencedExecution.size()));

        }
    }

    private static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof java.lang.Error) {
            throw (java.lang.Error) t;
        } else if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        } else {
            return new RuntimeException(t);
        }

    }
}
