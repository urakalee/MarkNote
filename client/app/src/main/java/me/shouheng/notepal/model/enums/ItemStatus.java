package me.shouheng.notepal.model.enums;

/**
 * Created by WngShhng on 2017/12/9.
 */
public enum ItemStatus {
    NORMAL(0),
    ARCHIVED(1),
    TRASHED(2),
    DELETED(3);

    public final int id;

    ItemStatus(int id) {
        this.id = id;
    }

    public static ItemStatus getStatusById(int id) {
        for (ItemStatus status : values()) {
            if (status.id == id) {
                return status;
            }
        }
        return NORMAL;
    }

    public boolean canEdit() {
        return this == NORMAL || this == ARCHIVED;
    }

    public boolean canMove() {
        return this == NORMAL;
    }

    public boolean canArchive() {
        return this == NORMAL;
    }

    public boolean canMoveOut() {
        return this == ARCHIVED || this == TRASHED;
    }

    public boolean canTrash() {
        return this == NORMAL || this == ARCHIVED;
    }

    public boolean canDelete() {
        return this == TRASHED;
    }
}
