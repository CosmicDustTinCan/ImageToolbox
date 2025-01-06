/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package ru.tech.imageresizershrinker.feature.root.presentation.components.navigation

import com.arkivanov.decompose.ComponentContext
import ru.tech.imageresizershrinker.colllage_maker.presentation.screenLogic.CollageMakerComponent
import ru.tech.imageresizershrinker.color_tools.presentation.screenLogic.ColorToolsComponent
import ru.tech.imageresizershrinker.core.ui.utils.navigation.Screen
import ru.tech.imageresizershrinker.feature.ai_upscale.presentation.screenLogic.AiUpscaleComponent
import ru.tech.imageresizershrinker.feature.apng_tools.presentation.screenLogic.ApngToolsComponent
import ru.tech.imageresizershrinker.feature.base64_tools.presentation.screenLogic.Base64ToolsComponent
import ru.tech.imageresizershrinker.feature.checksum_tools.presentation.screenLogic.ChecksumToolsComponent
import ru.tech.imageresizershrinker.feature.cipher.presentation.screenLogic.CipherComponent
import ru.tech.imageresizershrinker.feature.compare.presentation.screenLogic.CompareComponent
import ru.tech.imageresizershrinker.feature.crop.presentation.screenLogic.CropComponent
import ru.tech.imageresizershrinker.feature.delete_exif.presentation.screenLogic.DeleteExifComponent
import ru.tech.imageresizershrinker.feature.document_scanner.presentation.screenLogic.DocumentScannerComponent
import ru.tech.imageresizershrinker.feature.draw.presentation.screenLogic.DrawComponent
import ru.tech.imageresizershrinker.feature.easter_egg.presentation.screenLogic.EasterEggComponent
import ru.tech.imageresizershrinker.feature.erase_background.presentation.screenLogic.EraseBackgroundComponent
import ru.tech.imageresizershrinker.feature.filters.presentation.screenLogic.FiltersComponent
import ru.tech.imageresizershrinker.feature.format_conversion.presentation.screenLogic.FormatConversionComponent
import ru.tech.imageresizershrinker.feature.generate_palette.presentation.screenLogic.GeneratePaletteComponent
import ru.tech.imageresizershrinker.feature.gif_tools.presentation.screenLogic.GifToolsComponent
import ru.tech.imageresizershrinker.feature.gradient_maker.presentation.screenLogic.GradientMakerComponent
import ru.tech.imageresizershrinker.feature.image_preview.presentation.screenLogic.ImagePreviewComponent
import ru.tech.imageresizershrinker.feature.image_stacking.presentation.screenLogic.ImageStackingComponent
import ru.tech.imageresizershrinker.feature.image_stitch.presentation.screenLogic.ImageStitchingComponent
import ru.tech.imageresizershrinker.feature.jxl_tools.presentation.screenLogic.JxlToolsComponent
import ru.tech.imageresizershrinker.feature.libraries_info.presentation.screenLogic.LibrariesInfoComponent
import ru.tech.imageresizershrinker.feature.limits_resize.presentation.screenLogic.LimitsResizeComponent
import ru.tech.imageresizershrinker.feature.load_net_image.presentation.screenLogic.LoadNetImageComponent
import ru.tech.imageresizershrinker.feature.main.presentation.screenLogic.MainComponent
import ru.tech.imageresizershrinker.feature.markup_layers.presentation.screenLogic.MarkupLayersComponent
import ru.tech.imageresizershrinker.feature.mesh_gradients.presentation.screenLogic.MeshGradientsComponent
import ru.tech.imageresizershrinker.feature.pdf_tools.presentation.screenLogic.PdfToolsComponent
import ru.tech.imageresizershrinker.feature.pick_color.presentation.screenLogic.PickColorFromImageComponent
import ru.tech.imageresizershrinker.feature.recognize.text.presentation.screenLogic.RecognizeTextComponent
import ru.tech.imageresizershrinker.feature.resize_convert.presentation.screenLogic.ResizeAndConvertComponent
import ru.tech.imageresizershrinker.feature.root.presentation.components.navigation.NavigationChild.*
import ru.tech.imageresizershrinker.feature.root.presentation.screenLogic.RootComponent
import ru.tech.imageresizershrinker.feature.scan_qr_code.presentation.screenLogic.ScanQrCodeComponent
import ru.tech.imageresizershrinker.feature.settings.presentation.screenLogic.SettingsComponent
import ru.tech.imageresizershrinker.feature.single_edit.presentation.screenLogic.SingleEditComponent
import ru.tech.imageresizershrinker.feature.svg_maker.presentation.screenLogic.SvgMakerComponent
import ru.tech.imageresizershrinker.feature.watermarking.presentation.screenLogic.WatermarkingComponent
import ru.tech.imageresizershrinker.feature.webp_tools.presentation.screenLogic.WebpToolsComponent
import ru.tech.imageresizershrinker.feature.weight_resize.presentation.screenLogic.WeightResizeComponent
import ru.tech.imageresizershrinker.feature.zip.presentation.screenLogic.ZipComponent
import ru.tech.imageresizershrinker.image_splitting.presentation.screenLogic.ImageSplitterComponent
import ru.tech.imageresizershrinker.noise_generation.presentation.screenLogic.NoiseGenerationComponent
import javax.inject.Inject

