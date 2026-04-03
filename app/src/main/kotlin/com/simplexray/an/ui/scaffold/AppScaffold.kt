package com.simplexray.an.ui.scaffold

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.simplexray.an.R
import com.simplexray.an.common.ROUTE_CONFIG
import com.simplexray.an.common.ROUTE_LOG
import com.simplexray.an.common.ROUTE_SETTINGS
import com.simplexray.an.common.ROUTE_STATS
import com.simplexray.an.ui.theme.AppThemeAnimationDefaults
import com.simplexray.an.viewmodel.LogViewModel
import com.simplexray.an.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppScaffold(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    mainViewModel: MainViewModel,
    logViewModel: LogViewModel,
    onCreateNewConfigFileAndEdit: () -> Unit,
    onImportConfigFromClipboard: () -> Unit,
    onPerformExport: () -> Unit,
    onPerformBackup: () -> Unit,
    onPerformRestore: () -> Unit,
    onSwitchVpnService: () -> Unit,
    logListState: LazyListState,
    configListState: LazyListState,
    settingsListState: LazyListState,
    content: @Composable (paddingValues: androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var isLogSearching by remember { mutableStateOf(false) }
    val logSearchQuery by logViewModel.searchQuery.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isLogSearching) {
        if (isLogSearching) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopAppBar(
                currentRoute,
                onCreateNewConfigFileAndEdit,
                onImportConfigFromClipboard,
                onPerformExport,
                onPerformBackup,
                onPerformRestore,
                onSwitchVpnService,
                mainViewModel.controlMenuClickable.collectAsState().value,
                mainViewModel.isServiceEnabled.collectAsState().value,
                logViewModel,
                logListState = logListState,
                configListState = configListState,
                settingsListState = settingsListState,
                isLogSearching = isLogSearching,
                onLogSearchingChange = { isLogSearching = it },
                logSearchQuery = logSearchQuery,
                onLogSearchQueryChange = { logViewModel.onSearchQueryChange(it) },
                focusRequester = focusRequester,
                mainViewModel = mainViewModel
            )
        },
        bottomBar = {
            AppBottomNavigationBar(navController)
        },
        contentWindowInsets = androidx.compose.material3.ScaffoldDefaults.contentWindowInsets
    ) { paddingValues ->
        content(paddingValues)
    }
}

