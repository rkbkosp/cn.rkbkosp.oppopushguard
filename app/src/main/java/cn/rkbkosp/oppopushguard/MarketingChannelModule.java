// SPDX-License-Identifier: MIT
// Copyright (c) 2026 rkbkosp

package cn.rkbkosp.oppopushguard;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import io.github.libxposed.api.XposedInterface.ExceptionMode;
import io.github.libxposed.api.XposedInterface.Invoker;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam;

/** Blocks posts on OPPO Push's content/marketing channel for non-system apps. */
public final class MarketingChannelModule extends XposedModule {
    private static final String TAG = "OppoPushGuard";
    private static final String CHANNEL_ID = "push_oplus_category_content";
    private static final int PER_USER_RANGE = 100000;
    private static final int USER_SYSTEM = 0;
    private final AtomicLong blocked = new AtomicLong();
    private final AtomicLong lookupFailures = new AtomicLong();
    private volatile Context systemContext;

    @Override
    public void onSystemServerStarting(SystemServerStartingParam param) {
        try {
            Class<?> service = Class.forName(
                    "com.android.server.notification.NotificationManagerService",
                    false,
                    param.getClassLoader());
            int installed = 0;
            for (Method method : service.getDeclaredMethods()) {
                Class<?>[] types = method.getParameterTypes();
                if (!method.getName().equals("enqueueNotificationInternal")
                        || types.length < 8
                        || types[0] != String.class
                        || types[2] != Integer.TYPE
                        || types[6] != Notification.class
                        || types[7] != Integer.TYPE
                        || (method.getReturnType() != Void.TYPE
                            && method.getReturnType() != Boolean.TYPE)) {
                    continue;
                }
                final boolean returnsBoolean = method.getReturnType() == Boolean.TYPE;
                hook(method).setExceptionMode(ExceptionMode.PROTECTIVE).intercept(chain -> {
                    try {
                        String pkg = (String) chain.getArg(0);
                        Notification notification = (Notification) chain.getArg(6);
                        int incomingUserId = (Integer) chain.getArg(7);
                        int callingUid = (Integer) chain.getArg(2);
                        if (shouldBlock(chain.getThisObject(), pkg, notification,
                                incomingUserId, callingUid)) {
                            long count = blocked.incrementAndGet();
                            if (count == 1 || count % 100 == 0) {
                                log(Log.INFO, TAG, "Blocked " + count
                                        + " marketing-channel notifications");
                            }
                            return returnsBoolean ? Boolean.FALSE : null;
                        }
                    } catch (Throwable error) {
                        long count = lookupFailures.incrementAndGet();
                        if (count == 1 || count % 100 == 0) {
                            log(Log.ERROR, TAG, "Channel policy lookup failed ("
                                    + count + ")", error);
                        }
                    }
                    return chain.proceed();
                });
                installed++;
            }
            log(installed == 3 ? Log.INFO : Log.WARN, TAG,
                    "Installed " + installed + " notification enqueue hooks");
        } catch (Throwable error) {
            log(Log.ERROR, TAG, "Could not install notification hooks", error);
        }
    }

    private boolean shouldBlock(Object service, String pkg, Notification notification,
                                int incomingUserId, int callingUid) throws Throwable {
        if (pkg == null || notification == null
                || !CHANNEL_ID.equals(notification.getChannelId())) {
            return false;
        }
        int userId = incomingUserId >= 0
                ? incomingUserId : Math.max(USER_SYSTEM, callingUid / PER_USER_RANGE);
        Context context = getSystemContext(service);
        PackageManager pm = context.getPackageManager();
        Method getApplicationInfoAsUser = pm.getClass().getMethod(
                "getApplicationInfoAsUser", String.class, int.class, int.class);
        ApplicationInfo app = (ApplicationInfo) getInvoker(getApplicationInfoAsUser)
                .setType(Invoker.Type.ORIGIN)
                .invoke(pm, pkg, 0, userId);
        if (app == null) {
            return false;
        }
        int systemFlags = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (app.flags & systemFlags) == 0;
    }

    private Context getSystemContext(Object service) throws Throwable {
        Context cached = systemContext;
        if (cached != null) {
            return cached;
        }
        Class<?> type = service.getClass();
        while (type != null) {
            try {
                Method getContext = type.getDeclaredMethod("getContext");
                Context context = (Context) getInvoker(getContext)
                        .setType(Invoker.Type.ORIGIN).invoke(service);
                if (context != null) {
                    systemContext = context;
                    return context;
                }
            } catch (NoSuchMethodException ignored) {
                // Continue searching inherited implementations.
            }
            type = type.getSuperclass();
        }
        throw new IllegalStateException("NotificationManagerService has no Context");
    }
}
