package ru.tech.imageresizershrinker.resize_screen

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DoorBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.twotone.BrokenImage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.smarttoolfactory.cropper.ImageCropper
import com.smarttoolfactory.cropper.model.OutlineType
import com.smarttoolfactory.cropper.model.RectCropShape
import com.smarttoolfactory.cropper.settings.CropDefaults
import com.smarttoolfactory.cropper.settings.CropOutlineProperty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.ImageResizerShrinkerTheme
import ru.tech.imageresizershrinker.R
import ru.tech.imageresizershrinker.crash_screen.CrashActivity
import ru.tech.imageresizershrinker.crash_screen.GlobalExceptionHandler
import ru.tech.imageresizershrinker.resize_screen.components.*
import ru.tech.imageresizershrinker.resize_screen.viewModel.MainViewModel
import ru.tech.imageresizershrinker.theme.Github
import ru.tech.imageresizershrinker.theme.Telegram
import ru.tech.imageresizershrinker.utils.BitmapUtils
import ru.tech.imageresizershrinker.utils.BitmapUtils.decodeBitmapFromUri
import ru.tech.imageresizershrinker.utils.BitmapUtils.getUriByName
import ru.tech.imageresizershrinker.utils.BitmapUtils.toMap
import java.io.File

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    @OptIn(ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        GlobalExceptionHandler.initialize(applicationContext, CrashActivity::class.java)

        parseImageFromIntent(intent)

        setContent {
            ImageResizerShrinkerTheme {
                val focus = LocalFocusManager.current
                val toastHostState = rememberToastHostState()
                val scope = rememberCoroutineScope()
                var showExitDialog by rememberSaveable { mutableStateOf(false) }
                var showResetDialog by rememberSaveable { mutableStateOf(false) }
                var showSaveLoading by rememberSaveable { mutableStateOf(false) }
                var showOriginal by rememberSaveable { mutableStateOf(false) }
                var showEditExifDialog by rememberSaveable { mutableStateOf(false) }
                var showExifSavingDialog by rememberSaveable { mutableStateOf(false) }
                var showCropDialog by rememberSaveable { mutableStateOf(false) }

                val state = rememberLazyListState()

                val bitmapInfo = viewModel.bitmapInfo
                var map by remember(viewModel.exif) {
                    mutableStateOf(viewModel.exif?.toMap())
                }

                val pickImageLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickVisualMedia()
                    ) { uri ->
                        uri?.let {
                            try {
                                decodeBitmapFromUri(
                                    uri = it,
                                    onGetMimeType = viewModel::setMime,
                                    onGetExif = viewModel::updateExif,
                                    onGetBitmap = viewModel::updateBitmap,
                                )
                            } catch (e: Exception) {
                                scope.launch {
                                    toastHostState.showToast(
                                        getString(
                                            R.string.smth_went_wrong,
                                            e.localizedMessage ?: ""
                                        ),
                                        Icons.Rounded.ErrorOutline
                                    )
                                }
                            }
                        }
                    }

                val pickImage = {
                    pickImageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }

                val saveBitmap = {
                    showSaveLoading = true
                    viewModel.saveBitmap(
                        isExternalStorageWritable = isExternalStorageWritable(),
                        getFileOutputStream = { name, ext ->
                            val contentValues = ContentValues().apply {
                                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                                put(
                                    MediaStore.MediaColumns.MIME_TYPE,
                                    "image/$ext"
                                )
                                put(
                                    MediaStore.MediaColumns.RELATIVE_PATH,
                                    "DCIM/ResizedImages"
                                )
                            }
                            val imageUri = contentResolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                contentValues
                            )
                            contentResolver.openOutputStream(imageUri!!)
                        },
                        getExternalStorageDir = {
                            File(
                                Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DCIM
                                ), "ResizedImages"
                            )
                        },
                        getFileDescriptor = { name ->
                            getUriByName(name)?.let {
                                contentResolver.openFileDescriptor(it, "rw", null)
                            }
                        }
                    ) { success ->
                        if (!success) requestPermission()
                        else {
                            scope.launch {
                                toastHostState.showToast(
                                    getString(R.string.saved_to),
                                    Icons.Rounded.Save
                                )
                            }
                        }
                        showSaveLoading = false
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    focus.clearFocus()
                                }
                            )
                        }
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxSize()) {
                            CenterAlignedTopAppBar(
                                title = {
                                    AnimatedContent(
                                        targetState = viewModel.bitmap to viewModel.isLoading,
                                        transitionSpec = { fadeIn() with fadeOut() }
                                    ) { (bmp, loading) ->
                                        if (bmp == null) {
                                            Text(stringResource(R.string.app_name))
                                        } else if (!loading) {
                                            Text(
                                                stringResource(
                                                    R.string.size,
                                                    byteCount(bitmapInfo.size)
                                                )
                                            )
                                        } else {
                                            Text(stringResource(R.string.loading))
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                        3.dp
                                    )
                                ),
                                actions = {
                                    val interactionSource = remember { MutableInteractionSource() }
                                    IconButton(
                                        enabled = viewModel.bitmap != null,
                                        onClick = { showResetDialog = true }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.RestartAlt,
                                            contentDescription = null
                                        )
                                    }
                                    if (viewModel.bitmap != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .indication(
                                                    interactionSource,
                                                    LocalIndication.current
                                                )
                                                .pointerInput(Unit) {
                                                    detectTapGestures(
                                                        onPress = {
                                                            val press = PressInteraction.Press(it)
                                                            interactionSource.emit(press)
                                                            val pos =
                                                                state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset
                                                            showOriginal = true
                                                            delay(100)
                                                            state.animateScrollToItem(
                                                                0,
                                                                10000
                                                            )
                                                            tryAwaitRelease()
                                                            showOriginal = false
                                                            interactionSource.emit(
                                                                PressInteraction.Release(
                                                                    press
                                                                )
                                                            )
                                                            state.animateScrollToItem(
                                                                pos.first,
                                                                pos.second
                                                            )
                                                        }
                                                    )
                                                }
                                        ) {
                                            Icon(
                                                Icons.Rounded.History,
                                                null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .padding(8.dp)
                                            )
                                        }
                                    } else {
                                        IconButton(
                                            enabled = false,
                                            onClick = {}
                                        ) { Icon(Icons.Rounded.History, null) }
                                    }
                                },
                                navigationIcon = {
                                    FilledIconButton(
                                        enabled = viewModel.bitmap != null,
                                        onClick = { viewModel.setTelegramSpecs() },
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = animateColorAsState(
                                                if (viewModel.isTelegramSpecs) MaterialTheme.colorScheme.primaryContainer
                                                else Color.Transparent
                                            ).value,
                                            contentColor = animateColorAsState(
                                                if (viewModel.isTelegramSpecs) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurface
                                            ).value,
                                            disabledContainerColor = Color.Transparent
                                        )
                                    ) {
                                        Icon(Icons.Rounded.Telegram, null)
                                    }
                                }
                            )
                            LazyColumn(
                                state = state,
                                reverseLayout = true,
                                contentPadding = PaddingValues(
                                    bottom = WindowInsets.navigationBars.asPaddingValues()
                                        .calculateBottomPadding() + 120.dp,
                                    top = 48.dp,
                                    start = 20.dp,
                                    end = 20.dp
                                )
                            ) {
                                item {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        AnimatedContent(
                                            targetState = viewModel.previewBitmap to viewModel.isLoading,
                                            transitionSpec = { fadeIn() with fadeOut() }
                                        ) { pair ->
                                            val bmp = pair.first
                                            val loading = pair.second
                                            Box {
                                                AnimatedContent(
                                                    targetState = showOriginal,
                                                    transitionSpec = {
                                                        fadeIn() with fadeOut()
                                                    },
                                                    modifier = Modifier.align(Alignment.Center)
                                                ) { showOrig ->
                                                    if (showOrig) {
                                                        viewModel.bitmap?.asImageBitmap()
                                                            ?.let {
                                                                Image(
                                                                    bitmap = it,
                                                                    contentDescription = null,
                                                                    modifier = Modifier.clip(
                                                                        RoundedCornerShape(4.dp)
                                                                    )
                                                                )
                                                            }
                                                    } else {
                                                        bmp?.asImageBitmap()
                                                            ?.takeIf { viewModel.shouldShowPreview }
                                                            ?.let {
                                                                Image(
                                                                    bitmap = it,
                                                                    contentDescription = null,
                                                                    modifier = Modifier.clip(
                                                                        RoundedCornerShape(4.dp)
                                                                    )
                                                                )
                                                            }
                                                        if (!viewModel.shouldShowPreview && !loading && bmp != null) Box {
                                                            Column(
                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                modifier = Modifier.padding(8.dp)
                                                            ) {
                                                                Text(
                                                                    stringResource(R.string.image_too_large_preview),
                                                                    textAlign = TextAlign.Center
                                                                )
                                                                Spacer(Modifier.height(8.dp))
                                                                Icon(
                                                                    Icons.TwoTone.BrokenImage,
                                                                    null,
                                                                    modifier = Modifier.size(100.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                if (loading) Loading()
                                            }
                                        }
                                        if (viewModel.previewBitmap != null) {
                                            Spacer(Modifier.size(20.dp))
                                            ImageTransformBar(
                                                onEditExif = { showEditExifDialog = true },
                                                onCrop = { showCropDialog = true },
                                                onRotateLeft = viewModel::rotateLeft,
                                                onFlip = viewModel::flip,
                                                onRotateRight = viewModel::rotateRight
                                            )
                                            Spacer(Modifier.size(8.dp))
                                            PresetWidget(
                                                selectedPreset = viewModel.presetSelected,
                                                bitmap = viewModel.bitmap,
                                                bitmapInfo = bitmapInfo,
                                                onChangeBitmapInfo = viewModel::setBitmapInfo
                                            )
                                        } else if (!viewModel.isLoading) {
                                            ImageNotPickedWidget(onPickImage = pickImage)
                                            Spacer(Modifier.size(8.dp))
                                        }
                                        Spacer(Modifier.size(8.dp))
                                        ResizeImageField(
                                            bitmapInfo = bitmapInfo,
                                            bitmap = viewModel.bitmap,
                                            onHeightChange = viewModel::updateHeight,
                                            onWidthChange = viewModel::updateWidth
                                        )
                                        ProvideTextStyle(
                                            value = TextStyle(
                                                color = if (viewModel.bitmap == null) {
                                                    MaterialTheme.colorScheme.onSurface
                                                        .copy(alpha = 0.38f)
                                                        .compositeOver(MaterialTheme.colorScheme.surface)
                                                } else Color.Unspecified
                                            )
                                        ) {
                                            Column {
                                                if (bitmapInfo.mime != 2) Spacer(Modifier.height(8.dp))
                                                QualityWidget(
                                                    visible = bitmapInfo.mime != 2,
                                                    enabled = viewModel.bitmap != null,
                                                    quality = bitmapInfo.quality,
                                                    onQualityChange = viewModel::setQuality
                                                )
                                                Spacer(Modifier.height(8.dp))
                                                ToggleGroupButton(
                                                    modifier = Modifier
                                                        .block()
                                                        .padding(horizontal = 2.dp),
                                                    title = stringResource(R.string.extension),
                                                    enabled = viewModel.bitmap != null,
                                                    items = listOf("JPEG", "WEBP", "PNG"),
                                                    selectedIndex = bitmapInfo.mime,
                                                    indexChanged = {
                                                        viewModel.setMime(it)
                                                    }
                                                )
                                                Spacer(Modifier.height(8.dp))
                                                ToggleGroupButton(
                                                    modifier = Modifier
                                                        .block()
                                                        .padding(horizontal = 2.dp),
                                                    enabled = viewModel.bitmap != null,
                                                    title = stringResource(R.string.resize_type),
                                                    items = listOf(
                                                        stringResource(R.string.explicit),
                                                        stringResource(R.string.flexible),
                                                        stringResource(R.string.ratio)
                                                    ),
                                                    selectedIndex = bitmapInfo.resizeType,
                                                    indexChanged = {
                                                        viewModel.setResizeType(it)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.imePadding())
                                }
                            }
                        }

                        FlowRow(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .navigationBarsPadding()
                        ) {
                            if (viewModel.bitmap != null) {
                                FloatingActionButton(
                                    onClick = {
                                        if (bitmapInfo.mime != 0 && viewModel.bitmap != null && map?.isNotEmpty() == true) {
                                            showExifSavingDialog = true
                                        } else {
                                            saveBitmap()
                                        }
                                    },
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    BadgedBox(
                                        badge = {
                                            androidx.compose.animation.AnimatedVisibility(
                                                visible = bitmapInfo.mime != 0 && map?.isNotEmpty() == true,
                                                enter = fadeIn() + scaleIn(),
                                                exit = fadeOut() + scaleOut()
                                            ) {
                                                Badge(modifier = Modifier.size(8.dp))
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Rounded.Save, null)
                                    }
                                }
                            } else {
                                FloatingActionButton(
                                    onClick = {
                                        startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://github.com/T8RIN/ImageResizer")
                                            )
                                        )
                                    },
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Icon(Icons.Rounded.Github, null)
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            FloatingActionButton(onClick = pickImage) {
                                val expanded = state.isScrollingUp()
                                val horizontalPadding by animateDpAsState(targetValue = if (expanded) 16.dp else 0.dp)
                                Row(
                                    modifier = Modifier.padding(horizontal = horizontalPadding),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.AddPhotoAlternate, null)
                                    AnimatedVisibility(visible = expanded) {
                                        Row {
                                            Spacer(Modifier.width(8.dp))
                                            Text(stringResource(R.string.pick_image_alt))
                                        }
                                    }
                                }
                            }
                        }

                        if (showExitDialog) {
                            AlertDialog(
                                onDismissRequest = { showExitDialog = false },
                                dismissButton = {
                                    FilledTonalButton(
                                        onClick = {
                                            finishAffinity()
                                        }
                                    ) {
                                        Text(stringResource(R.string.close))
                                    }
                                },
                                confirmButton = {
                                    Button(onClick = { showExitDialog = false }) {
                                        Text(stringResource(R.string.stay))
                                    }
                                },
                                title = { Text(stringResource(R.string.app_closing)) },
                                text = {
                                    Text(
                                        stringResource(R.string.app_closing_sub),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                icon = { Icon(Icons.Outlined.DoorBack, null) }
                            )
                        } else if (showSaveLoading) {
                            LoadingDialog()
                        } else if (showResetDialog) {
                            AlertDialog(
                                icon = { Icon(Icons.Rounded.RestartAlt, null) },
                                title = { Text(stringResource(R.string.reset_image)) },
                                text = { Text(stringResource(R.string.reset_image_sub)) },
                                onDismissRequest = { showResetDialog = false },
                                confirmButton = {
                                    Button(onClick = { showResetDialog = false }) {
                                        Text(stringResource(R.string.close))
                                    }
                                },
                                dismissButton = {
                                    FilledTonalButton(onClick = {
                                        viewModel.resetValues()
                                        showResetDialog = false
                                        scope.launch {
                                            toastHostState.showToast(
                                                getString(R.string.values_reset),
                                                Icons.Rounded.DoneOutline
                                            )
                                        }
                                    }) {
                                        Text(stringResource(R.string.reset))
                                    }
                                }
                            )
                        } else if (showEditExifDialog) {
                            var showAddExifDialog by rememberSaveable { mutableStateOf(false) }
                            var showClearExifDialog by rememberSaveable { mutableStateOf(false) }
                            AlertDialog(
                                modifier = Modifier
                                    .systemBarsPadding()
                                    .animateContentSize()
                                    .widthIn(max = 640.dp)
                                    .padding(16.dp),
                                onDismissRequest = { showEditExifDialog = false },
                                title = { Text(stringResource(R.string.edit_exif)) },
                                icon = { Icon(Icons.Rounded.Dataset, null) },
                                confirmButton = {
                                    Button(
                                        onClick = { showEditExifDialog = false }
                                    ) {
                                        Text(stringResource(R.string.ok))
                                    }
                                },
                                dismissButton = {
                                    val count =
                                        remember(map) {
                                            BitmapUtils.tags.count {
                                                it !in (map?.keys ?: emptyList())
                                            }
                                        }
                                    Row {
                                        if (map?.isEmpty() == false) {
                                            OutlinedButton(onClick = {
                                                showClearExifDialog = true
                                            }) {
                                                Text(stringResource(R.string.clear))
                                            }
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        if (count > 0) {
                                            FilledTonalButton(
                                                onClick = { showAddExifDialog = true }
                                            ) {
                                                Text(stringResource(R.string.add_tag))
                                            }
                                        }
                                    }
                                },
                                properties = DialogProperties(usePlatformDefaultWidth = false),
                                text = {
                                    if (map?.isEmpty() == false) {
                                        Box {
                                            Divider(Modifier.align(Alignment.TopCenter))
                                            Column(Modifier.verticalScroll(rememberScrollState())) {
                                                Spacer(Modifier.height(8.dp))
                                                map?.forEach { (tag, value) ->
                                                    Card(
                                                        modifier = Modifier
                                                            .padding(vertical = 4.dp),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                                                alpha = 0.5f
                                                            )
                                                        )
                                                    ) {
                                                        Column(Modifier.fillMaxWidth()) {
                                                            Row {
                                                                Text(
                                                                    text = tag,
                                                                    fontSize = 16.sp,
                                                                    modifier = Modifier
                                                                        .padding(12.dp)
                                                                        .weight(1f)
                                                                )
                                                                IconButton(
                                                                    onClick = {
                                                                        viewModel.removeExifTag(
                                                                            tag
                                                                        )
                                                                        map =
                                                                            map?.toMutableMap()
                                                                                ?.apply {
                                                                                    remove(tag)
                                                                                }
                                                                    }
                                                                ) {
                                                                    Icon(
                                                                        Icons.Rounded.RemoveCircleOutline,
                                                                        null
                                                                    )
                                                                }
                                                            }
                                                            OutlinedTextField(
                                                                onValueChange = {
                                                                    viewModel.updateExifByTag(
                                                                        tag,
                                                                        it
                                                                    )
                                                                    map =
                                                                        map?.toMutableMap()
                                                                            ?.apply {
                                                                                this[tag] = it
                                                                            }
                                                                },
                                                                value = value,
                                                                textStyle = LocalTextStyle.current.copy(
                                                                    fontSize = 16.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                ),
                                                                keyboardOptions = KeyboardOptions.Default.copy(
                                                                    imeAction = ImeAction.Next
                                                                ),
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(8.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(Modifier.height(8.dp))
                                            }
                                            Divider(Modifier.align(Alignment.BottomCenter))
                                        }
                                    } else {
                                        Text(
                                            stringResource(R.string.no_exif),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            )

                            if (showAddExifDialog) {
                                AlertDialog(
                                    modifier = Modifier
                                        .systemBarsPadding()
                                        .animateContentSize()
                                        .widthIn(max = 640.dp)
                                        .padding(16.dp),
                                    onDismissRequest = { showAddExifDialog = false },
                                    title = { Text(stringResource(R.string.add_tag)) },
                                    icon = { Icon(Icons.Rounded.Dataset, null) },
                                    confirmButton = {
                                        Button(
                                            onClick = { showAddExifDialog = false }
                                        ) {
                                            Text(stringResource(R.string.ok))
                                        }
                                    },
                                    properties = DialogProperties(usePlatformDefaultWidth = false),
                                    text = {
                                        Box {
                                            Divider(Modifier.align(Alignment.TopCenter))
                                            Column(
                                                Modifier.verticalScroll(rememberScrollState())
                                            ) {
                                                val tags =
                                                    remember(map) {
                                                        BitmapUtils.tags.filter {
                                                            it !in (map?.keys ?: emptyList())
                                                        }.sorted()
                                                    }
                                                if (tags.isEmpty()) {
                                                    SideEffect {
                                                        showAddExifDialog = false
                                                    }
                                                }
                                                Spacer(Modifier.height(8.dp))
                                                tags.forEach { tag ->
                                                    Card(
                                                        modifier = Modifier
                                                            .padding(vertical = 4.dp),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                                                alpha = 0.5f
                                                            )
                                                        )
                                                    ) {
                                                        Column(Modifier.fillMaxWidth()) {
                                                            Row {
                                                                Text(
                                                                    text = tag,
                                                                    fontSize = 16.sp,
                                                                    modifier = Modifier
                                                                        .padding(12.dp)
                                                                        .weight(1f)
                                                                )
                                                                IconButton(
                                                                    onClick = {
                                                                        viewModel.removeExifTag(
                                                                            tag
                                                                        )
                                                                        map =
                                                                            map?.toMutableMap()
                                                                                ?.apply {
                                                                                    this[tag] =
                                                                                        ""
                                                                                }
                                                                    }
                                                                ) {
                                                                    Icon(
                                                                        Icons.Rounded.AddCircle,
                                                                        null
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                Spacer(Modifier.height(8.dp))
                                            }
                                            Divider(Modifier.align(Alignment.BottomCenter))
                                        }
                                    }
                                )
                            } else if (showClearExifDialog) {
                                AlertDialog(
                                    onDismissRequest = { showClearExifDialog = false },
                                    title = { Text(stringResource(R.string.clear_exif)) },
                                    icon = { Icon(Icons.Rounded.Delete, null) },
                                    confirmButton = {
                                        Button(
                                            onClick = { showClearExifDialog = false }
                                        ) {
                                            Text(stringResource(R.string.cancel))
                                        }
                                    },
                                    dismissButton = {
                                        FilledTonalButton(
                                            onClick = {
                                                showClearExifDialog = false
                                                map = emptyMap()
                                                viewModel.clearExif()
                                            }
                                        ) {
                                            Text(stringResource(R.string.clear))
                                        }
                                    },
                                    text = {
                                        Text(stringResource(R.string.clear_exif_sub))
                                    }
                                )
                            }
                        } else if (showExifSavingDialog) {
                            AlertDialog(
                                icon = { Icon(Icons.Rounded.Save, null) },
                                title = { Text(stringResource(R.string.exif)) },
                                text = { Text(stringResource(R.string.might_be_error_with_exif)) },
                                onDismissRequest = { showExifSavingDialog = false },
                                confirmButton = {
                                    Button(onClick = { showExifSavingDialog = false }) {
                                        Text(stringResource(R.string.close))
                                    }
                                },
                                dismissButton = {
                                    FilledTonalButton(
                                        onClick = {
                                            showExifSavingDialog = false
                                            saveBitmap()
                                        }
                                    ) {
                                        Text(stringResource(R.string.save))
                                    }
                                }
                            )
                        } else if (viewModel.bitmap != null && showCropDialog) {
                            viewModel.bitmap?.let {
                                val bmp = remember(it) { it.asImageBitmap() }
                                var crop by remember { mutableStateOf(false) }
                                AlertDialog(
                                    modifier = Modifier
                                        .systemBarsPadding()
                                        .animateContentSize()
                                        .widthIn(max = 640.dp)
                                        .padding(16.dp),
                                    properties = DialogProperties(usePlatformDefaultWidth = false),
                                    onDismissRequest = { showCropDialog = false },
                                    text = {
                                        ImageCropper(
                                            imageBitmap = bmp,
                                            contentDescription = null,
                                            cropProperties = CropDefaults.properties(
                                                cropOutlineProperty = CropOutlineProperty(
                                                    OutlineType.Rect,
                                                    RectCropShape(0, "")
                                                )
                                            ),
                                            onCropStart = {},
                                            crop = crop,
                                            onCropSuccess = { image ->
                                                viewModel.updateBitmap(image.asAndroidBitmap())
                                                showCropDialog = false
                                            }
                                        )
                                    },
                                    confirmButton = {
                                        Button(onClick = { crop = true }) {
                                            Text(stringResource(R.string.crop))
                                        }
                                    },
                                    dismissButton = {
                                        FilledTonalButton(onClick = { showCropDialog = false }) {
                                            Text(stringResource(R.string.cancel))
                                        }
                                    }
                                )
                            }
                        }

                        ToastHost(hostState = toastHostState)

                        BackHandler { showExitDialog = true }
                    }
                }
            }
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1
        )
    }

    private fun isExternalStorageWritable(): Boolean {
        return if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) true
        else ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        parseImageFromIntent(intent)
    }

    private fun parseImageFromIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("image/") == true) {
                    intent.parcelable<Uri>(Intent.EXTRA_STREAM)?.let {
                        kotlin.runCatching {
                            decodeBitmapFromUri(
                                uri = it,
                                onGetMimeType = viewModel::setMime,
                                onGetExif = viewModel::updateExif,
                                onGetBitmap = viewModel::updateBitmap,
                            )
                        }
                    }
                }
            }
        }
    }
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

fun Modifier.block(shape: Shape = RoundedCornerShape(16.dp)) = composed {
    background(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = shape
    ).padding(4.dp)
}