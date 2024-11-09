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

@file:Suppress("FunctionName")

package ru.tech.imageresizershrinker.feature.svg_maker.presentation.screenLogic

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.exifinterface.media.ExifInterface
import com.arkivanov.decompose.ComponentContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.image.ShareProvider
import ru.tech.imageresizershrinker.core.domain.image.model.ImageInfo
import ru.tech.imageresizershrinker.core.domain.saving.FileController
import ru.tech.imageresizershrinker.core.domain.saving.FilenameCreator
import ru.tech.imageresizershrinker.core.domain.saving.model.FileSaveTarget
import ru.tech.imageresizershrinker.core.domain.saving.model.ImageSaveTarget
import ru.tech.imageresizershrinker.core.domain.saving.model.SaveResult
import ru.tech.imageresizershrinker.core.domain.saving.model.SaveTarget
import ru.tech.imageresizershrinker.core.domain.saving.model.onSuccess
import ru.tech.imageresizershrinker.core.domain.utils.smartJob
import ru.tech.imageresizershrinker.core.ui.utils.BaseComponent
import ru.tech.imageresizershrinker.core.ui.utils.state.update
import ru.tech.imageresizershrinker.feature.svg_maker.domain.SvgManager
import ru.tech.imageresizershrinker.feature.svg_maker.domain.SvgParams

class SvgMakerComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val initialUris: List<Uri>?,
    private val svgManager: SvgManager,
    private val shareProvider: ShareProvider<Bitmap>,
    private val fileController: FileController,
    private val filenameCreator: FilenameCreator,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    init {
        debounce {
            initialUris?.let(::setUris)
        }
    }

    private val _uris = mutableStateOf<List<Uri>>(emptyList())
    val uris by _uris

    private val _isSaving: MutableState<Boolean> = mutableStateOf(false)
    val isSaving by _isSaving

    private val _done: MutableState<Int> = mutableIntStateOf(0)
    val done by _done

    private val _left: MutableState<Int> = mutableIntStateOf(-1)
    val left by _left

    private val _params = mutableStateOf(SvgParams.Default)
    val params by _params

    private var savingJob: Job? by smartJob {
        _isSaving.update { false }
    }

    fun save(
        oneTimeSaveLocationUri: String?,
        onResult: (List<SaveResult>) -> Unit
    ) {
        savingJob = componentScope.launch(defaultDispatcher) {
            val results = mutableListOf<SaveResult>()

            _isSaving.value = true
            _done.update { 0 }
            _left.value = uris.size

            svgManager.convertToSvg(
                imageUris = uris.map { it.toString() },
                params = params,
                onFailure = {
                    results.add(
                        SaveResult.Error.Exception(it)
                    )
                }
            ) { uri, svgBytes ->
                results.add(
                    fileController.save(
                        saveTarget = SvgSaveTarget(uri, svgBytes),
                        keepOriginalMetadata = true,
                        oneTimeSaveLocationUri = oneTimeSaveLocationUri
                    )
                )
                _done.update { it + 1 }
            }

            _isSaving.value = false
            onResult(results.onSuccess(::registerSave))
        }
    }

    fun performSharing(
        onFailure: (Throwable) -> Unit,
        onComplete: () -> Unit
    ) {
        savingJob = componentScope.launch {
            _done.update { 0 }
            _left.update { uris.size }

            _isSaving.value = true
            val results = mutableListOf<String?>()

            svgManager.convertToSvg(
                imageUris = uris.map { it.toString() },
                params = params,
                onFailure = onFailure
            ) { uri, jxlBytes ->
                results.add(
                    shareProvider.cacheByteArray(
                        byteArray = jxlBytes,
                        filename = filename(uri)
                    )
                )
                _done.update { it + 1 }
            }

            shareProvider.shareUris(results.filterNotNull())

            _isSaving.value = false
            onComplete()
        }
    }

    private fun filename(
        uri: String
    ): String = filenameCreator.constructImageFilename(
        ImageSaveTarget<ExifInterface>(
            imageInfo = ImageInfo(
                originalUri = uri
            ),
            originalUri = uri,
            sequenceNumber = done + 1,
            metadata = null,
            data = ByteArray(0),
            extension = "svg"
        ),
        forceNotAddSizeInFilename = true
    )

    private fun SvgSaveTarget(
        uri: String,
        svgBytes: ByteArray
    ): SaveTarget = FileSaveTarget(
        originalUri = uri,
        filename = filename(uri),
        data = svgBytes,
        mimeType = "image/svg+xml",
        extension = "svg"
    )

    fun cancelSaving() {
        savingJob?.cancel()
        savingJob = null
        _isSaving.value = false
    }

    fun setUris(newUris: List<Uri>) {
        _uris.update { newUris.distinct() }
    }

    fun removeUri(uri: Uri) {
        _uris.update { it - uri }
        registerChanges()
    }

    fun addUris(list: List<Uri>) {
        setUris(uris + list)
        registerChanges()
    }

    fun updateParams(newParams: SvgParams) {
        _params.update { newParams }
        registerChanges()
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialUris: List<Uri>?,
        ): SvgMakerComponent
    }

}