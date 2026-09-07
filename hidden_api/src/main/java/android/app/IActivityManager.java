package android.app;

import android.content.res.Configuration;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

public interface IActivityManager extends IInterface {

    void forceStopPackage(String packageName, int userId);

    Configuration getConfiguration();

    void updatePersistentConfiguration(Configuration values);

    void updatePersistentConfigurationWithAttribution(
        Configuration values,
        String callingPackage,
        String callingAttributionTag
    );

    abstract class Stub extends Binder implements IActivityManager {

        public static IActivityManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }

    }
}
