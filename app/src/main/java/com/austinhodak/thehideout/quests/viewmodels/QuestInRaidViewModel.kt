package com.austinhodak.thehideout.quests.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.models.QuestExtra
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.tarkovapi.utils.QuestExtraHelper
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.mapsList
import com.austinhodak.thehideout.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestInRaidViewModel @Inject constructor(
    private val repository: TarkovRepo,
    @ApplicationContext context: Context
) : ViewModel() {

    private val _questsExtras = MutableLiveData<List<QuestExtra.QuestExtraItem>>()
    val questsExtra = _questsExtras

    private val _userData = MutableLiveData<User?>(null)
    val userData = _userData

    init {
        if (uid() != null) {
            questsFirebase.child("users/${uid()}").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _userData.value = snapshot.getValue<User>()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

        _questsExtras.value = QuestExtraHelper.getQuests(context = context)
    }

    private val _questDetails = MutableLiveData<Quest?>(null)
    val questDetails = _questDetails

    fun getQuest(id: String) {
        viewModelScope.launch {
            repository.getQuestByID(id).collect {
                _questDetails.value = it
            }
        }
    }

    fun getItem(id: String) = flow {
        viewModelScope.launch(Dispatchers.IO) {
            emit(repository.getItemByID(id))
        }
    }

    suspend fun getObjectiveText(questObjective: Quest.QuestObjective): String {
        val location = mapsList.getMap(questObjective.location?.toInt()) ?: "Any Map"
        val item = if (questObjective.type == "key" || questObjective.targetItem == null) {
            repository.getItemByID(questObjective.target?.get(0) ?: "").firstOrNull()?.pricing ?: questObjective.target?.first()
        } else {
            questObjective.targetItem
        }

        val itemName = if (item is Pricing) {
            item.name
        } else {
            item as String
        }

        return when (questObjective.type) {
            "key" -> "$itemName needed"
            "pickup" -> "$itemName"
            "kill" -> "${questObjective.number} $itemName"
            "collect" -> "${questObjective.number} $itemName"
            "place" -> "$itemName"
            "mark" -> "Place MS2000 marker"
            "locate" -> "$itemName"
            "find" -> "${questObjective.number} $itemName"
            "reputation" -> "Loyalty level ${questObjective.number} with ${Traders.values().find { it.int == questObjective.target?.first()?.toInt() ?: 0}?.id}"
            "warning" -> "$itemName"
            "skill" -> "Skill level ${questObjective.number} with $itemName"
            "survive" -> "$location ${questObjective.number} times."
            "build" -> "$itemName"
            else -> ""
        }
    }
}