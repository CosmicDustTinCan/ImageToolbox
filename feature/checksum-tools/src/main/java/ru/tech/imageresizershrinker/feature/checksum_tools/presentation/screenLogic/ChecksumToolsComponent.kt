/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
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

package ru.tech.imageresizershrinker.feature.checksum_tools.presentation.screenLogic

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.model.ChecksumType
import ru.tech.imageresizershrinker.core.ui.utils.BaseComponent
import ru.tech.imageresizershrinker.core.ui.utils.state.update
import ru.tech.imageresizershrinker.feature.checksum_tools.domain.ChecksumManager
import ru.tech.imageresizershrinker.feature.checksum_tools.domain.ChecksumSource
import ru.tech.imageresizershrinker.feature.checksum_tools.presentation.components.ChecksumPage

class ChecksumToolsComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted initialUri: Uri?,
    @Assisted val onGoBack: () -> Unit,
    private val checksumManager: ChecksumManager,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _checksumType: MutableState<ChecksumType> =
        mutableStateOf(ChecksumType.entries.first())
    val checksumType: ChecksumType by _checksumType

    private val _calculateFromUriPage: MutableState<ChecksumPage.CalculateFromUri> = mutableStateOf(
        ChecksumPage.CalculateFromUri(
            uri = null,
            checksum = ""
        )
    )
    val calculateFromUriPage: ChecksumPage.CalculateFromUri by _calculateFromUriPage

    init {
        debounce {
            initialUri?.let(::setUri)
        }
    }

    fun setUri(uri: Uri) {
        componentScope.launch {
            _calculateFromUriPage.update {
                it.copy(
                    uri = uri,
                    checksum = checksumManager.calculateChecksum(
                        type = checksumType,
                        source = ChecksumSource.Uri(uri.toString())
                    )
                )
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialUri: Uri?,
            onGoBack: () -> Unit,
        ): ChecksumToolsComponent
    }

}