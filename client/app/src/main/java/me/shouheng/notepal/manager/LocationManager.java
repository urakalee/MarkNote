package me.shouheng.notepal.manager;

public class LocationManager {

    private static LocationManager sInstance;

    public static LocationManager getInstance() {
        if (sInstance == null) {
            synchronized (LocationManager.class) {
                if (sInstance == null) {
                    sInstance = new LocationManager();
                }
            }
        }
        return sInstance;
    }

    private LocationManager() {
    }

    public void locate() {
    }

    public void stop() {
    }
}
