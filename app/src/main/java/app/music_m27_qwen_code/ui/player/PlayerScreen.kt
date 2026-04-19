package app.music_m27_qwen_code.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import app.music_m27_qwen_code.ui.components.formatDuration
import app.music_m27_qwen_code.ui.theme.*
import app.music_m27_qwen_code.viewmodel.PlayerMode
import app.music_m27_qwen_code.viewmodel.PlayerUiState
import app.music_m27_qwen_code.utils.LyricLine

@Composable
fun PlayerScreen(
    uiState: PlayerUiState,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val song = uiState.currentSong

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AsyncImage(
            model = song?.albumArtUri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(50.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                songTitle = song?.title ?: "",
                artistName = song?.artist ?: "",
                onBack = onBack,
                isFavorite = uiState.isFavorite,
                onToggleFavorite = onToggleFavorite
            )

            when (uiState.playerMode) {
                PlayerMode.COVER -> CoverMode(
                    uiState = uiState,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSeek = onSeek,
                    onToggleMode = onToggleMode
                )
                PlayerMode.LYRICS -> LyricsMode(
                    uiState = uiState,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSeek = onSeek,
                    onToggleMode = onToggleMode
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    songTitle: String,
    artistName: String,
    onBack: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                "返回",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = songTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = artistName,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "收藏",
                tint = if (isFavorite) AccentPink else Color.White
            )
        }
    }
}

@Composable
private fun CoverMode(
    uiState: PlayerUiState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleMode: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        AsyncImage(
            model = uiState.currentSong?.albumArtUri,
            contentDescription = "Album Art",
            modifier = Modifier
                .size(280.dp)
                .padding(24.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        if (uiState.lyrics.isNotEmpty()) {
            val currentLyric = uiState.lyrics.getOrNull(uiState.currentLyricIndex)
            val nextLyric = uiState.lyrics.getOrNull(uiState.currentLyricIndex + 1)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                currentLyric?.let {
                    Text(
                        text = it.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
                nextLyric?.let {
                    Text(
                        text = it.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.3f))

        ProgressSection(
            currentPosition = uiState.currentPosition,
            duration = uiState.duration,
            onSeek = onSeek
        )

        ControlSection(
            isPlaying = uiState.isPlaying,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onPrevious = onPrevious,
            onToggleMode = onToggleMode
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun LyricsMode(
    uiState: PlayerUiState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleMode: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.currentLyricIndex) {
        if (uiState.currentLyricIndex >= 0 && uiState.lyrics.isNotEmpty()) {
            listState.animateScrollToItem(
                index = maxOf(0, uiState.currentLyricIndex - 1)
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onToggleMode) {
                Icon(Icons.Default.Lyrics, "歌词", tint = PrimaryGreen)
                Spacer(modifier = Modifier.width(4.dp))
                Text("词", color = PrimaryGreen)
            }
        }

        ProgressSection(
            currentPosition = uiState.currentPosition,
            duration = uiState.duration,
            onSeek = onSeek
        )

        if (uiState.lyrics.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无歌词",
                    style = MaterialTheme.typography.bodyLarge,
                    color = LyricInactive
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 100.dp)
            ) {
                itemsIndexed(uiState.lyrics) { index, lyricLine ->
                    LyricLineItem(
                        lyricLine = lyricLine,
                        isCurrentLine = index == uiState.currentLyricIndex,
                        onClick = {
                            onSeek(lyricLine.time)
                        }
                    )
                }
            }
        }

        ControlSection(
            isPlaying = uiState.isPlaying,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onPrevious = onPrevious,
            onToggleMode = onToggleMode,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

@Composable
private fun LyricLineItem(
    lyricLine: LyricLine,
    isCurrentLine: Boolean,
    onClick: () -> Unit
) {
    val animatedSize by animateFloatAsState(
        targetValue = if (isCurrentLine) 1.2f else 1f,
        label = "lyric_size"
    )

    Text(
        text = lyricLine.text,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = (18 * animatedSize).sp,
            fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal
        ),
        color = if (isCurrentLine) Color.White else LyricInactive,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { onClick() }
    )
}

@Composable
private fun ProgressSection(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Slider(
            value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
            onValueChange = { value ->
                onSeek((value * duration).toLong())
            },
            colors = SliderDefaults.colors(
                thumbColor = PrimaryGreen,
                activeTrackColor = PrimaryGreen,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ControlSection(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious, modifier = Modifier.size(56.dp)) {
            Icon(
                Icons.Default.SkipPrevious,
                "上一首",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        FloatingActionButton(
            onClick = onPlayPause,
            containerColor = PrimaryGreen,
            modifier = Modifier.size(72.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "暂停" else "播放",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        IconButton(onClick = onNext, modifier = Modifier.size(56.dp)) {
            Icon(
                Icons.Default.SkipNext,
                "下一首",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
