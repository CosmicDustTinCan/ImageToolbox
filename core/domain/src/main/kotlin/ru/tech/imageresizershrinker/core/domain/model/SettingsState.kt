package ru.tech.imageresizershrinker.core.domain.model

import ru.tech.imageresizershrinker.core.domain.Domain
import ru.tech.imageresizershrinker.core.domain.ImageScaleMode

data class SettingsState(
    val nightMode: NightMode,
    val isDynamicColors: Boolean,
    val allowChangeColorByImage: Boolean,
    val emojisCount: Int,
    val isAmoledMode: Boolean,
    val appColorTuple: String,
    val borderWidth: Float,
    val presets: List<Preset>,
    val aspectRatios: List<DomainAspectRatio>,
    val fabAlignment: Int,
    val selectedEmoji: Int?,
    val imagePickerModeInt: Int,
    val clearCacheOnLaunch: Boolean,
    val showDialogOnStartup: Boolean,
    val groupOptionsByTypes: Boolean,
    val screenList: List<Int>,
    val colorTupleList: String?,
    val addSequenceNumber: Boolean,
    val saveFolderUri: String?,
    val filenamePrefix: String,
    val addSizeInFilename: Boolean,
    val addOriginalFilename: Boolean,
    val randomizeFilename: Boolean,
    val font: FontFam,
    val fontScale: Float?,
    val allowCollectCrashlytics: Boolean,
    val allowCollectAnalytics: Boolean,
    val allowBetas: Boolean,
    val drawContainerShadows: Boolean,
    val drawButtonShadows: Boolean,
    val drawSliderShadows: Boolean,
    val drawSwitchShadows: Boolean,
    val drawFabShadows: Boolean,
    val drawAppBarShadows: Boolean,
    val appOpenCount: Int,
    val lockDrawOrientation: Boolean,
    val themeContrastLevel: Double,
    val themeStyle: Int,
    val isInvertThemeColors: Boolean,
    val screensSearchEnabled: Boolean,
    val copyToClipboardMode: CopyToClipboardMode,
    val hapticsStrength: Int,
    val overwriteFiles: Boolean,
    val filenameSuffix: String,
    val defaultImageScaleMode: ImageScaleMode
) : Domain {

    companion object {
        val Default by lazy {
            SettingsState(
                nightMode = NightMode.System,
                isDynamicColors = true,
                allowChangeColorByImage = true,
                emojisCount = 1,
                isAmoledMode = false,
                appColorTuple = "",
                borderWidth = -1f,
                presets = emptyList(),
                fabAlignment = 1,
                selectedEmoji = 0,
                imagePickerModeInt = 0,
                clearCacheOnLaunch = true,
                showDialogOnStartup = true,
                groupOptionsByTypes = true,
                screenList = emptyList(),
                colorTupleList = null,
                addSequenceNumber = true,
                saveFolderUri = null,
                filenamePrefix = "ResizedImage",
                addSizeInFilename = true,
                addOriginalFilename = false,
                randomizeFilename = false,
                font = FontFam.Montserrat,
                fontScale = 1f,
                allowCollectCrashlytics = true,
                allowCollectAnalytics = true,
                allowBetas = true,
                drawContainerShadows = true,
                drawButtonShadows = true,
                drawSwitchShadows = true,
                drawSliderShadows = true,
                drawFabShadows = true,
                drawAppBarShadows = true,
                appOpenCount = 0,
                aspectRatios = DomainAspectRatio.defaultList,
                lockDrawOrientation = true,
                themeContrastLevel = 0.0,
                themeStyle = 0,
                isInvertThemeColors = false,
                screensSearchEnabled = false,
                hapticsStrength = 1,
                overwriteFiles = false,
                filenameSuffix = "",
                defaultImageScaleMode = ImageScaleMode.Default,
                copyToClipboardMode = CopyToClipboardMode.Disabled
            )
        }
    }
}