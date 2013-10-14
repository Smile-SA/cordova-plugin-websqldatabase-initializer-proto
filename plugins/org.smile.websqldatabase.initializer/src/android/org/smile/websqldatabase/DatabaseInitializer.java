package org.smile.websqldatabase;

public abstract class DatabaseInitializer {
    /**
     * Method to call to launch Cordova application with automatic database initialization
     */
    public static void load(DatabaseInitializable app){
        boolean shouldLoadDatabase = AsyncLoadDatabase.shouldLoadDatabase(app);

        if (shouldLoadDatabase) {
            new AsyncLoadDatabase(app).execute();
        } else {
            app.loadWebApp();
        }
    }
}
