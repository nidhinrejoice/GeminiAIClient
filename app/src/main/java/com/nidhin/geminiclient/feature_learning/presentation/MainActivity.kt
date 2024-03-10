package com.nidhin.geminiclient.feature_learning.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.nidhin.geminiclient.R
import com.nidhin.geminiclient.feature_learning.presentation.MainViewModel.UserAction.*
import com.nidhin.geminiclient.ui.theme.GeminiClientTheme
import com.nidhin.geminiclient.utils.showCustomToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var toast: Toast
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toast = Toast(this)
        lifecycleScope.launch {
            viewModel.eventFlow.collectLatest {
                toast.showCustomToast(this@MainActivity, it.toString())
            }
        }
        setContent {
            GeminiClientTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val lazyListState = rememberLazyListState()
                    if (drawerState.isOpen) {
                        viewModel.userAction(GetThreadHistory)
                    }
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet {
                                Text("Chat History", modifier = Modifier.padding(16.dp))
                                Divider()
                                viewModel.state.value.threadHistory.forEach {
                                    NavigationDrawerItem(
                                        label = {
                                            Text(
                                                text = it.aiResponse,
                                                maxLines = 1,
                                                style = MaterialTheme.typography.labelLarge,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        onClick = {
                                            viewModel.userAction(GetChatDetails(it.threadId))
                                            scope.launch {
                                                drawerState.close()
                                                lazyListState.animateScrollToItem(
                                                    0,
                                                    0
                                                )
                                            }
                                        },
                                        selected = false
                                    )
                                }
                            }
                        }
                    ) {
                        Column {
                            TopAppBar(
                                title = { Text(text = stringResource(R.string.app_name)) },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        scope.launch {
                                            drawerState.open()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Toggle drawer"
                                        )
                                    }
                                }
                            )
                            MultiChatScreen(viewModel, lazyListState = lazyListState,
                                onResponseLongPressed = {

                                    val clipboardManager =
                                        this@MainActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Copied Text", it)
                                    clipboardManager.setPrimaryClip(clip)
                                    toast.showCustomToast(
                                        this@MainActivity,
                                        "Content copied to clipboard"
                                    )
                                })
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun MultiChatScreen(
    viewModel: MainViewModel,
    lazyListState: LazyListState,
    onResponseLongPressed: (String) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)

    ) {

        val scope = rememberCoroutineScope()
        val (chatRef, promptRef, optionsRef) = createRefs()
        val text = remember { mutableStateOf("") }
        val keyboardController = LocalSoftwareKeyboardController.current
        Row(modifier = Modifier.constrainAs(optionsRef) {
            end.linkTo(parent.end)
            top.linkTo(parent.top)
        }) {

            TextButton(
                onClick = {
                    viewModel.userAction(NewChat)
                },
            ) {
                Icon(imageVector = Icons.Rounded.OpenInNew, contentDescription = "New Thread")
                Spacer(modifier = Modifier.size(4.dp))
                Text(text = "New Chat")
            }

            if (viewModel.state.value.chatHistory.isNotEmpty())
                ElevatedButton(onClick = {
                    viewModel.userAction(ClearChat)
                }) {
                    Text(text = "Reset Chat")
                }
        }
        Column(modifier = Modifier
            .fillMaxWidth()
            .constrainAs(promptRef) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }
        ) {
            OutlinedTextField(
                value = text.value,
                enabled = !viewModel.isAiResponseLoading.value,
                onValueChange = { text.value = it },
                label = { Text("Enter your query here") },
                placeholder = { Text("Enter your query here") },
                modifier = Modifier
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        viewModel.userAction(GenerateAiContent(text.value))
                        text.value = ""
                        scope.launch {
                            lazyListState.animateScrollToItem(
                                viewModel.state.value.chatHistory.size,
                                0
                            )
                        }
                    }
                )
            )
        }
        Column(
            modifier = Modifier
                .constrainAs(chatRef) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(optionsRef.bottom)
                    bottom.linkTo(promptRef.top)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
                .padding(4.dp)
        ) {
            Divider()

            LazyColumn(state = lazyListState) {
                items(viewModel.state.value.chatHistory) { chat ->
                    val aiResponse = chat.aiResponse
                    var contentAlignment: Alignment = Alignment.CenterEnd
                    val inComing = chat.role != "user"
                    if (inComing) {
                        contentAlignment = Alignment.CenterStart
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = if (inComing) 0.dp else 18.dp,
                                end = if (!inComing) 0.dp else 18.dp
                            ),
                        contentAlignment = contentAlignment
                    ) {
                        ElevatedCard(
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .padding(12.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            onResponseLongPressed(aiResponse)
                                        }
                                    )
                                },
                            elevation = CardDefaults.elevatedCardElevation(),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (inComing) MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            val textContent =
                                aiResponse.split("**").filter { it.isNotEmpty() }
                            Text(modifier = Modifier.padding(8.dp), text = buildAnnotatedString {
                                for (i in textContent.indices) {
                                    if (i % 2 == 0 && inComing) {
                                        withStyle(
                                            style = SpanStyle(
//                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        ) {
                                            append(textContent[i])
                                        }
                                    } else {
                                        withStyle(style = SpanStyle(fontSize = 13.sp)) {
                                            append(textContent[i])
                                        }

                                    }
                                }
                            })

                        }
                    }
                }
                item {
                    viewModel.state.value.promptInProgress?.let {
                        PromptInProgress(
                            visibility = viewModel.isAiResponseLoading.value,
                            prompt = it
                        )

                    }
                }
                if (viewModel.enableRetryPrompt.value.isNotEmpty()) {
                    item {
                        Column {
                            viewModel.state.value.promptInProgress?.let {
                                PromptInProgress(visibility = true, prompt = it)
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = viewModel.enableRetryPrompt.value,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                OutlinedButton(onClick = {
                                    viewModel.userAction(RetryPrompt)
                                }) {
                                    Text(text = "Retry")
                                }
                            }
                        }
                    }
                }
                if (viewModel.isAiResponseLoading.value) {
                    item {
                        Loader()
                    }
                }
            }
        }
    }

}

@Composable
fun PromptInProgress(visibility: Boolean, prompt: String) {
    val density = LocalDensity.current
    AnimatedVisibility(
        visible = visibility,
        enter = slideInHorizontally {
            with(density) { -40.dp.roundToPx() }
        } + expandVertically(
            expandFrom = Alignment.Top
        ) + fadeIn(
            initialAlpha = 0.3f
        )) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 0.dp,
                    end = 18.dp
                ),
            contentAlignment = Alignment.CenterEnd
        ) {
            OutlinedCard(
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(12.dp),
                elevation = CardDefaults.elevatedCardElevation(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = prompt,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun Loader() {
    val isPlaying by remember {
        mutableStateOf(true)
    }
    val speed by remember {
        mutableFloatStateOf(1f)
    }
//    var repeat by remember {
//        mutableStateOf(true)
//    }
    val composition by rememberLottieComposition(

        LottieCompositionSpec
            .RawRes(R.raw.loading)

    )

    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isPlaying,
        speed = speed,
        restartOnPlay = false

    )
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.size(40.dp))
        LottieAnimation(
            composition,
            progress,
            modifier = Modifier.size(80.dp)
        )

    }
}
