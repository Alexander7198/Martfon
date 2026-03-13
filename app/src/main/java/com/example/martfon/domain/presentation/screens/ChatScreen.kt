package com.example.martfon.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.martfon.data.network.dto.UserDto
import com.example.martfon.domain.model.Message
import com.example.martfon.presentation.viewmodels.ChatViewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    firebaseToken: String,
    otherUserId: String,
    onLogout: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    // Устанавливаем токен при загрузке экрана
    LaunchedEffect(firebaseToken) {
        viewModel.setAuthToken(firebaseToken)
    }

    // Устанавливаем chatId при загрузке экрана
    LaunchedEffect(chatId) {
        viewModel.setChatId(chatId)
    }

    val messages by viewModel.messages.collectAsState()
    val newMessageText by viewModel.newMessageText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val listState = rememberLazyListState()

    // ===== СТАТУС =====
    LaunchedEffect(Unit) {
        viewModel.startStatusUpdates()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopStatusUpdates()
        }
    }

    var otherUserStatus by remember { mutableStateOf<UserDto?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            otherUserStatus = viewModel.getOtherUserStatus(otherUserId)
            delay(10000)
        }
    }

    // Автоскролл
    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Чат")
                        otherUserStatus?.let { user ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (user.status == "online")
                                                Color.Green
                                            else
                                                Color.Gray
                                        )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (user.status == "online")
                                        "online"
                                    else {
                                        user.lastSeen?.let { formatLastSeen(it) } ?: "давно"
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Выйти"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Сообщения
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading && messages.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    error != null && messages.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Ошибка загрузки: $error",
                                    color = MaterialTheme.colorScheme.error
                                )
                                Button(
                                    onClick = { viewModel.refreshMessages() },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Повторить")
                                }
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            reverseLayout = true,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = messages,
                                key = { message -> message.id }
                            ) { message ->
                                MessageBubble(
                                    message = message,
                                    modifier = Modifier.animateItemPlacement()
                                )
                            }
                        }
                    }
                }
            }

            // Поле ввода
            MessageInput(
                newMessageText = newMessageText,
                onTextChange = { viewModel.updateMessageText(it) },
                onSendClick = { viewModel.sendMessage() },
                isEnabled = !isLoading && error == null && firebaseToken.isNotEmpty()
            )
        }
    }
}

// ⬇️ MessageBubble и MessageInput остаются без изменений ⬇️

@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    val isSentByMe = message.isSentByMe

    val bubbleColor = if (isSentByMe) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val alignment = if (isSentByMe) {
        Alignment.CenterEnd
    } else {
        Alignment.CenterStart
    }

    val timeText = remember(message.timestamp) {
        try {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
        } catch (_: Exception) {
            ""
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MessageInput(
    newMessageText: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isEnabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newMessageText,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Введите сообщение...") },
            singleLine = true,
            enabled = isEnabled,
            shape = MaterialTheme.shapes.large
        )

        Spacer(modifier = Modifier.width(8.dp))

        val canSend = newMessageText.trim().isNotEmpty() && isEnabled

        FloatingActionButton(
            onClick = { if (canSend) onSendClick() },
            modifier = Modifier.size(48.dp),
            containerColor = if (canSend) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            }
        ) {
            Text(
                text = "➤",
                color = if (canSend) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                }
            )
        }
    }
}

// ===== ВСПОМОГАТЕЛЬНАЯ ФУНКЦИЯ =====
fun formatLastSeen(timestamp: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = format.parse(timestamp)
        if (date == null) return "давно"

        val diff = System.currentTimeMillis() - date.time

        when {
            diff < 60000 -> "только что"
            diff < 3600000 -> "${diff / 60000} мин назад"
            diff < 86400000 -> "${diff / 3600000} ч назад"
            else -> {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                dateFormat.format(date)
            }
        }
    } catch (e: Exception) {
        "давно"
    }
}