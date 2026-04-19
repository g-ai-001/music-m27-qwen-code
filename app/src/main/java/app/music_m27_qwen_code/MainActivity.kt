package app.music_m27_qwen_code

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.music_m27_qwen_code.ui.components.MiniPlayer
import app.music_m27_qwen_code.ui.home.HomeScreen
import app.music_m27_qwen_code.ui.me.MeScreen
import app.music_m27_qwen_code.ui.player.PlayerScreen
import app.music_m27_qwen_code.ui.theme.DarkBackground
import app.music_m27_qwen_code.ui.theme.MusicAppTheme
import app.music_m27_qwen_code.ui.theme.PrimaryGreen
import app.music_m27_qwen_code.ui.theme.TextPrimary
import app.music_m27_qwen_code.viewmodel.LibraryViewModel
import app.music_m27_qwen_code.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // Permission granted, can scan music
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()

        setContent {
            MusicAppTheme {
                MusicApp()
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicApp() {
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val context = androidx.compose.ui.platform.LocalContext.current

    val libraryState by libraryViewModel.uiState.collectAsState()
    val playerState by playerViewModel.uiState.collectAsState()

    var showPlayer by remember { mutableStateOf(false) }
    var currentTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        libraryViewModel.initialize(context)
        playerViewModel.initialize(context)
        playerViewModel.startPositionUpdates()
        libraryViewModel.scanMusic()
    }

    if (showPlayer) {
        PlayerScreen(
            uiState = playerState,
            onBack = { showPlayer = false },
            onPlayPause = playerViewModel::togglePlayPause,
            onNext = playerViewModel::next,
            onPrevious = playerViewModel::previous,
            onSeek = playerViewModel::seekTo,
            onToggleFavorite = playerViewModel::toggleFavorite,
            onToggleMode = playerViewModel::togglePlayerMode
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = DarkBackground
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, "首页") },
                        label = { Text("首页") },
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = PrimaryGreen.copy(alpha = 0.1f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, "我的") },
                        label = { Text("我的") },
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = PrimaryGreen.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
                when (currentTab) {
                    0 -> HomeScreen(
                        uiState = libraryState,
                        onScanMusic = libraryViewModel::scanMusic,
                        onSearch = libraryViewModel::search,
                        onTabSelected = libraryViewModel::selectTab,
                        onSongClick = { song, index ->
                            playerViewModel.setPlaylist(libraryState.allSongs, index)
                            playerViewModel.play()
                        },
                        onOpenPlayer = { showPlayer = true },
                        onOpenPlaylist = { },
                        onCreatePlaylist = libraryViewModel::createPlaylist,
                        modifier = Modifier.weight(1f)
                    )
                    1 -> MeScreen(
                        favorites = libraryState.favorites,
                        recentSongs = libraryState.recentSongs,
                        playlists = libraryState.playlists,
                        onPlaylistClick = { },
                        onSongClick = { song, index ->
                            playerViewModel.setPlaylist(libraryState.favorites, index)
                            playerViewModel.play()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (playerState.currentSong != null) {
                    MiniPlayer(
                        song = playerState.currentSong,
                        isPlaying = playerState.isPlaying,
                        onPlayPause = playerViewModel::togglePlayPause,
                        onNext = playerViewModel::next,
                        onClick = { showPlayer = true }
                    )
                }
            }
        }
    }
}
