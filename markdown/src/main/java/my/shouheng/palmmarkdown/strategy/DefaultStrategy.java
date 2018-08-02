package my.shouheng.palmmarkdown.strategy;

import android.text.TextUtils;
import android.widget.EditText;

import me.urakalee.markdown.action.ActionStrategy;

/**
 * Created by wangshouheng on 2017/10/7.
 */
public abstract class DefaultStrategy implements ActionStrategy {

    @Override
    public void quote(String source, int selectionStart, int selectionEnd, String selection, EditText editor) {
        String result = isSingleLine(source, selectionStart) ? "> " + selection : "\n>" + selection;
        editor.getText().replace(selectionStart, selectionEnd, result);
        editor.setSelection(selectionStart + result.length());
    }

    @Override
    public void bold(String source, int selectionStart, int selectionEnd, String selection, EditText editor) {
        String result = "**" + selection + "**";
        if (TextUtils.isEmpty(selection)) {
            editor.getText().replace(selectionStart, selectionEnd, result);
            editor.setSelection(selectionStart + result.length() - 2);
            return;
        }
        editor.getText().replace(selectionStart, selectionEnd, result);
        editor.setSelection(selectionStart + result.length());
    }

    @Override
    public void italic(String source, int selectionStart, int selectionEnd, String selection, EditText editor) {
        String result = "*" + selection + "*";
        if (TextUtils.isEmpty(selection)) {
            editor.getText().replace(selectionStart, selectionEnd, result);
            editor.setSelection(selectionStart + result.length() - 1);
            return;
        }
        editor.getText().replace(selectionStart, selectionEnd, result);
        editor.setSelection(selectionStart + result.length());
    }

    @Override
    public void codeBlock(String source, int selectionStart, int selectionEnd, String selection, EditText editor) {
        String result = isSingleLine(source, selectionStart) ? "```\n" + selection + "\n```\n" : "\n```\n" + selection + "\n```\n";
        if (TextUtils.isEmpty(selection)) {
            editor.getText().replace(selectionStart, selectionEnd, result);
            editor.setSelection(selectionStart + result.length() - 5);
            return;
        }
        editor.getText().replace(selectionStart, selectionEnd, result);
        editor.setSelection(selectionStart + result.length());
    }

    @Override
    public void strike(String source, int selectionStart, int selectionEnd, String selection, EditText editor) {
        String result = "~~" + selection + "~~";
        if (TextUtils.isEmpty(selection)) {
            editor.getText().replace(selectionStart, selectionEnd, result);
            editor.setSelection(selectionStart + result.length() - 2);
            return;
        }
        editor.getText().replace(selectionStart, selectionEnd, result);
        editor.setSelection(selectionStart + result.length());
    }

    @Override
    public void horizontalLine(String source, int selectionStart, int selectionEnd, String selection, EditText editor) {
        String result = isSingleLine(source, selectionStart) ? "-------\n" : "\n-------\n";
        editor.getText().replace(selectionStart, selectionStart, result);
    }

    @Override
    public void xml(String source, int selectionStart, int selectionEnd, String selection, EditText editor) {
        String result = "`" + selection + "`";
        if (TextUtils.isEmpty(selection)) {
            editor.getText().replace(selectionStart, selectionEnd, result);
            editor.setSelection(selectionStart + result.length() - 1);
            return;
        }
        editor.getText().replace(selectionStart, selectionEnd, result);
        editor.setSelection(selectionStart + result.length());
    }

    @Override
    public void link(String source, int selectionStart, int selectionEnd, String title, String link, EditText editor) {
        String result = title == null ?
                (link == null ? "[]()" : "[](" + link + ")") :
                (link == null ? "[" + title + "]()" : "[" + title + "](" + link + ")");
        editor.getText().insert(selectionStart, result);
        editor.setSelection(selectionStart + result.length());
    }

