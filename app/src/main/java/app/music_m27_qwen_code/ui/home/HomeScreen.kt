package app.music_m27_qwen_code.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.music_m27_qwen_code.data.model.Song
import app.music_m27_qwen_code.ui.components.*
import app.music_m27_qwen_code.ui.theme.*
import app.music_m27_qwen_code.viewmodel.HomeTab
import app.music_m27_qwen_code.viewmodel.LibraryUiState
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: LibraryUiState,
    onScanMusic: () -> Unit,
    onSearch: (String) -> Unit,
    onTabSelected: (HomeTab) -> Unit,
    onSongClick: (Song, Int) -> Unit,
    onOpenPlayer: () -> Unit,
    onOpenPlaylist: (Long) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = { Text("本地音乐", color = TextPrimary) },
            actions = {
                IconButton(onClick = onScanMusic) {
                    Icon(Icons.Default.Refresh, "扫描", tint = TextPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearch(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("搜索本地音乐...") },
            leadingIcon = { Icon(Icons.Default.Search, "搜索") },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        TabRow(
            selectedTabIndex = uiState.selectedTab.ordinal,
            containerColor = DarkBackground,
            contentColor = PrimaryGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab.ordinal]),
                    color = PrimaryGreen
                )
            }
        ) {
            HomeTab.entries.forEach { tab ->
                Tab(
                    selected = uiState.selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = when (tab) {
                                HomeTab.RECOMMEND -> "推荐"
                                HomeTab.PLAYLIST -> "歌单"
                                HomeTab.ARTIST -> "歌手"
                                HomeTab.ALBUM -> "专辑"
                            }
                        )
                    }
                )
            }
        }

        if (searchQuery.isNotEmpty() && uiState.searchResults.isNotEmpty()) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.searchResults) { song ->
                    SongListItem(
                        song = song,
                        onClick = { onSongClick(song, uiState.searchResults.indexOf(song)) },
                        onMoreClick = { }
                    )
                }
            }
        } else {
            when (uiState.selectedTab) {
                HomeTab.RECOMMEND -> RecommendTab(
                    recentSongs = uiState.recentSongs,
                    favorites = uiState.favorites,
                    allSongs = uiState.allSongs,
                    onSongClick = onSongClick,
                    onAllSongsClick = { onSongClick(uiState.allSongs.first(), 0) },
                    onFavoritesClick = { onSongClick(uiState.favorites.first(), 0) }
                )
                HomeTab.PLAYLIST -> PlaylistTab(
                    playlists = uiState.playlists,
                    onPlaylistClick = onOpenPlaylist,
                    onCreateClick = { showCreateDialog = true }
                )
                HomeTab.ARTIST -> ArtistTab(songs = uiState.allSongs)
                HomeTab.ALBUM -> AlbumTab(songs = uiState.allSongs)
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                onCreatePlaylist(name)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun RecommendTab(
    recentSongs: List<Song>,
    favorites: List<Song>,
    allSongs: List<Song>,
    onSongClick: (Song, Int) -> Unit,
    onAllSongsClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (recentSongs.isNotEmpty()) {
            item {
                SectionHeader("最近播放")
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentSongs.take(10)) { song ->
                        RecentSongCard(
                            song = song,
                            onClick = { onSongClick(song, recentSongs.indexOf(song)) }
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item { SectionHeader("本地歌单") }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    QuickPlaylistCard(
                        name = "所有歌曲",
                        onClick = onAllSongsClick,
                        icon = {
                            Icon(
                                Icons.Default.MusicNote,
                                "所有歌曲",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    )
                }
                item {
                    QuickPlaylistCard(
                        name = "我的收藏",
                        onClick = onFavoritesClick,
                        icon = {
                            Icon(
                                Icons.Default.Favorite,
                                "收藏",
                                tint = AccentPink,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item { SectionHeader("歌曲列表") }
        items(allSongs.take(30)) { song ->
            SongListItem(
                song = song,
                onClick = { onSongClick(song, allSongs.indexOf(song)) },
                onMoreClick = { }
            )
        }
    }
}

@Composable
private fun PlaylistTab(
    playlists: List<app.music_m27_qwen_code.data.model.Playlist>,
    onPlaylistClick: (Long) -> Unit,
    onCreateClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            QuickPlaylistCard(
                name = "新建歌单",
                onClick = onCreateClick,
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    Icon(
                        Icons.Default.Add,
                        "新建",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        items(playlists) { playlist ->
            PlaylistCard(
                playlist = playlist,
                songCount = 0,
                onClick = { onPlaylistClick(playlist.id) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ArtistTab(songs: List<Song>) {
    val artists = songs.groupBy { it.artist }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(artists.entries.toList()) { (artist, artistSongs) ->
            ListItem(
                headlineContent = { Text(artist) },
                supportingContent = { Text("${artistSongs.size} 首歌曲") },
                leadingContent = {
                    Icon(Icons.Default.Person, "歌手", modifier = Modifier.size(40.dp))
                }
            )
        }
    }
}

@Composable
private fun AlbumTab(songs: List<Song>) {
    val albums = songs.groupBy { it.album }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(albums.entries.toList()) { (album, albumSongs) ->
            ListItem(
                headlineContent = { Text(album) },
                supportingContent = { Text("${albumSongs.size} 首歌曲") },
                leadingContent = {
                    albumSongs.firstOrNull()?.albumArtUri?.let { uri ->
                        coil3.compose.AsyncImage(
                            model = uri,
                            contentDescription = album,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    } ?: Icon(Icons.Default.Album, "专辑", modifier = Modifier.size(48.dp))
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun RecentSongCard(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
    ) {
        coil3.compose.AsyncImage(
            model = song.albumArtUri,
            contentDescription = song.title,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建歌单") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("歌单名称") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onCreate(name) }, enabled = name.isNotBlank()) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
