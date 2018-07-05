package me.shouheng.notepal.model;

import android.net.Uri;

import me.shouheng.notepal.provider.annotation.Column;
import me.shouheng.notepal.provider.annotation.Table;
import me.shouheng.notepal.provider.schema.NoteSchema;

/**
 * Created by wangshouheng on 2017/5/12.
 */
@Table(name = NoteSchema.TABLE_NAME)
public class Note extends Model {

    @Column(name = NoteSchema.PARENT_CODE)
    private long parentCode;

    @Column(name = NoteSchema.TREE_PATH)
    private String treePath;

    @Column(name = NoteSchema.TITLE)
    private String title;

    @Column(name = NoteSchema.CONTENT_CODE)
    private long contentCode;

    @Column(name = NoteSchema.TAGS)
    private String tags;

    @Column(name = NoteSchema.PREVIEW_IMAGE)
    private Uri previewImage;

    @Column(name = NoteSchema.PREVIEW_CONTENT)
    private String previewContent;

    // region Android端字段，不计入数据库

    private Notebook notebook;

    private String content;

    private String tagsName;

    public Notebook getNotebook() {
        return notebook;
    }

    public void setNotebook(Notebook notebook) {
        this.notebook = notebook;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTagsName() {
        return tagsName;
    }

    public void setTagsName(String tagsName) {
        this.tagsName = tagsName;
    }

    // endregion

    public Note() {
    }

    public long getParentCode() {
        return parentCode;
    }

    public void setParentCode(long parentCode) {
        this.parentCode = parentCode;
    }

    public String getTreePath() {
        return treePath;
    }

    public void setTreePath(String treePath) {
        this.treePath = treePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getContentCode() {
        return contentCode;
    }

    public void setContentCode(long contentCode) {
        this.contentCode = contentCode;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Uri getPreviewImage() {
        return previewImage;
    }

    public void setPreviewImage(Uri previewImage) {
        this.previewImage = previewImage;
    }

    public String getPreviewContent() {
        return previewContent;
    }

    public void setPreviewContent(String previewContent) {
        this.previewContent = previewContent;
    }

    @Override
    public String toString() {
        return "Note{" +
                "parentCode=" + parentCode +
                ", treePath='" + treePath + '\'' +
                ", title='" + title + '\'' +
                ", contentCode=" + contentCode +
                ", tags='" + tags + '\'' +
                ", previewImage=" + previewImage +
                ", previewContent='" + previewContent + '\'' +
                ", content='" + content + '\'' +
                ", tagsName='" + tagsName + '\'' +
                "} " + super.toString();
    }
}