internal class ChildProvider @Inject constructor(
    private val apngToolsComponentFactory: ApngToolsComponent.Factory,
    private val cipherComponentFactory: CipherComponent.Factory,
    private val collageMakerComponentFactory: CollageMakerComponent.Factory,
    private val compareComponentFactory: CompareComponent.Factory,
    private val cropComponentFactory: CropComponent.Factory,
    private val deleteExifComponentFactory: DeleteExifComponent.Factory,
    private val documentScannerComponentFactory: DocumentScannerComponent.Factory,
    private val drawComponentFactory: DrawComponent.Factory,
    private val eraseBackgroundComponentFactory: EraseBackgroundComponent.Factory,
    private val filtersComponentFactory: FiltersComponent.Factory,
    private val formatConversionComponentFactory: FormatConversionComponent.Factory,
    private val generatePaletteComponentFactory: GeneratePaletteComponent.Factory,
    private val gifToolsComponentFactory: GifToolsComponent.Factory,
    private val gradientMakerComponentFactory: GradientMakerComponent.Factory,
    private val imagePreviewComponentFactory: ImagePreviewComponent.Factory,
    private val imageSplittingComponentFactory: ImageSplitterComponent.Factory,
    private val imageStackingComponentFactory: ImageStackingComponent.Factory,
    private val imageStitchingComponentFactory: ImageStitchingComponent.Factory,
    private val jxlToolsComponentFactory: JxlToolsComponent.Factory,
    private val limitResizeComponentFactory: LimitsResizeComponent.Factory,
    private val loadNetImageComponentFactory: LoadNetImageComponent.Factory,
    private val noiseGenerationComponentFactory: NoiseGenerationComponent.Factory,
    private val pdfToolsComponentFactory: PdfToolsComponent.Factory,
    private val pickColorFromImageComponentFactory: PickColorFromImageComponent.Factory,
    private val recognizeTextComponentFactory: RecognizeTextComponent.Factory,
    private val resizeAndConvertComponentFactory: ResizeAndConvertComponent.Factory,
    private val scanQrCodeComponentFactory: ScanQrCodeComponent.Factory,
    private val settingsComponentFactory: SettingsComponent.Factory,
    private val singleEditComponentFactory: SingleEditComponent.Factory,
    private val svgMakerComponentFactory: SvgMakerComponent.Factory,
    private val watermarkingComponentFactory: WatermarkingComponent.Factory,
    private val webpToolsComponentFactory: WebpToolsComponent.Factory,
    private val weightResizeComponentFactory: WeightResizeComponent.Factory,
    private val zipComponentFactory: ZipComponent.Factory,
    private val easterEggComponentFactory: EasterEggComponent.Factory,
    private val colorToolsComponentFactory: ColorToolsComponent.Factory,
    private val librariesInfoComponentFactory: LibrariesInfoComponent.Factory,
    private val mainComponentFactory: MainComponent.Factory,
    private val markupLayersComponentFactory: MarkupLayersComponent.Factory,
    private val base64ToolsComponentFactory: Base64ToolsComponent.Factory,
    private val checksumToolsComponentFactory: ChecksumToolsComponent.Factory,
    private val meshGradientsComponentFactory: MeshGradientsComponent.Factory,
    private val aiUpscaleComponentFactory: AiUpscaleComponent.Factory
) {
    fun RootComponent.createChild(
        config: Screen,
        componentContext: ComponentContext
    ): NavigationChild = when (config) {
        Screen.ColorTools -> ColorTools(
            colorToolsComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        Screen.EasterEgg -> EasterEgg(
            easterEggComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        Screen.Main -> Main(
            mainComponentFactory(
                componentContext = componentContext,
                onTryGetUpdate = ::tryGetUpdate,
                onGetClipList = ::updateUris,
                onNavigate = ::navigateToNew,
                isUpdateAvailable = isUpdateAvailable
            )
        )

        is Screen.ApngTools -> ApngTools(
            apngToolsComponentFactory(
                componentContext = componentContext,
                initialType = config.type,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.Cipher -> Cipher(
            cipherComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack
            )
        )

        is Screen.CollageMaker -> CollageMaker(
            collageMakerComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.Compare -> Compare(
            compareComponentFactory(
                componentContext = componentContext,
                initialComparableUris = config.uris
                    ?.takeIf { it.size == 2 }
                    ?.let { it[0] to it[1] },
                onGoBack = ::navigateBack
            )
        )

        is Screen.Crop -> Crop(
            cropComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.DeleteExif -> DeleteExif(
            deleteExifComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        Screen.DocumentScanner -> DocumentScanner(
            documentScannerComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.Draw -> Draw(
            drawComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.EraseBackground -> EraseBackground(
            eraseBackgroundComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.Filter -> Filter(
            filtersComponentFactory(
                componentContext = componentContext,
                initialType = config.type,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.FormatConversion -> FormatConversion(
            formatConversionComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.GeneratePalette -> GeneratePalette(
            generatePaletteComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack
            )
        )

        is Screen.GifTools -> GifTools(
            gifToolsComponentFactory(
                componentContext = componentContext,
                initialType = config.type,
                onGoBack = ::navigateBack
            )
        )

        is Screen.GradientMaker -> GradientMaker(
            gradientMakerComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.ImagePreview -> ImagePreview(
            imagePreviewComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.ImageSplitting -> ImageSplitting(
            imageSplittingComponentFactory(
                componentContext = componentContext,
                initialUris = config.uri,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.ImageStacking -> ImageStacking(
            imageStackingComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.ImageStitching -> ImageStitching(
            imageStitchingComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.JxlTools -> JxlTools(
            jxlToolsComponentFactory(
                componentContext = componentContext,
                initialType = config.type,
                onGoBack = ::navigateBack
            )
        )

        is Screen.LimitResize -> LimitResize(
            limitResizeComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.LoadNetImage -> LoadNetImage(
            loadNetImageComponentFactory(
                componentContext = componentContext,
                initialUrl = config.url,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )


        Screen.NoiseGeneration -> NoiseGeneration(
            noiseGenerationComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
            )
        )

        is Screen.PdfTools -> PdfTools(
            pdfToolsComponentFactory(
                componentContext = componentContext,
                initialType = config.type,
                onGoBack = ::navigateBack
            )
        )

        is Screen.PickColorFromImage -> PickColorFromImage(
            pickColorFromImageComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack
            )
        )

        is Screen.RecognizeText -> RecognizeText(
            recognizeTextComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack
            )
        )

        is Screen.ResizeAndConvert -> ResizeAndConvert(
            resizeAndConvertComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.ScanQrCode -> ScanQrCode(
            scanQrCodeComponentFactory(
                componentContext = componentContext,
                initialQrCodeContent = config.qrCodeContent,
                onGoBack = ::navigateBack
            )
        )

        Screen.Settings -> Settings(
            settingsComponentFactory(
                componentContext = componentContext,
                onTryGetUpdate = ::tryGetUpdate,
                onNavigate = ::navigateToNew,
                isUpdateAvailable = isUpdateAvailable,
                onGoBack = ::navigateBack
            )
        )

        is Screen.SingleEdit -> SingleEdit(
            singleEditComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onNavigate = ::navigateTo,
                onGoBack = ::navigateBack
            )
        )

        is Screen.SvgMaker -> SvgMaker(
            svgMakerComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack
            )
        )

        is Screen.Watermarking -> Watermarking(
            watermarkingComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.WebpTools -> WebpTools(
            webpToolsComponentFactory(
                componentContext = componentContext,
                initialType = config.type,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.WeightResize -> WeightResize(
            weightResizeComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.Zip -> Zip(
            zipComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack
            )
        )

        Screen.LibrariesInfo -> LibrariesInfo(
            librariesInfoComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        is Screen.MarkupLayers -> MarkupLayers(
            markupLayersComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.Base64Tools -> Base64Tools(
            base64ToolsComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.ChecksumTools -> ChecksumTools(
            checksumToolsComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack
            )
        )

        is Screen.MeshGradients -> MeshGradients(
            meshGradientsComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.AiUpscale -> AiUpscale(
            aiUpscaleComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
                initialUris = config.uris,
            )
        )
    }
}