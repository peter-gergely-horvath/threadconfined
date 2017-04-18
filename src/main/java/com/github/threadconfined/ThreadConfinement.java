package com.github.threadconfined;

import javassist.util.proxy.MethodFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ThreadConfinement {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadConfinement.class);

    private ThreadConfinement() {
        throw new AssertionError(ThreadConfinement.class + " is a static utility class, no instances allowed!");
    }

    public static <T> T threadConfined(T object) {
        return threadConfined(object, Thread.currentThread(), ViolationAction.THROW_EXCEPTION);
    }


    public static <T> T threadConfined(T object, Thread thread,
                                       ViolationAction violationAction) {
        return threadConfined(object, thread,
                (ViolationCallback) violationAction);
    }

    public static <T> T threadConfined(T object,
                                       ViolationAction violationAction) {
        return threadConfined(object, Thread.currentThread(),
                (ViolationCallback) violationAction);
    }

    public static <T> T threadConfined(T object, Thread thread,
                                       ViolationCallback violationCallback)
            throws ProxyCreationFailedException {

        try {

            if (violationCallback instanceof ViolationAction
                    && (violationCallback) == ViolationAction.NO_OP) {
                return object;
            }

            javassist.util.proxy.ProxyFactory factory = new javassist.util.proxy.ProxyFactory();
            factory.setSuperclass(object.getClass());

            factory.setFilter(IGNORE_FINALIZE_PROXY_METHOD_FILTER);


            @SuppressWarnings("unchecked")
            Class<T> proxyObjectClass = factory.createClass();
            T proxyObj = proxyObjectClass.newInstance();

            ThreadConfinementGuardianProxy handler = new ThreadConfinementGuardianProxy(
                    object, thread, violationCallback);

            ((javassist.util.proxy.ProxyObject) proxyObj).setHandler(handler);

            return proxyObj;

        } catch (RuntimeException | InstantiationException | IllegalAccessException e) {
            throw new ProxyCreationFailedException(
                    "Failed to create the proxy object", e);
        }
    }

    @FunctionalInterface
    public interface ViolationCallback {

        void onAccessFromUnexpectedThread(Object targetObject, Thread confinedToThread, Thread actualThread);
    }

    public enum ViolationAction implements ViolationCallback {
        NO_OP {
            @Override
            public void onAccessFromUnexpectedThread(
                    Object targetObject, Thread confinedToThread, Thread actualThread) {
                // NO-OP
            }
        },
        THROW_EXCEPTION {
            @Override
            public void onAccessFromUnexpectedThread(
                    Object targetObject, Thread confinedToThread, Thread actualThread) {

                throw new ThreadConfinementViolationException(
                        "Illegal access from thread '"
                                + actualThread
                                + "': object should only be accessed from '"
                                + confinedToThread + "'");

            }
        },
        LOG_WARNING {
            @Override
            public void onAccessFromUnexpectedThread(
                    Object targetObject, Thread confinedToThread, Thread actualThread) {

                if (LOGGER.isWarnEnabled()) {
                    String warningMessage = String.format(
                            "Illegal access from thread '%s': object should only be accessed from '%s'",
                            actualThread, confinedToThread);

                    LOGGER.warn(warningMessage, new Exception());
                }
            }
        };

    }

    private static final MethodFilter IGNORE_FINALIZE_PROXY_METHOD_FILTER =
            method -> !(method.getName().equals("finalize")
                    && method.getParameterTypes().length == 0);


}
