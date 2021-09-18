package com.austinhodak.thehideout.quests

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.Maps
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.EmptyText
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.mapsList
import com.austinhodak.thehideout.quests.viewmodels.QuestMainViewModel
import com.austinhodak.thehideout.utils.getIcon
import com.austinhodak.thehideout.utils.isAvailable
import com.austinhodak.thehideout.utils.isLocked
import com.austinhodak.thehideout.utils.openActivity
import com.google.accompanist.pager.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun QuestMainScreen(
    navViewModel: NavViewModel,
    questViewModel: QuestMainViewModel,
    tarkovRepo: TarkovRepo
) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val quests by tarkovRepo.getAllQuests().collectAsState(initial = emptyList())

    val isSearchOpen by questViewModel.isSearchOpen.observeAsState(false)

    HideoutTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            bottomBar = {
                QuestBottomNav(navController = navController)
            },
            floatingActionButton = {
                /*FloatingActionButton(onClick = { }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "")
                }*/
            },
            topBar = {
                if (isSearchOpen) {
                    SearchToolbar(
                        onClosePressed = {
                            questViewModel.setSearchOpen(false)
                            questViewModel.clearSearch()
                        },
                        onValue = {
                            questViewModel.setSearchKey(it)
                        }
                    )
                } else {
                    TopAppBar(
                        title = {
                            val selected by questViewModel.view.observeAsState()
                            val scrollState = rememberScrollState()
                            Row(
                                Modifier.horizontalScroll(scrollState),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                when (navBackStackEntry?.destination?.route) {
                                    BottomNavigationScreens.Overview.route,
                                    BottomNavigationScreens.Items.route -> {
                                        Text(
                                            "Quests",
                                            modifier = Modifier.padding(end = 16.dp)
                                        )
                                    }
                                    BottomNavigationScreens.Quests.route,
                                    BottomNavigationScreens.Maps.route -> {
                                        Chip(text = "Available", selected = selected == QuestFilter.AVAILABLE) {
                                            questViewModel.setView(QuestFilter.AVAILABLE)
                                        }
                                        Chip(text = "Locked", selected = selected == QuestFilter.LOCKED) {
                                            questViewModel.setView(QuestFilter.LOCKED)
                                        }
                                        Chip(text = "Completed", selected = selected == QuestFilter.COMPLETED) {
                                            questViewModel.setView(QuestFilter.COMPLETED)
                                        }
                                        Chip(text = "All", selected = selected == QuestFilter.ALL) {
                                            questViewModel.setView(QuestFilter.ALL)
                                        }
                                    }
                                    else -> {
                                        Text(
                                            "Quests",
                                            modifier = Modifier.padding(end = 16.dp)
                                        )
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                navViewModel.isDrawerOpen.value = true
                            }) {
                                Icon(Icons.Filled.Menu, contentDescription = null, tint = White)
                            }
                        },
                        backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                        elevation = 0.dp,
                        actions = {
                            when (navBackStackEntry?.destination?.route) {
                                BottomNavigationScreens.Maps.route,
                                BottomNavigationScreens.Quests.route -> {
                                    IconButton(onClick = {
                                        questViewModel.setSearchOpen(true)
                                    }) {
                                        Icon(Icons.Filled.Search, contentDescription = null, tint = White)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        ) { padding ->

            if (quests.isNullOrEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colors.secondary
                    )
                }
            } else {
                NavHost(navController = navController, startDestination = BottomNavigationScreens.Overview.route) {
                    composable(BottomNavigationScreens.Overview.route) {
                        QuestOverviewScreen(questViewModel = questViewModel, tarkovRepo = tarkovRepo, quests)
                    }
                    composable(BottomNavigationScreens.Quests.route) {
                        QuestTradersScreen(questViewModel = questViewModel, scope = scope, quests = quests, padding = padding, isMapTab = false)
                    }
                    composable(BottomNavigationScreens.Items.route) {

                    }
                    composable(BottomNavigationScreens.Maps.route) {
                        QuestTradersScreen(questViewModel = questViewModel, scope = scope, quests = quests, padding = padding, isMapTab = true)
                    }
                }
            }
        }
    }
}

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
private fun QuestTradersScreen(
    questViewModel: QuestMainViewModel,
    scope: CoroutineScope,
    quests: List<Quest>,
    padding: PaddingValues,
    isMapTab: Boolean
) {
    val userData by questViewModel.userData.observeAsState()
    val selectedView by questViewModel.view.observeAsState()
    val searchKey by questViewModel.searchKey.observeAsState("")

    val pagerState = if (!isMapTab) {
        rememberPagerState(pageCount = Traders.values().size)
    } else {
        rememberPagerState(pageCount = Maps.values().size)
    }

    val completedQuests = userData?.quests?.values?.filter { it?.completed == true }?.map { it?.id }

    Column(
        Modifier.fillMaxWidth()
    ) {
        if (!isMapTab) {
            TraderTabs(pagerState, scope)
        } else {
            MapsTab(pagerState, scope)
        }

        HorizontalPager(modifier = Modifier.fillMaxWidth(), state = pagerState) { page ->
            val questsList = if (!isMapTab) {
                val trader = Traders.values()[page]
                quests.filter { it.giver?.name == trader.id }
            } else {
                val map = Maps.values()[page]
                quests.filter {
                    if (map.int == -1) {
                        it.getMapsIDs()?.contains(map.int) == true
                    } else {
                        it.getMapsIDs()?.contains(map.int) == true
                    }
                }
            }
            val data = when (selectedView) {
                QuestFilter.AVAILABLE -> {
                    questsList.filter {
                        if (userData?.isQuestCompleted(it) == true) {
                            false
                        } else {
                            if (it.requirement?.quests.isNullOrEmpty() && it.requirement?.level ?: 0 <= 15) {
                                true
                            } else {
                                it.requirement?.quests?.forEach {
                                    if (it != null)
                                        return@filter completedQuests?.containsAll(it) == true
                                }
                                false
                            }
                        }
                    }
                }
                QuestFilter.LOCKED -> {
                    questsList.filterNot {
                        if (userData?.isQuestCompleted(it) == true) {
                            true
                        } else {
                            if (it.requirement?.quests.isNullOrEmpty() && it.requirement?.level ?: 0 <= 15) {
                                true
                            } else {
                                it.requirement?.quests?.forEach {
                                    if (it != null)
                                        return@filterNot completedQuests?.containsAll(it) == true
                                }
                                false
                            }
                        }
                    }
                }
                QuestFilter.ALL -> questsList
                QuestFilter.COMPLETED -> {
                    questsList.filter {
                        completedQuests?.contains(it.id.toInt()) == true
                    }
                }
                else -> questsList
            }.filter {
                it.title?.contains(searchKey, ignoreCase = true) == true
                        || it.getMaps(mapsList).contains(searchKey, ignoreCase = true)
            }

            if (data.isEmpty()) {
                EmptyText(text = "No Quests.")
                return@HorizontalPager
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp, bottom = padding.calculateBottomPadding())
            ) {
                data.forEach {
                    item {
                        QuestCard(it, userData, questViewModel, scope)
                    }
                }
            }
        }
    }

}

@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
private fun QuestCard(
    quest: Quest,
    userData: User?,
    questViewModel: QuestMainViewModel,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .fillMaxWidth(),
        backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
        onClick = {
            context.openActivity(QuestDetailActivity::class.java) {
                putString("questID", quest.id)
            }
        }
    ) {
        Column {
            Row(
                Modifier.padding(16.dp)
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                ) {
                    Text(
                        text = quest.title.toString(),
                        style = MaterialTheme.typography.h6,
                        fontSize = 18.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = quest.getMaps(mapsList),
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
                Column(
                    Modifier.fillMaxHeight()
                ) {
                    /*CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "Level ${quest.requirement?.level}",
                            style = MaterialTheme.typography.overline
                        )
                    }*/
                    when {
                        quest.isLocked(userData) -> {
                            OutlinedButton(
                                onClick = {
                                    questViewModel.skipToQuest(quest)
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = Color.Transparent,
                                    contentColor = Color.Gray
                                ),
                                border = BorderStroke(1.dp, color = Color.Gray)
                            ) {
                                Text("SKIP TO")
                            }
                        }
                        quest.isAvailable(userData) -> {
                            OutlinedButton(
                                onClick = { questViewModel.markQuestCompleted(quest) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = Color.Transparent,
                                    contentColor = Red400
                                ),
                                border = BorderStroke(1.dp, color = Red400)
                            ) {
                                Text("COMPLETE")
                            }
                        }
                        else -> {
                            OutlinedButton(
                                onClick = { questViewModel.undoQuest(quest, true) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = Color.Transparent,
                                    contentColor = Color.Gray
                                ),
                                border = BorderStroke(1.dp, color = Color.Gray)
                            ) {
                                Text("UNDO")
                            }
                        }
                    }
                }
            }
            Divider()
            Column(
                Modifier.padding(vertical = 8.dp)
            ) {
                quest.objective?.forEach {
                    QuestObjectiveItem(it, questViewModel, scope, userData, quest)
                }
            }
        }
    }
}

@Composable
private fun QuestObjectiveItem(
    questObjective: Quest.QuestObjective,
    questViewModel: QuestMainViewModel,
    scope: CoroutineScope,
    userData: User?,
    quest: Quest
) {
    var text by remember { mutableStateOf("") }

    Row(
        Modifier
            .height(36.dp)
            .clickable {
                questViewModel.toggleObjective(quest, questObjective)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        scope.launch {
            text = questViewModel.getObjectiveText(questObjective)
        }
        Icon(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(20.dp),
            painter = painterResource(id = questObjective.getIcon()),
            contentDescription = "",
            tint = if (userData?.isObjectiveCompleted(questObjective) == true) Green500 else White
        )
        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 16.dp),
            style = MaterialTheme.typography.body2,
            color = if (userData?.isObjectiveCompleted(questObjective) == true) Green500 else White
        )
    }
}

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
private fun TraderTabs(
    pagerState: PagerState,
    scope: CoroutineScope
) {
    ScrollableTabRow(
        modifier = Modifier.fillMaxWidth(),
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, tabPositions), color = Red400)
        },
    ) {
        Traders.values().forEachIndexed { index, trader ->
            LeadingIconTab(
                text = { Text(trader.id, fontFamily = Bender) },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                icon = {
                    Image(
                        painter = painterResource(id = trader.icon),
                        contentDescription = "",
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(24.dp)
                    )
                },
                selectedContentColor = Red400,
                unselectedContentColor = White
            )
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
private fun MapsTab(
    pagerState: PagerState,
    scope: CoroutineScope
) {
    ScrollableTabRow(
        modifier = Modifier.fillMaxWidth(),
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, tabPositions), color = Red400)
        },
    ) {
        Maps.values().forEachIndexed { index, map ->
            LeadingIconTab(
                text = { Text(map.id, fontFamily = Bender) },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                icon = {
                    Image(
                        painter = painterResource(id = map.icon),
                        contentDescription = "",
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(24.dp)
                    )
                },
                selectedContentColor = Red400,
                unselectedContentColor = White
            )
        }
    }
}

