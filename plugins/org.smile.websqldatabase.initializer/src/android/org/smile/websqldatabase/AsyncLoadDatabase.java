package org.smile.websqldatabase;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.lang.String;
import java.util.zip.ZipInputStream;

public class AsyncLoadDatabase extends AsyncTask<Void, Integer, Void> {
    private static final String TAG = "AsyncLoadDatabase";

    private static final String DATABASE_FOLDER = "app_database/";

    private ProgressDialog progressDialog;

    private Context context;
    private DatabaseInitializable app;
    private Exception exception;
    private static DatabaseConfig config;

    public AsyncLoadDatabase(DatabaseInitializable obj) {
        Log.d(TAG, "AsyncLoadDatabase constructor");
        this.context = ((Activity) obj).getApplicationContext().getApplicationContext();
        this.app = obj;
        this.config = app.getDatabaseConfig();
    }

    /*public void detach() {
        Log.d(TAG, "detach");
        this.app = null;
    }*/

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "doInBackground");
        final String dbPath = getDatabasePath(context, config);
        final String dbsDbPath = getDatabasesDBPath(context, config);

        try {
            File databaseFile = new File(dbPath);
            File databasesDBFile = new File(dbsDbPath);

            if (databaseFile.exists() || databasesDBFile.exists()) {
                databaseFile.delete();
                databasesDBFile.delete();
            }

            copyFromAsset(config.getDatabaseZippedName(), dbPath);
            copyFromAsset(config.getDatabaseDBName(), dbsDbPath);

        } catch (IOException e) {
            this.exception = e;
            e.printStackTrace();
        }

        return null;
    }

    private void copyFromAsset(String assetSource, String destination) throws IOException {
        createFolderIfNotExist(destination);

        InputStream in = null;
        OutputStream out = null;

        try {
            in = context.getAssets().open(assetSource);

            // Handling zipped database
            if (assetSource.endsWith("zip")) {
                in = new ZipInputStream(in);
                ((ZipInputStream) in).getNextEntry(); // "Reads the next ZIP file entry and positions the stream at the beginning of the entry data."
            }

            out = new FileOutputStream(destination);
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);

        } catch (IOException e) {
            throw e;
        } finally {
            closeSilently(in);
            closeSilently(out);
        }

    }

    private void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "onPreExecute");
        progressDialog = ProgressDialog.show((Context) app, config.getLoadingTitle(),
                config.getLoadingText(), true, false);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.d(TAG, "onPostExecute");
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        if (app != null) {
            app.loadWebApp();
        }
    }

    private static void createFolderIfNotExist(String filePath) {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
    }

    private static String getDatabasePath(Context context, DatabaseConfig config) {
        return context.getApplicationInfo().dataDir + "/" + DATABASE_FOLDER + "file__0/" + config.getDatabaseName();
    }

    private static String getDatabasesDBPath(Context context, DatabaseConfig config) {
        return context.getApplicationInfo().dataDir + "/" + DATABASE_FOLDER + config.getDatabaseDBName();
    }

    public static boolean shouldLoadDatabase(DatabaseInitializable app) {
        // TODO: Check the files size in case of incomplete copy (whatever the reason)?
        Context context = (Context) app;
        DatabaseConfig config = app.getDatabaseConfig();
        return !new File(getDatabasePath(context, config)).exists() || !new File(getDatabasesDBPath(context, config)).exists();
    }
}
