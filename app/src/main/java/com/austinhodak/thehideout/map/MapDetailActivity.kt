package com.austinhodak.thehideout.map

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apollographql.apollo3.ApolloClient
import com.austinhodak.tarkovapi.MapEnums
import com.austinhodak.tarkovapi.MapsQuery
import com.austinhodak.tarkovapi.models.Stim
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.LoadingItem
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.flea_market.detail.BasicStatRow
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class MapDetailActivity : AppCompatActivity() {

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    @Inject
    lateinit var apolloClient: ApolloClient

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mapID = intent.getStringExtra("id") ?: "56f40101d2720b2a4d8b45d6"

        setContent {

            var mapData: MapsQuery.Map? by remember { mutableStateOf(null) }

            LaunchedEffect(key1 = "maps") {
                try {
                    val data = apolloClient.query(MapsQuery()).execute().data
                    mapData = data?.maps?.find { it?.id == mapID }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val icon = MapEnums.values().find { it.int.toString() == mapData?.tarkovDataId }?.icon ?: R.drawable.icons8_map_96

            HideoutTheme {

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row {
                                    Image(
                                        painter = painterResource(icon), contentDescription = null,
                                        Modifier.size(36.dp)
                                    )
                                    Column(Modifier.padding(start = 16.dp)) {
                                        Text(
                                            text = mapData?.name ?: "Loading...",
                                            color = MaterialTheme.colors.onPrimary,
                                            style = MaterialTheme.typography.h6,
                                            maxLines = 1,
                                            fontSize = 18.sp,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = mapData?.description ?: "",
                                            color = MaterialTheme.colors.onPrimary,
                                            style = MaterialTheme.typography.caption,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            },
                            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                }
                            }
                        )
                    }
                ) {
                    if (mapData == null) {
                        LoadingItem()
                        return@Scaffold
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        item {
                            BasicStatsCard(map = mapData!!)
                        }
                        item {
                            mapData?.bosses?.forEach { boss ->
                                BossCard(boss!!)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BossCard(boss: MapsQuery.Boss) {
        Card(
            backgroundColor = if (isSystemInDarkTheme()) Color(
                0xFE1F1F1F
            ) else MaterialTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column (Modifier.padding(bottom = 8.dp)) {
                Row (Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    //Icon
                    Image(painter = painterResource(id = boss.icon()), contentDescription = null, Modifier.size(42.dp), contentScale = ContentScale.Crop)
                    Column(
                        Modifier.padding(start = 16.dp)
                    ) {
                        Text(
                            text = boss.name ?: "",
                            fontWeight = FontWeight.Medium,
                            color = White,
                            style = MaterialTheme.typography.h6,
                            maxLines = 1,
                            fontSize = 18.sp,
                        )
                        Text(
                            text = "${(boss.spawnChance * 100).roundToInt()}% Spawn Chance",
                            style = MaterialTheme.typography.caption,
                            color = White
                        )
                    }
                }
                Divider(color = DividerDark)
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "SPAWN LOCATIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
                    )
                }
                boss.spawnLocations.forEach { location ->
                    BasicStatRow(title = location?.name?.uppercase() ?: "", text = "${(location?.chance?.times(100))?.roundToInt()}%")
                }
                if (boss.escorts.isNotEmpty()) {
                    Divider(
                        Modifier.padding(top = 8.dp),
                        color = DividerDark
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "ESCORTS (COUNT/CHANCE)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
                        )
                    }
                    boss.escorts.forEach { escort ->
                        BasicStatRow(title = escort?.name?.uppercase() ?: "", text = "${escort?.amount?.joinToString(separator = " • ") { 
                            val chance = (it?.chance?.times(100))?.roundToInt() ?: 100
                            "${it?.count}/${chance}%"
                        }}")
                    }
                }
            }
        }
    }

    @Composable
    private fun BasicStatsCard(map: MapsQuery.Map) {
        Card(
            backgroundColor = if (isSystemInDarkTheme()) Color(
                0xFE1F1F1F
            ) else MaterialTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(
                Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(
                        bottom = 8.dp,
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "INFO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                        )
                    }
                }
                BasicStatRow(title = "PLAYERS", text = map.players)
                BasicStatRow(title = "RAID DURATION", text = "${map.raidDuration} Minutes")
            }
        }
    }

    @Composable
    private fun BuffsCard(item: Item, effects: Stim?) {
        Card(
            backgroundColor = if (isSystemInDarkTheme()) Color(
                0xFE1F1F1F
            ) else MaterialTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(
                Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(
                        bottom = 8.dp,
                        top = 16.dp,
                        start = 0.dp,
                        end = 16.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "BUFFS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
                item.getBuffs(effects).filter { it.type == "buff" }.sortedBy { it.icon == null }.forEach { effect ->
                    Row(
                        modifier = Modifier.padding(end = 16.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        effect.icon?.let {
                            Image(painter = painterResource(id = it), contentDescription = null, modifier = Modifier
                                .padding(start = 16.dp)
                                .size(16.dp))
                        }
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = effect.title.uppercase(),
                                style = MaterialTheme.typography.body1,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 16.dp)
                                    .weight(1f),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                        //Spacer(modifier = Modifier.weight(1f))
                        if (effect.color != null) {
                            Text(
                                text = effect.value ?: "",
                                style = MaterialTheme.typography.body1,
                                fontSize = 14.sp,
                                color = effect.color!!
                            )
                        } else {
                            Text(
                                text = effect.value ?: "",
                                style = MaterialTheme.typography.body1,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DeBuffsCard(item: Item, effects: Stim?) {
        Card(
            backgroundColor = if (isSystemInDarkTheme()) Color(
                0xFE1F1F1F
            ) else MaterialTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(
                Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(
                        bottom = 8.dp,
                        top = 16.dp,
                        start = 0.dp,
                        end = 16.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "DEBUFFS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
                item.getBuffs(effects).filter { it.type == "debuff" }.sortedBy { it.icon == null }.forEach { effect ->
                    Row(
                        modifier = Modifier.padding(end = 16.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        effect.icon?.let {
                            Image(painter = painterResource(id = it), contentDescription = null, modifier = Modifier
                                .padding(start = 16.dp)
                                .size(16.dp))
                        }
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = effect.title.uppercase(),
                                style = MaterialTheme.typography.body1,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 16.dp)
                                    .weight(1f),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                        //Spacer(modifier = Modifier.weight(1f))
                        if (effect.color != null) {
                            Text(
                                text = effect.value ?: "",
                                style = MaterialTheme.typography.body1,
                                fontSize = 14.sp,
                                color = effect.color!!
                            )
                        } else {
                            Text(
                                text = effect.value ?: "",
                                style = MaterialTheme.typography.body1,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }
    }

}