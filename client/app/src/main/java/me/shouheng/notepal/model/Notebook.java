package me.shouheng.notepal.model;

import me.shouheng.notepal.provider.annotation.Column;
import me.shouheng.notepal.provider.annotation.Table;
import me.shouheng.notepal.provider.schema.NotebookSchema;

/**
 * Created by wangshouheng on 2017/7/23.
 */
@Table(name = NotebookSchema.TABLE_NAME)
public class Notebook extends Model implements Selectable {

    @Column(name = NotebookSchema.TITLE)
    private String title;

    @Column(name = NotebookSchema.TREE_PATH)
    private String treePath;

    @Column(name = NotebookSchema.COLOR)
    private int color;

    // region Android端字段，不计入数据库

    /**
     * 目录中内容的数量
     */
    private int count;
    private boolean isSelected;

    private boolean create;
    private String originTitle;

    // endregion

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTreePath() {
        return treePath;
    }

    public void setTreePath(String treePath) {
        this.treePath = treePath;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    public boolean needCreate() {
        return create;
    }

    public void create(String title) {
        this.title = title;
        create = true;
    }

    public boolean needRename() {
        return originTitle != null;
    }

    public String getOriginTitle() {
        return originTitle;
    }

    public void rename(String newTitle) {
        originTitle = title;
        title = newTitle;
    }

    public void reset() {
        create = false;
        originTitle = null;
    }

    @Override
    public String toString() {
        return "Notebook{" +
                "title='" + title + '\'' +
                ", treePath='" + treePath + '\'' +
                ", color=" + color +
                ", count=" + count +
                "} " + super.toString();
    }
}
