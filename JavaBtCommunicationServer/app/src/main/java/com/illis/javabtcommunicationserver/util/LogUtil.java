package com.illis.javabtcommunicationserver.util;

import com.orhanobut.logger.Logger;

public class LogUtil
{
    public static void logForCheckMemoryAddressAndHashCode(String message, Object obj) {
        if (obj != null)
            Logger.i(message + "-> hashCode = " + obj.hashCode() + "\n"
                    + message + "-> System.identityHashCode = " + System.identityHashCode(obj));
        OrderOfObjectsAfterGCMain.printAddresses(message + "-> Memory Address =", obj);
    }
}