    @Override
    public void table(String source, int selectionStart, int selectionEnd, int rows, int cols, EditText editor) {
        StringBuilder sb = new StringBuilder();
        int i;

        if (!isTwoSingleLines(source, selectionStart)) {
            sb.append(isSingleLine(source, selectionStart) ? "\n" : "\n\n");
        }

        sb.append("|");
        for (i = 0; i < cols; i++) sb.append(" HEADER |");

        sb.append("\n|");
        for (i = 0; i < cols; i++) sb.append(":----------:|");

        sb.append("\n");
        for (int i2 = 0; i2 < rows; i2++) {
            sb.append("|");
            for (i = 0; i < cols; i++) {
                sb.append("            |");
            }
            sb.append("\n");
        }

        String result = sb.toString();
        editor.getText().insert(selectionStart, result);
        editor.setSelection(selectionStart + result.length());
    }

    @Override
    public void image(String source, int selectionStart, int selectionEnd, String title, String imgUri, EditText editor) {
        imgUri = TextUtils.isEmpty(imgUri) ? "" : imgUri;

        String result = isSingleLine(source, selectionStart) ? "![" + title + "](" + imgUri + ")"
                : "\n![" + title + "](" + imgUri + ")";

        editor.getText().insert(selectionStart, result);
        editor.setSelection(TextUtils.isEmpty(imgUri) ? result.length() + selectionStart - 1
                : result.length() + selectionStart);
    }

    @Override
    public void mark(String source, int selectionStart, int selectionEnd, String selection, EditText editor) {
        String result = "==" + selection + "==";
        if (TextUtils.isEmpty(selection)) {
            editor.getText().replace(selectionStart, selectionEnd, result);
            editor.setSelection(selectionStart + result.length() - 2);
            return;
        }
        editor.getText().replace(selectionStart, selectionEnd, result);
        editor.setSelection(selectionStart + result.length());
    }

    @Override
    public void mathJax(String source, int selectionStart, int selectionEnd, String exp, boolean isSingleLine, EditText editor) {
        if (isSingleLine) {
            String result = isSingleLine(source, selectionStart) ? "$$" + exp + "$$" : "\n$$" + exp + "$$";
            editor.getText().insert(selectionStart, result);
            editor.setSelection(result.length() + selectionStart);
        } else {
            String result = "$" + exp + "$";
            editor.getText().replace(selectionStart, selectionEnd, result);
            editor.setSelection(selectionStart + result.length() - 1);
        }
    }

    @Override
    public void sub(String source, int selectionStart, int selectionEnd, String selection, EditText editor) {
        String result = "~" + selection + "~";
        if (TextUtils.isEmpty(selection)) {
            editor.getText().replace(selectionStart, selectionEnd, result);
            editor.setSelection(selectionStart + result.length() - 1);
            return;
        }
        editor.getText().replace(selectionStart, selectionEnd, result);
        editor.setSelection(selectionStart + result.length());
    }

    @Override
    public void sup(String source, int selectionStart, int selectionEnd, String selection, EditText editor) {
        String result = "^" + selection + "^";
        if (TextUtils.isEmpty(selection)) {
            editor.getText().replace(selectionStart, selectionEnd, result);
            editor.setSelection(selectionStart + result.length() - 1);
            return;
        }
        editor.getText().replace(selectionStart, selectionEnd, result);
        editor.setSelection(selectionStart + result.length());
    }

    @Override
    public void footNote(String source, int selectionStart, int selectionEnd, EditText editor) {
    }

    private boolean isSingleLine(String source, int selectionStart) {
        if (source.isEmpty()) return true;
        source = source.substring(0, selectionStart);
        return source.length() == 0 || source.charAt(source.length() - 1) == '\n';
    }

    private boolean isTwoSingleLines(String source, int selectionStart) {
        source = source.substring(0, selectionStart);
        return source.length() >= 2
                && source.charAt(source.length() - 1) == '\n'
                && source.charAt(source.length() - 2) == '\n';
    }
}
