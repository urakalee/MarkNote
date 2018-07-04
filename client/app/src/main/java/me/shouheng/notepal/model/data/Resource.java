package me.shouheng.notepal.model.data;

import static me.shouheng.notepal.model.data.LoadStatus.FAILED;
import static me.shouheng.notepal.model.data.LoadStatus.LOADING;
import static me.shouheng.notepal.model.data.LoadStatus.SUCCESS;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by wang shouheng on 2018/3/13. */
public class Resource<T> {

    @NonNull
    public LoadStatus status;

    @Nullable
    public T data;

    @Nullable
    public String message;

    /**
     * reserved field */
    private Long udf1;

    private Resource(@NonNull LoadStatus status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(SUCCESS, data, null);
    }

    public static <T> Resource<T> error(String msg, @Nullable T data) {
        return new Resource<>(FAILED, data, msg);
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(LOADING, data, null);
    }

    public Long getUdf1() {
        return udf1;
    }

    public void setUdf1(Long udf1) {
        this.udf1 = udf1;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "status=" + status +
                ", data=" + data +
                ", message='" + message + '\'' +
                ", udf1=" + udf1 +
                '}';
    }
}
