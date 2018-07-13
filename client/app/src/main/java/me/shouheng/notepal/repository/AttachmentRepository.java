package me.shouheng.notepal.repository;

import me.shouheng.notepal.PalmApp;
import me.shouheng.notepal.model.Attachment;
import me.shouheng.notepal.provider.AttachmentsStore;
import me.shouheng.notepal.provider.BaseStore;

/**
 * Created by WangShouheng on 2018/3/13.
 */
public class AttachmentRepository extends BaseRepository<Attachment> {

    @Override
    protected BaseStore<Attachment> getStore() {
        return AttachmentsStore.getInstance(PalmApp.getContext());
    }
}
