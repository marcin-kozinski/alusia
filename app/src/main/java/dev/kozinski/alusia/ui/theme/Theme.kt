package dev.kozinski.alusia.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ForgetMeNot.`80`,
    onPrimary = ForgetMeNot.`20`,
    primaryContainer = ForgetMeNot.`30`,
    onPrimaryContainer = ForgetMeNot.`90`,
    inversePrimary = ForgetMeNot.`40`,
    secondary = Sky.`80`,
    onSecondary = Sky.`20`,
    secondaryContainer = Sky.`30`,
    onSecondaryContainer = Sky.`90`,
    tertiary = Heather.`80`,
    onTertiary = Heather.`20`,
    tertiaryContainer = Heather.`30`,
    onTertiaryContainer = Heather.`90`,
    background = Neutral.`10`,
    onBackground = Neutral.`90`,
    surface = Neutral.`10`,
    onSurface = Neutral.`90`,
    surfaceVariant = Neutral.`30`,
    onSurfaceVariant = Neutral.`80`,
    inverseSurface = Neutral.`90`,
    inverseOnSurface = Neutral.`20`,
    outline = Neutral.`60`,
)

private val LightColorScheme = lightColorScheme(
    primary = ForgetMeNot.`40`,
    onPrimary = ForgetMeNot.`100`,
    primaryContainer = ForgetMeNot.`90`,
    onPrimaryContainer = ForgetMeNot.`10`,
    inversePrimary = ForgetMeNot.`80`,
    secondary = Sky.`40`,
    onSecondary = Sky.`100`,
    secondaryContainer = Sky.`90`,
    onSecondaryContainer = Sky.`10`,
    tertiary = Heather.`40`,
    onTertiary = Heather.`100`,
    tertiaryContainer = Heather.`90`,
    onTertiaryContainer = Heather.`10`,
    background = Neutral.`99`,
    onBackground = Neutral.`10`,
    surface = Neutral.`99`,
    onSurface = Neutral.`10`,
    surfaceVariant = Neutral.`90`,
    onSurfaceVariant = Neutral.`30`,
    inverseSurface = Neutral.`20`,
    inverseOnSurface = Neutral.`95`,
    outline = Neutral.`50`,
)

@Composable
fun AlusiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }.also { println(it.toHexes()) }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            if (Build.VERSION.SDK_INT >= 23) {
                window.statusBarColor = Color.Transparent.toArgb()
                insetsController.isAppearanceLightStatusBars = !darkTheme
            } else {
                window.statusBarColor = colorScheme.scrim.toArgb()
            }
            if (Build.VERSION.SDK_INT >= 26) {
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
            if (Build.VERSION.SDK_INT >= 29) {
                window.navigationBarColor = Color.Transparent.toArgb()
            } else {
                window.navigationBarColor = colorScheme.scrim.toArgb()
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

fun ColorScheme.toHexes(): String {
    return "ColorScheme(" +
            "primary=${primary.toHex()}\n" +
            "onPrimary=${onPrimary.toHex()}\n" +
            "primaryContainer=${primaryContainer.toHex()}\n" +
            "onPrimaryContainer=${onPrimaryContainer.toHex()}\n" +
            "inversePrimary=${inversePrimary.toHex()}\n" +
            "secondary=${secondary.toHex()}\n" +
            "onSecondary=${onSecondary.toHex()}\n" +
            "secondaryContainer=${secondaryContainer.toHex()}\n" +
            "onSecondaryContainer=${onSecondaryContainer.toHex()}\n" +
            "tertiary=${tertiary.toHex()}\n" +
            "onTertiary=${onTertiary.toHex()}\n" +
            "tertiaryContainer=${tertiaryContainer.toHex()}\n" +
            "onTertiaryContainer=${onTertiaryContainer.toHex()}\n" +
            "background=${background.toHex()}\n" +
            "onBackground=${onBackground.toHex()}\n" +
            "surface=${surface.toHex()}\n" +
            "onSurface=${onSurface.toHex()}\n" +
            "surfaceVariant=${surfaceVariant.toHex()}\n" +
            "onSurfaceVariant=${onSurfaceVariant.toHex()}\n" +
            "surfaceTint=${surfaceTint.toHex()}\n" +
            "inverseSurface=${inverseSurface.toHex()}\n" +
            "inverseOnSurface=${inverseOnSurface.toHex()}\n" +
            "error=${error.toHex()}\n" +
            "onError=${onError.toHex()}\n" +
            "errorContainer=${errorContainer.toHex()}\n" +
            "onErrorContainer=${onErrorContainer.toHex()}\n" +
            "outline=${outline.toHex()}\n" +
            "outlineVariant=${outlineVariant.toHex()}\n" +
            "scrim=${scrim.toHex()}\n" +
            ")"
}

fun Color.toHex(): String {
    return "0x%X".format(toArgb())
}
