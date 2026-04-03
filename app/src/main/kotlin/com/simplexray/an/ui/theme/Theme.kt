package com.simplexray.an.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.simplexray.an.common.ThemeMode

private val AmoledBackground = Color(0xFF000000)
private val AmoledSurfaceLow = Color(0xFF101010)
private val AmoledSurface = Color(0xFF141414)
private val AmoledSurfaceHigh = Color(0xFF1A1A1A)
private val AmoledSurfaceHighest = Color(0xFF202020)
private val AmoledOutline = Color(0xFF3A3A3A)
private val AmoledOutlineVariant = Color(0xFF2A2A2A)

object AppThemeAnimationDefaults {
    val ThemeColorAnimationSpec = tween<Color>(
        durationMillis = 550,
        easing = FastOutSlowInEasing
    )

    val TopAppBarScrollAnimationSpec = tween<Float>(
        durationMillis = 180,
        easing = FastOutSlowInEasing
    )

    val SystemBarColorAnimationSpec = tween<Color>(
        durationMillis = 500,
        easing = FastOutSlowInEasing
    )
}

@Composable
fun AppTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark, ThemeMode.Amoled -> true
        ThemeMode.Auto -> isSystemDark
    }

    val baseColorScheme = when {
        dynamicColor && isDark -> dynamicDarkColorScheme(context)
        dynamicColor && !isDark -> dynamicLightColorScheme(context)
        isDark -> darkColorScheme()
        else -> lightColorScheme()
    }

    val finalColorScheme = if (themeMode == ThemeMode.Amoled) {
        baseColorScheme.copy(
            background = AmoledBackground,
            surface = AmoledBackground,
            surfaceDim = AmoledBackground,
            surfaceBright = AmoledSurface,
            surfaceContainerLowest = AmoledBackground,
            surfaceContainerLow = AmoledSurfaceLow,
            surfaceContainer = AmoledSurface,
            surfaceContainerHigh = AmoledSurfaceHigh,
            surfaceContainerHighest = AmoledSurfaceHighest,
            outline = AmoledOutline,
            outlineVariant = AmoledOutlineVariant
        )
    } else {
        baseColorScheme
    }

    val animatedColorScheme = finalColorScheme.animated()

    MaterialTheme(
        colorScheme = animatedColorScheme,
        content = content
    )
}

object AppSwitchDefaults {
    @Composable
    fun colors(): SwitchColors {
        val colorScheme = MaterialTheme.colorScheme
        return SwitchDefaults.colors(
            checkedThumbColor = colorScheme.primary,
            checkedTrackColor = colorScheme.primaryContainer,
            checkedBorderColor = colorScheme.primary,
            uncheckedThumbColor = colorScheme.onSurfaceVariant,
            uncheckedTrackColor = colorScheme.surfaceContainerHighest,
            uncheckedBorderColor = colorScheme.outline,
            disabledCheckedThumbColor = colorScheme.onSurface.copy(alpha = 0.38f),
            disabledCheckedTrackColor = colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
            disabledCheckedBorderColor = colorScheme.outline.copy(alpha = 0.5f),
            disabledUncheckedThumbColor = colorScheme.onSurface.copy(alpha = 0.38f),
            disabledUncheckedTrackColor = colorScheme.surfaceContainerHighest.copy(alpha = 0.45f),
            disabledUncheckedBorderColor = colorScheme.outline.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun ColorScheme.animated(): ColorScheme {
    @Composable
    fun Color.toAnimatedColor(label: String): Color {
        val animatedColor by animateColorAsState(
            targetValue = this,
            animationSpec = AppThemeAnimationDefaults.ThemeColorAnimationSpec,
            label = label
        )
        return animatedColor
    }

    return copy(
        primary = primary.toAnimatedColor("primary"),
        onPrimary = onPrimary.toAnimatedColor("onPrimary"),
        primaryContainer = primaryContainer.toAnimatedColor("primaryContainer"),
        onPrimaryContainer = onPrimaryContainer.toAnimatedColor("onPrimaryContainer"),
        inversePrimary = inversePrimary.toAnimatedColor("inversePrimary"),
        secondary = secondary.toAnimatedColor("secondary"),
        onSecondary = onSecondary.toAnimatedColor("onSecondary"),
        secondaryContainer = secondaryContainer.toAnimatedColor("secondaryContainer"),
        onSecondaryContainer = onSecondaryContainer.toAnimatedColor("onSecondaryContainer"),
        tertiary = tertiary.toAnimatedColor("tertiary"),
        onTertiary = onTertiary.toAnimatedColor("onTertiary"),
        tertiaryContainer = tertiaryContainer.toAnimatedColor("tertiaryContainer"),
        onTertiaryContainer = onTertiaryContainer.toAnimatedColor("onTertiaryContainer"),
        background = background.toAnimatedColor("background"),
        onBackground = onBackground.toAnimatedColor("onBackground"),
        surface = surface.toAnimatedColor("surface"),
        onSurface = onSurface.toAnimatedColor("onSurface"),
        surfaceVariant = surfaceVariant.toAnimatedColor("surfaceVariant"),
        onSurfaceVariant = onSurfaceVariant.toAnimatedColor("onSurfaceVariant"),
        surfaceTint = surfaceTint.toAnimatedColor("surfaceTint"),
        inverseSurface = inverseSurface.toAnimatedColor("inverseSurface"),
        inverseOnSurface = inverseOnSurface.toAnimatedColor("inverseOnSurface"),
        error = error.toAnimatedColor("error"),
        onError = onError.toAnimatedColor("onError"),
        errorContainer = errorContainer.toAnimatedColor("errorContainer"),
        onErrorContainer = onErrorContainer.toAnimatedColor("onErrorContainer"),
        outline = outline.toAnimatedColor("outline"),
        outlineVariant = outlineVariant.toAnimatedColor("outlineVariant"),
        scrim = scrim.toAnimatedColor("scrim"),
        surfaceBright = surfaceBright.toAnimatedColor("surfaceBright"),
        surfaceDim = surfaceDim.toAnimatedColor("surfaceDim"),
        surfaceContainer = surfaceContainer.toAnimatedColor("surfaceContainer"),
        surfaceContainerHigh = surfaceContainerHigh.toAnimatedColor("surfaceContainerHigh"),
        surfaceContainerHighest = surfaceContainerHighest.toAnimatedColor("surfaceContainerHighest"),
        surfaceContainerLow = surfaceContainerLow.toAnimatedColor("surfaceContainerLow"),
        surfaceContainerLowest = surfaceContainerLowest.toAnimatedColor("surfaceContainerLowest")
    )
}