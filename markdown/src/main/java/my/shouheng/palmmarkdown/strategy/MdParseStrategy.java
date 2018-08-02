package my.shouheng.palmmarkdown.strategy;

import android.widget.EditText;

/**
 * Created by wangshouheng on 2017/10/7.
 */
public interface MdParseStrategy {

    void h1(String source, int selectionStart, int selectionEnd, String selection, EditText editor);

    void list(String source, int selectionStart, int selectionEnd, EditText editor);

    void todo(String source, int selectionStart, int selectionEnd, EditText editor);

    void indent(String source, int selectionStart, int selectionEnd, String selection, EditText editor);

    void dedent(String source, int selectionStart, int selectionEnd, String selection, EditText editor);

    void quote(String source, int selectionStart, int selectionEnd, String selection, EditText editor);

    void bold(String source, int selectionStart, int selectionEnd, String selection, EditText editor);

    void italic(String source, int selectionStart, int selectionEnd, String selection, EditText editor);

    void codeBlock(String source, int selectionStart, int selectionEnd, String selection, EditText editor);

    void strike(String source, int selectionStart, int selectionEnd, String selection, EditText editor);

    void horizontalLine(String source, int selectionStart, int selectionEnd, String selection, EditText editor);

    void xml(String source, int selectionStart, int selectionEnd, String selection, EditText editor);

    void link(String source, int selectionStart, int selectionEnd, String title, String link, EditText editor);

    void table(String source, int selectionStart, int selectionEnd, int rows, int cols, EditText editor);

    void image(String source, int selectionStart, int selectionEnd, String title, String imgUri, EditText editor);

    void mark(String source, int selectionStart, int selectionEnd, String selection, EditText editor);

    void mathJax(String source, int selectionStart, int selectionEnd, String exp, boolean isSingleLine, EditText editor);

    void sub(String source, int selectionStart, int selectionEnd, String selection, EditText editor);

    void sup(String source, int selectionStart, int selectionEnd, String selection, EditText editor);

    void footNote(String source, int selectionStart, int selectionEnd, EditText editor);
}
