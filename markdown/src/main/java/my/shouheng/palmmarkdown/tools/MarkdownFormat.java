package my.shouheng.palmmarkdown.tools;

import android.support.annotation.DrawableRes;

import my.shouheng.palmmarkdown.R;

/**
 * Created by wangshouheng on 2017/6/29.
 */
public enum MarkdownFormat {
    H1(6, R.drawable.ic_shortcut_format_header_1, true),

    LIST(16, R.drawable.ic_format_list_bulleted_black_24dp, true),
    TODO(19, R.drawable.ic_check_box_black_24dp, true),

    INDENT(25, R.drawable.ic_format_indent_increase_white_24dp, true),
    DEDENT(26, R.drawable.ic_format_indent_decrease_white_24dp, true),
    QUOTE(2, R.drawable.ic_format_quote_black_24dp, true),

    STRIKE(4, R.drawable.ic_strikethrough_s_black_24dp, true),
    HORIZONTAL_LINE(5, R.drawable.ic_remove_black_24dp, true),

    XML(12, R.drawable.ic_shortcut_xml, true),
    CODE_BLOCK(3, R.drawable.ic_shortcut_console, true),

    MARK(18, R.drawable.ic_font_download_black_24dp, true),
    ITALIC(0, R.drawable.ic_format_italic_black_24dp, true),
    BOLD(1, R.drawable.ic_format_bold_black_24dp, true),

    MATH_JAX(21, R.drawable.ic_functions_grey_24dp, true),
    SUB_SCRIPT(22, R.drawable.ic_subscript, true),
    SUPER_SCRIPT(23, R.drawable.ic_superscript, true),

    // line2
    ATTACHMENT(17, R.drawable.ic_attach_file_white, true),
    LINK(13, R.drawable.ic_link, true),
    TABLE(14, R.drawable.ic_grid_on_grey_24dp, true),

    FOOTNOTE(24, R.drawable.ic_directions_black_24dp, false);

    public final int id;

    @DrawableRes
    public final int drawableResId;

    /**
     * should the menu item be visible to user
     */
    public final boolean visible;

    MarkdownFormat(int id, @DrawableRes int drawableResId, boolean visible) {
        this.id = id;
        this.drawableResId = drawableResId;
        this.visible = visible;
    }
}
