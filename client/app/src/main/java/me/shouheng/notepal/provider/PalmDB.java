package me.shouheng.notepal.provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Resources need to modify when add new model:
 * <p>
 * !!!!!Please mark sure that the Column is same to the Schema!!!!
 * <p>
 * 1. Extends {@link me.shouheng.notepal.model.Model} to add model;
 * 2. Implement {@link me.shouheng.notepal.provider.schema.BaseSchema} to add schema;
 * 3. Extends {@link BaseStore} to add store;
 * 4. Modify {@link me.shouheng.notepal.model.enums.ModelType} to register model type;
 * 5. Modify {@link me.shouheng.notepal.provider.helper.TimelineHelper} to enable in time line;
 * 6. Modify {@link me.shouheng.notepal.model.ModelFactory} to add model factory.
 * <p>
 * Others:
 * 7. Extends {@link me.shouheng.notepal.viewmodel.BaseViewModel} to add view mode;
 * 8. Extends {@link me.shouheng.notepal.repository.BaseRepository} to add repository.
 * 9. Most import modify {@link PalmDB#VERSION}.
 * <p>
 * Created by wangshouheng on 2017/3/13.
 */
public class PalmDB extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "NotePal.db";
    private static final int VERSION = 7;

    private Context mContext;
    @SuppressLint("StaticFieldLeak")
    private static PalmDB sInstance = null;

    public static PalmDB getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (PalmDB.class) {
                if (sInstance == null) {
                    sInstance = new PalmDB(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private PalmDB(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        AttachmentsStore.getInstance(mContext).onCreate(db);
        TimelineStore.getInstance(mContext).onCreate(db);
        CategoryStore.getInstance(mContext).onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        AttachmentsStore.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
        TimelineStore.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
        CategoryStore.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public synchronized void close() {
        super.close();
        sInstance = null;
    }
}