@Composable
fun AppTopAppBar(
    currentRoute: String?,
    onCreateNewConfigFileAndEdit: () -> Unit,
    onImportConfigFromClipboard: () -> Unit,
    onPerformExport: () -> Unit,
    onPerformBackup: () -> Unit,
    onPerformRestore: () -> Unit,
    onSwitchVpnService: () -> Unit,
    controlMenuClickable: Boolean,
    isServiceEnabled: Boolean,
    logViewModel: LogViewModel,
    logListState: LazyListState,
    configListState: LazyListState,
    settingsListState: LazyListState,
    isLogSearching: Boolean = false,
    onLogSearchingChange: (Boolean) -> Unit = {},
    logSearchQuery: String = "",
    onLogSearchQueryChange: (String) -> Unit = {},
    focusRequester: FocusRequester = FocusRequester(),
    mainViewModel: MainViewModel
) {
    val title = when (currentRoute) {
        "stats" -> stringResource(R.string.core_stats_title)
        "config" -> stringResource(R.string.configuration)
        "log" -> stringResource(R.string.log)
        "settings" -> stringResource(R.string.settings)
        else -> stringResource(R.string.app_name)
    }

    val showScrolledColor by remember(
        currentRoute,
        logListState,
        configListState,
        settingsListState
    ) {
        derivedStateOf {
            when (currentRoute) {
                "log" -> logListState.firstVisibleItemIndex > 0 || logListState.firstVisibleItemScrollOffset > 0
                "config" -> configListState.firstVisibleItemIndex > 0 || configListState.firstVisibleItemScrollOffset > 0
                "settings" -> settingsListState.firstVisibleItemIndex > 0 || settingsListState.firstVisibleItemScrollOffset > 0
                else -> false
            }
        }
    }

    val topAppBarContainerColor = MaterialTheme.colorScheme.run {
        lerp(
            start = surface,
            stop = surfaceContainer,
            fraction = animateFloatAsState(
                targetValue = if (showScrolledColor) 1f else 0f,
                animationSpec = AppThemeAnimationDefaults.TopAppBarScrollAnimationSpec,
                label = "topAppBarScrollFraction"
            ).value
        )
    }
    val topAppBarContentColor = MaterialTheme.colorScheme.onSurface

    Surface(
        color = topAppBarContainerColor,
        contentColor = topAppBarContentColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentRoute == "log" && isLogSearching) {
                IconButton(onClick = {
                    onLogSearchingChange(false)
                    onLogSearchQueryChange("")
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.close_search)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (currentRoute == "log" && isLogSearching) {
                    TextField(
                        value = logSearchQuery,
                        onValueChange = onLogSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text(stringResource(R.string.search)) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentRoute == "log" && isLogSearching) {
                    if (logSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onLogSearchQueryChange("") }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(R.string.clear_search)
                            )
                        }
                    }
                } else {
                    TopAppBarActions(
                        currentRoute = currentRoute,
                        onCreateNewConfigFileAndEdit = onCreateNewConfigFileAndEdit,
                        onImportConfigFromClipboard = onImportConfigFromClipboard,
                        onPerformExport = onPerformExport,
                        onPerformBackup = onPerformBackup,
                        onPerformRestore = onPerformRestore,
                        onSwitchVpnService = onSwitchVpnService,
                        controlMenuClickable = controlMenuClickable,
                        isServiceEnabled = isServiceEnabled,
                        logViewModel = logViewModel,
                        onLogSearchingChange = onLogSearchingChange,
                        mainViewModel = mainViewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.TopAppBarActions(
    currentRoute: String?,
    onCreateNewConfigFileAndEdit: () -> Unit,
    onImportConfigFromClipboard: () -> Unit,
    onPerformExport: () -> Unit,
    onPerformBackup: () -> Unit,
    onPerformRestore: () -> Unit,
    onSwitchVpnService: () -> Unit,
    controlMenuClickable: Boolean,
    isServiceEnabled: Boolean,
    logViewModel: LogViewModel,
    onLogSearchingChange: (Boolean) -> Unit = {},
    mainViewModel: MainViewModel
) {
    when (currentRoute) {
        "config" -> ConfigActions(
            onCreateNewConfigFileAndEdit = onCreateNewConfigFileAndEdit,
            onImportConfigFromClipboard = onImportConfigFromClipboard,
            onSwitchVpnService = onSwitchVpnService,
            controlMenuClickable = controlMenuClickable,
            isServiceEnabled = isServiceEnabled,
            mainViewModel = mainViewModel
        )

        "stats" -> StatsActions(
            onSwitchVpnService = onSwitchVpnService,
            controlMenuClickable = controlMenuClickable,
            isServiceEnabled = isServiceEnabled,
            mainViewModel = mainViewModel
        )

        "log" -> LogActions(
            onPerformExport = onPerformExport,
            logViewModel = logViewModel,
            onLogSearchingChange = onLogSearchingChange
        )

        "settings" -> SettingsActions(
            onPerformBackup = onPerformBackup,
            onPerformRestore = onPerformRestore
        )
    }
}

@Composable
private fun ConfigActions(
    onCreateNewConfigFileAndEdit: () -> Unit,
    onImportConfigFromClipboard: () -> Unit,
    onSwitchVpnService: () -> Unit,
    controlMenuClickable: Boolean,
    isServiceEnabled: Boolean,
    mainViewModel: MainViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = onSwitchVpnService,
        enabled = controlMenuClickable
    ) {
        Icon(
            painter = painterResource(
                id = if (isServiceEnabled) R.drawable.pause else R.drawable.play
            ),
            contentDescription = null
        )
    }

    OverflowMenu(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.new_profile)) },
            onClick = {
                onCreateNewConfigFileAndEdit()
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.import_from_clipboard)) },
            onClick = {
                expanded = false
                scope.launch {
                    delay(100)
                    onImportConfigFromClipboard()
                }
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.connectivity_test)) },
            onClick = {
                mainViewModel.testConnectivity()
                expanded = false
            },
            enabled = isServiceEnabled
        )
    }
}