@Composable
private fun QuestOverviewScreen(
    questViewModel: QuestMainViewModel,
    tarkovRepo: TarkovRepo,
    quests: List<Quest>
) {
    val questTotal = quests.size
    val pmcElimsTotal by questViewModel.pmcElimsTotal.observeAsState()
    val scavElimsTotal by questViewModel.scavElimsTotal.observeAsState()
    val questItemsTotal by questViewModel.questItemsTotal.observeAsState()
    val questFIRItemsTotal by questViewModel.questFIRItemsTotal.observeAsState()
    val handoverItemsTotal by questViewModel.handoverItemsTotal.observeAsState()
    val placedTotal by questViewModel.placedTotal.observeAsState()
    val pickupTotal by questViewModel.pickupTotal.observeAsState()

    val questTotalCompletedUser by questViewModel.questTotalCompletedUser.observeAsState()
    val pmcElimsTotalUser by questViewModel.pmcElimsTotalUser.observeAsState()
    val scavElimsTotalUser by questViewModel.scavElimsTotalUser.observeAsState()
    val questItemsTotalUser by questViewModel.questItemsTotalUser.observeAsState()
    val placedTotalUser by questViewModel.placedTotalUser.observeAsState()
    val pickupTotalUser by questViewModel.pickupTotalUser.observeAsState()

    LazyColumn(
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        item {
            OverviewItem(
                color = Green500,
                s1 = "Quests Completed",
                s2 = "$questTotalCompletedUser/$questTotal",
                progress = (questTotalCompletedUser?.toDouble()?.div(questTotal.toDouble()))?.toFloat(),
            )
        }
        item {
            OverviewItem(
                color = Red500,
                s1 = "PMC Eliminations",
                s2 = "$pmcElimsTotalUser/$pmcElimsTotal",
                progress = (pmcElimsTotalUser?.toDouble()?.div(pmcElimsTotal?.toDouble() ?: 1.0))?.toFloat(),
                icon = R.drawable.icons8_sniper_96
            )
        }
        item {
            OverviewItem(
                color = Color(0xFFFF9800),
                s1 = "Scav Eliminations",
                s2 = "$scavElimsTotalUser/$scavElimsTotal",
                progress = (scavElimsTotalUser?.toDouble()?.div(scavElimsTotal?.toDouble() ?: 1.0))?.toFloat(),
                icon = R.drawable.icons8_target_96
            )
        }
        item {
            OverviewItem(
                color = Color(0xFF03A9F4),
                s1 = "Quest Items",
                s2 = "$questItemsTotalUser/$questItemsTotal",
                progress = (questItemsTotalUser?.toDouble()?.div(questItemsTotal?.toDouble() ?: 1.0))?.toFloat(),
                icon = R.drawable.ic_search_black_24dp
            )
        }
        /*item {
            OverviewItem(
                color = Color(0xFF03A9F4),
                s1 = "Found in Raid Items",
                s2 = "0/$questFIRItemsTotal",
                progress = 0f,
                icon = R.drawable.ic_baseline_check_circle_outline_24
            )
        }
        item {
            OverviewItem(
                color = Color(0xFF03A9F4),
                s1 = "Handover Items",
                s2 = "0/$handoverItemsTotal",
                progress = 0f,
                icon = R.drawable.ic_baseline_swap_horizontal_circle_24
            )
        }*/
        item {
            OverviewItem(
                color = Color(0xFF9C27B0),
                s1 = "Placed Objectives",
                s2 = "$placedTotalUser/$placedTotal",
                progress = (placedTotalUser?.toDouble()?.div(placedTotal?.toDouble() ?: 1.0))?.toFloat(),
                icon = R.drawable.icons8_low_importance_96
            )
        }
        item {
            OverviewItem(
                color = Color(0xFF9C27B0),
                s1 = "Pickup Objectives",
                s2 = "$pickupTotalUser/$pickupTotal",
                progress = (pickupTotalUser?.toDouble()?.div(pickupTotal?.toDouble() ?: 1.0))?.toFloat(),
                icon = R.drawable.icons8_upward_arrow_96
            )
        }
    }
}

