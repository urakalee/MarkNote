package me.shouheng.notepal.model;

import java.io.Serializable;
import java.util.Date;

import me.shouheng.notepal.model.enums.ItemStatus;
import me.shouheng.notepal.provider.annotation.Column;
import me.shouheng.notepal.provider.schema.BaseSchema;
import me.shouheng.notepal.util.TimeUtils;


/**
 * Created by wangshouheng on 2017/3/13. */
public class Model implements Serializable {

    @Column(name = BaseSchema.ID)
    protected long id;

    @Column(name = BaseSchema.CODE)
    protected long code;

    @Column(name = BaseSchema.USER_ID)
    protected long userId;

    @Column(name = BaseSchema.ADDED_TIME)
    protected Date addedTime;

    @Column(name = BaseSchema.LAST_MODIFIED_TIME)
    protected Date lastModifiedTime;

    @Column(name = BaseSchema.LAST_SYNC_TIME)
    protected Date lastSyncTime;

    @Column(name = BaseSchema.STATUS)
    protected ItemStatus status;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Date getAddedTime() {
        return addedTime;
    }

    public void setAddedTime(Date addedTime) {
        this.addedTime = addedTime;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public Date getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(Date lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Model{" +
                "id=" + id +
                ", code=" + code +
                ", userId=" + userId +
                ", addedTime=" + TimeUtils.formatDate(addedTime, TimeUtils.DateFormat.YYYY_MMM_dd_E_hh_mm_a) +
                ", lastModifiedTime=" +  TimeUtils.formatDate(lastModifiedTime, TimeUtils.DateFormat.YYYY_MMM_dd_E_hh_mm_a) +
                ", lastSyncTime=" + TimeUtils.formatDate(lastSyncTime, TimeUtils.DateFormat.YYYY_MMM_dd_E_hh_mm_a) +
                ", status=" + (status == null ? "null" : status.name()) +
                '}';
    }
}
