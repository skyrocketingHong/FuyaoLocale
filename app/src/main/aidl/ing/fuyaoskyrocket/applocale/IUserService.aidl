package ing.fuyaoskyrocket.applocale;

interface IUserService {
    void exit() = 1;
    void destroy() = 16777114;
    int getUid() = 1000;

    // ILocaleManager
    void setApplicationLocales(String packageName, in LocaleList locales) = 2000;
    LocaleList getApplicationLocales(String packageName) = 2001;
    LocaleList getSystemLocales() = 2002;

    // Batch locale operations (single Binder round-trip)
    String[] getApplicationLocaleTags(in String[] packageNames) = 2003;
    String[] setApplicationLocalesForPackages(in String[] packageNames, in LocaleList locales) = 2004;

    // IActivityManager
    void forceStopPackage(String packageName) = 3000;

    // IActivityTaskManager
    String getFirstRunningTaskPackage() = 4000;
}