@Composable
private fun OverviewItem(
    color: Color = Color.Gray,
    icon: Int = R.drawable.ic_baseline_assignment_turned_in_24,
    s1: String = "",
    s2: String = "",
    progress: Float? = 0.5f
) {
    val p by remember { mutableStateOf(progress) }
    val animatedProgress by animateFloatAsState(
        targetValue = p ?: 0.5f,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .height(72.dp)
            .fillMaxWidth(),
        backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
    ) {
        Row {
            Icon(
                modifier = Modifier
                    .size(72.dp)
                    .background(color)
                    .padding(24.dp),
                painter = painterResource(id = icon),
                contentDescription = "",
                tint = Color.White
            )
            Column(
                Modifier.weight(1f)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth(),
                    progress = animatedProgress,
                    color = color
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = s1,
                        style = MaterialTheme.typography.h6,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        modifier = Modifier,
                        text = s2,
                        style = MaterialTheme.typography.h5,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun Chip(
    selected: Boolean = false,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        color = when {
            selected -> Red400
            else -> Color(0xFF2F2F2F)
        },
        contentColor = when {
            selected -> Color.Black
            else -> Color.White
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                onClick()
            }
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(8.dp),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuestBottomNav(
    navController: NavController
) {
    val items = listOf(
        BottomNavigationScreens.Overview,
        BottomNavigationScreens.Quests,
        //BottomNavigationScreens.Items,
        BottomNavigationScreens.Maps
    )

    BottomNavigation(
        backgroundColor = Color(0xFE1F1F1F)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEachIndexed { _, item ->
            BottomNavigationItem(
                icon = {
                    if (item.icon != null) {
                        Icon(item.icon, "")
                    } else {
                        Icon(painter = painterResource(id = item.iconDrawable!!), contentDescription = item.resourceId)
                    }
                },
                label = { Text(item.resourceId) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                alwaysShowLabel = false, // This hides the title for the unselected items
                onClick = {
                    try {
                        if (currentDestination?.route == item.route) return@BottomNavigationItem
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                },
                selectedContentColor = MaterialTheme.colors.secondary,
                unselectedContentColor = Color(0x99FFFFFF),
            )
        }
    }
}

sealed class BottomNavigationScreens(
    val route: String,
    val resourceId: String,
    val icon: ImageVector? = null,
    @DrawableRes val iconDrawable: Int? = null
) {
    object Overview : BottomNavigationScreens("Overview", "Overview", null, R.drawable.ic_baseline_dashboard_24)
    object Quests : BottomNavigationScreens("Quests", "Quests", null, R.drawable.ic_baseline_assignment_turned_in_24)
    object Items : BottomNavigationScreens("Items", "Items", null, R.drawable.ic_baseline_assignment_24)
    object Maps : BottomNavigationScreens("Maps", "Maps", null, R.drawable.ic_baseline_map_24)
}

enum class QuestFilter {
    AVAILABLE,
    LOCKED,
    COMPLETED,
    ALL
}