package me.urakalee.next2.support.theme

import me.shouheng.notepal.R

/**
 * @author Uraka.Lee
 */
enum class ThemeColor(
        val colorRes: Int,
        val darkColorRes: Int,
        val identifyName: String,
        val displayName: String) {

    GREEN(R.color.md_green_500, R.color.md_green_700, "primary_green", "#4CAF50"),
    RED(R.color.theme_red, R.color.md_red_700, "primary_red", "#DB4437"),
    PINK(R.color.theme_pink, R.color.md_pink_300, "primary_pink", "#FB7299"),
    BLUE(R.color.md_indigo_500, R.color.md_indigo_700, "primary_blue", "#3F51B5"),
    TEAL(R.color.md_teal_500, R.color.md_teal_700, "primary_teal", "#009688"),
    ORANGE(R.color.theme_orange, R.color.theme_orange_dark, "primary_orange", "#FF9800"),
    DEEP_PURPLE(R.color.md_deep_purple_500, R.color.md_deep_purple_700, "primary_deep_purple", "#673AB7"),
    LIGHT_BLUE(R.color.theme_light_blue, R.color.theme_light_blue_dark, "primary_light_blue", "#617FDE"),
    BROWN(R.color.md_brown_500, R.color.md_brown_700, "primary_brown", "#795548"),
    BLUE_GREY(R.color.md_blue_grey_500, R.color.md_blue_grey_700, "primary_blue_grey", "#607D8B"),
    WHITE(R.color.theme_white, R.color.md_grey_400, "primary_white", "#D9D9D9"),
    BLACK(R.color.theme_black, R.color.theme_black_dark, "primary_black", "#272B35"),
    LIGHT_GREEN(R.color.theme_light_green, R.color.theme_light_green_dark, "primary_light_green", "#06CE90");

    companion object {

        val defaultColor = BLUE_GREY

        fun getByPrimaryName(primaryName: String): ThemeColor {
            return values().firstOrNull {
                it.identifyName == primaryName
            } ?: defaultColor
        }
    }
}
