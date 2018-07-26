package me.shouheng.notepal.model;

import android.net.Uri;
import android.support.annotation.NonNull;

import org.joda.time.LocalDate;

import java.io.File;

import me.shouheng.notepal.provider.annotation.Column;
import me.shouheng.notepal.provider.annotation.Table;
import me.shouheng.notepal.provider.schema.NoteSchema;
import me.urakalee.next2.config.TimeConfig;

/**
 * Created by wangshouheng on 2017/5/12.
 */
@Table(name = NoteSchema.TABLE_NAME)
public class Note extends Model {

    public static final String DEFAULT_SUFFIX = ".md";

    @Column(name = NoteSchema.TREE_PATH)
    private String treePath;

    @Column(name = NoteSchema.TITLE)
    private String title;

    @Column(name = NoteSchema.CONTENT_CODE)
    private long attachmentCode;

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

    private String timePath;
    private String originTitle;
    private File originFile;

    public String getFileName() {
        if (title.endsWith(DEFAULT_SUFFIX)) {
            return title;
        } else {
            return title + DEFAULT_SUFFIX;
        }
    }

    public void setTitleByFileName(@NonNull String name) {
        if (name.endsWith(DEFAULT_SUFFIX)) {
            title = name.substring(0, name.length() - DEFAULT_SUFFIX.length());
        } else {
            title = name;
        }
    }

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

    public String getTimePath() {
        return timePath;
    }

    public void setTimePath(String timePath) {
        this.timePath = timePath;
    }

    public void generateTimePath() {
        setTimePath(LocalDate.now().toString(TimeConfig.MONTH_FORMAT));
    }

    public String getOriginTitle() {
        return originTitle;
    }

    public String getOriginFileName() {
        if (originTitle.endsWith(DEFAULT_SUFFIX)) {
            return originTitle;
        } else {
            return originTitle + DEFAULT_SUFFIX;
        }
    }

    public void setOriginTitle(String originTitle) {
        this.originTitle = originTitle;
    }

    public boolean isNewNote() {
        return originTitle == null;
    }

    public void finishNew() {
        originTitle = title;
    }

    public boolean needRename() {
        return originTitle != null && !title.equals(originTitle);
    }

    public File getOriginFile() {
        return originFile;
    }

    public void setOriginFile(File originFile) {
        this.originFile = originFile;
    }

    public void finishRename() {
        originTitle = title;
        originFile = null;
    }

    // endregion

    public Note() {
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

    public long getAttachmentCode() {
        return attachmentCode;
    }

    public void setAttachmentCode(long attachmentCode) {
        this.attachmentCode = attachmentCode;
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
                "treePath='" + treePath + '\'' +
                ", title='" + title + '\'' +
                ", attachmentCode=" + attachmentCode +
                ", tags='" + tags + '\'' +
                ", previewImage=" + previewImage +
                ", previewContent='" + previewContent + '\'' +
                ", content='" + content + '\'' +
                ", tagsName='" + tagsName + '\'' +
                "} " + super.toString();
    }
}
