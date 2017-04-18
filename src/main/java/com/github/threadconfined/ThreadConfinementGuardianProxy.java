package com.github.threadconfined;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;

class ThreadConfinementGuardianProxy implements MethodHandler {

    private final Object target;
    private final WeakReference<Thread> confinedToThreadRef;
    private final ThreadConfinement.ViolationCallback violationCallback;

    ThreadConfinementGuardianProxy(Object target,
                                   Thread expectedThread,
                                   ThreadConfinement.ViolationCallback violationCallback) {
        this.target = target;
        this.confinedToThreadRef = new WeakReference<>(expectedThread);
        this.violationCallback = violationCallback;
    }
    
    
    /* (non-Javadoc)
     * @see javassist.util.proxy.MethodHandler#invoke(
     *          java.lang.Object, java.lang.reflect.Method, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(Object self, Method overridden, Method forwarder,
            Object[] args) throws Throwable {

        Thread confinedToThread = this.confinedToThreadRef.get();
        if (Thread.currentThread() != confinedToThread) {
            violationCallback.onAccessFromUnexpectedThread(
                    target, confinedToThread, Thread.currentThread());
        }
        
        return overridden.invoke(target, args);
    }

}
