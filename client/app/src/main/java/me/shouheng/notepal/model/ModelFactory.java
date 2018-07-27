package me.shouheng.notepal.model;

import android.support.annotation.NonNull;

import java.util.Date;

import me.shouheng.notepal.PalmApp;
import me.shouheng.notepal.config.TextLength;
import me.shouheng.notepal.model.enums.ItemStatus;
import me.shouheng.notepal.model.enums.ModelType;
import me.shouheng.notepal.model.enums.Operation;
import me.shouheng.notepal.model.enums.Portrait;
import me.shouheng.notepal.model.enums.WeatherType;
import me.shouheng.notepal.util.ColorUtils;
import me.shouheng.notepal.util.LogUtils;
import me.urakalee.next2.model.Note;

/**
 * Created by wangshouheng on 2017/11/17.
 */
public class ModelFactory {

    private static long getLongCode() {
        return System.currentTimeMillis();
    }

    @NonNull
    private static <T extends Model> T getModel(Class<T> itemType) {
        try {
            T newItem = itemType.newInstance();
            newItem.setCode(getLongCode());
            newItem.setAddedTime(new Date());
            newItem.setLastModifiedTime(new Date());
            newItem.setLastSyncTime(new Date(0));
            newItem.setStatus(ItemStatus.NORMAL);
            return newItem;
        } catch (Exception e) {
            LogUtils.e(e);
        }
        return null;
    }

    @NonNull
    public static Notebook newNotebook() {
        Notebook notebook = getModel(Notebook.class);
        notebook.setColor(ColorUtils.primaryColor(PalmApp.getContext()));
        return notebook;
    }

    @NonNull
    public static Note newNote() {
        return getModel(Note.class);
    }

    public static Attachment getAttachment() {
        Attachment attachment = getModel(Attachment.class);
        attachment.setModelType(ModelType.NONE);
        attachment.setModelCode(0);
        return attachment;
    }

    public static Location getLocation() {
        Location location = getModel(Location.class);
        location.setModelType(ModelType.NONE);
        return location;
    }

    public static Weather getWeather(WeatherType type, int temperature) {
        Weather weather = getModel(Weather.class);
        weather.setType(type);
        weather.setTemperature(temperature);
        return weather;
    }

    public static MindSnagging getMindSnagging() {
        return getModel(MindSnagging.class);
    }

    public static Category getCategory() {
        Category category = getModel(Category.class);
        category.setPortrait(Portrait.FOLDER);
        category.setCategoryOrder(0);
        // use the primary color as the category color
        category.setColor(ColorUtils.primaryColor(PalmApp.getContext()));
        return category;
    }

    public static <T extends Model> TimeLine getTimeLine(T model, Operation operation) {
        TimeLine timeLine = new TimeLine();

        timeLine.setCode(ModelFactory.getLongCode());
        timeLine.setUserId(model.getUserId());
        timeLine.setAddedTime(new Date());
        timeLine.setLastModifiedTime(new Date());
        timeLine.setLastSyncTime(new Date(0));
        timeLine.setStatus(ItemStatus.NORMAL);

        timeLine.setOperation(operation);
        timeLine.setModelName(getModelName(model));
        timeLine.setModelCode(model.getCode());
        timeLine.setModelType(ModelType.getTypeByName(model.getClass()));
        return timeLine;
    }

    public static Feedback getFeedback() {
        return getModel(Feedback.class);
    }

    private static <M extends Model> String getModelName(M model) {
        String modelName = null;
        if (model instanceof Attachment) {
            return ((Attachment) model).getUri().toString();
        } else if (model instanceof MindSnagging) {
            modelName = ((MindSnagging) model).getContent();
        } else if (model instanceof Note) {
            modelName = ((Note) model).getTitle();
        } else if (model instanceof Notebook) {
            modelName = ((Notebook) model).getTitle();
        } else if (model instanceof Location) {
            Location location = ((Location) model);
            modelName = location.getCountry() + "|" + location.getCity() + "|" + location.getDistrict();
        } else if (model instanceof Weather) {
            Weather weather = ((Weather) model);
            modelName = PalmApp.getStringCompact(weather.getType().nameRes) + "|" + weather.getTemperature();
        }
        if (modelName != null && modelName.length() > TextLength.TIMELINE_TITLE_LENGTH.length) {
            return modelName.substring(0, TextLength.TIMELINE_TITLE_LENGTH.length);
        }
        return modelName;
    }
}