@Composable
private fun LogActions(
    onPerformExport: () -> Unit,
    logViewModel: LogViewModel,
    onLogSearchingChange: (Boolean) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val hasLogsToExport by logViewModel.hasLogsToExport.collectAsStateWithLifecycle()

    IconButton(onClick = { onLogSearchingChange(true) }) {
        Icon(
            painterResource(id = R.drawable.search),
            contentDescription = stringResource(R.string.search)
        )
    }
    OverflowMenu(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.export)) },
            onClick = {
                onPerformExport()
                expanded = false
            },
            enabled = hasLogsToExport
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.clear_logs)) },
            onClick = {
                logViewModel.clearLogs()
                expanded = false
            },
            enabled = hasLogsToExport
        )
    }
}

@Composable
private fun SettingsActions(
    onPerformBackup: () -> Unit,
    onPerformRestore: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    OverflowMenu(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.backup)) },
            onClick = {
                onPerformBackup()
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.restore)) },
            onClick = {
                onPerformRestore()
                expanded = false
            }
        )
    }
}

@Composable
private fun StatsActions(
    onSwitchVpnService: () -> Unit,
    controlMenuClickable: Boolean,
    isServiceEnabled: Boolean,
    mainViewModel: MainViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = onSwitchVpnService,
        enabled = controlMenuClickable
    ) {
        Icon(
            painter = painterResource(
                id = if (isServiceEnabled) R.drawable.pause else R.drawable.play
            ),
            contentDescription = null
        )
    }

    OverflowMenu(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.connectivity_test)) },
            onClick = {
                mainViewModel.testConnectivity()
                expanded = false
            },
            enabled = isServiceEnabled
        )
    }
}

@Composable
private fun OverflowMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier.wrapContentSize(TopEnd),
        contentAlignment = TopEnd
    ) {
        IconButton(onClick = { onExpandedChange(true) }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.more)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            content = content
        )
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            alwaysShowLabel = false,
            selected = currentRoute == ROUTE_STATS,
            onClick = { navigateToRoute(navController, ROUTE_STATS) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.dashboard),
                    contentDescription = stringResource(R.string.core_stats_title)
                )
            },
            label = { Text(stringResource(R.string.core_stats_title)) }
        )
        NavigationBarItem(
            alwaysShowLabel = false,
            selected = currentRoute == ROUTE_CONFIG,
            onClick = { navigateToRoute(navController, ROUTE_CONFIG) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.code),
                    contentDescription = stringResource(R.string.configuration)
                )
            },
            label = { Text(stringResource(R.string.configuration)) }
        )
        NavigationBarItem(
            alwaysShowLabel = false,
            selected = currentRoute == ROUTE_LOG,
            onClick = { navigateToRoute(navController, ROUTE_LOG) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.history),
                    contentDescription = stringResource(R.string.log)
                )
            },
            label = { Text(stringResource(R.string.log)) }
        )
        NavigationBarItem(
            alwaysShowLabel = false,
            selected = currentRoute == ROUTE_SETTINGS,
            onClick = { navigateToRoute(navController, ROUTE_SETTINGS) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = stringResource(R.string.settings)
                )
            },
            label = { Text(stringResource(R.string.settings)) }
        )
    }
}

private fun navigateToRoute(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
