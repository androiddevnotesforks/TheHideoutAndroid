@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.austinhodak.thehideout.hideout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.getPrice
import com.austinhodak.thehideout.hideout.models.HideoutCraft
import com.austinhodak.thehideout.hideout.models.Input
import com.austinhodak.thehideout.viewmodels.FleaViewModel
import com.austinhodak.thehideout.viewmodels.HideoutViewModel
import com.bumptech.glide.Glide
import net.idik.lib.slimadapter.SlimAdapter
import kotlin.math.abs
import kotlin.math.roundToInt

class HideoutCraftsListFragment : Fragment() {

    private lateinit var mAdapter: SlimAdapter
    private lateinit var mRecyclerView: RecyclerView
    private val viewModel: HideoutViewModel by activityViewModels()
    private val fleaViewModel: FleaViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hideout_module_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)
        setupAdapter()
    }

    private fun setupRecyclerView(view: View) {
        mRecyclerView = view.findViewById(R.id.moduleList)
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupAdapter() {
        val greenTextColor = resources.getColor(R.color.md_green_400)
        val redTextColor = resources.getColor(R.color.md_red_400)

        mAdapter = SlimAdapter.create().register<HideoutCraft>(R.layout.hideout_craft_item) { craft, i ->

            //Requirements
            val requirementRV = i.findViewById<RecyclerView>(R.id.craftInputRV)
            requirementRV.layoutManager = LinearLayoutManager(requireContext())

            SlimAdapter.create().attachTo(requirementRV).register<Input>(R.layout.hideout_crafting_item_requirement) { inputItem, rI ->
                val fleaItem = fleaViewModel.getItemById(inputItem.id)
                val inputIcon = rI.findViewById<ImageView>(R.id.craftInputIcon)
                Glide.with(this).load(fleaItem.getItemIcon()).into(inputIcon)

                rI.text(R.id.craftInputName, "x${inputItem.qty} ${fleaItem.name}")
                rI.text(R.id.craftInputPrice, "${fleaItem.price?.times(inputItem.qty)?.roundToInt()?.getPrice("₽")}")
            }.updateData(craft.input)
            //Requirements

            val fleaItem = fleaViewModel.getItemById(craft.output.first().id)

            viewModel.getHideoutByID(craft.facility) {
                i.text(R.id.hideoutCraftItemModule, "INPUT • ${it.module.toUpperCase()} LVL ${it.level}")
            }

            //Craft Image
            Glide.with(this).load(fleaItem.getItemIcon()).into(i.findViewById(R.id.craftIcon))

            //Craft Name
            i.text(R.id.craftName, fleaItem.name)

            //Time to craft
            i.text(R.id.craftTime, craft.getTimeToCraft())

            //Craft Output Name + Qty
            i.text(R.id.craftOutputName, "${fleaItem.shortName} x${craft.output[0].qty}")

            //Craft Output Price
            i.text(R.id.craftOutputPrice, (fleaItem.price?.times(craft.output[0].qty)?.getPrice("₽")))

            //Craft Total Cost of Input Items
            val totalCostToCraft = fleaItem.getTotalCostToCraft(craft.input, fleaViewModel)

            //Total Cost Text
            i.text(R.id.craftOutputCost, totalCostToCraft.getPrice("₽"))

            //(Price of item * qty) - cost of items
            val profit = (fleaItem.price!! * craft.output[0].qty - totalCostToCraft)
            i.text(R.id.craftOutputProfit, profit.getPrice("₽"))
            i.textColor(R.id.craftOutputProfit, if (profit <= 0) redTextColor else greenTextColor)

            fleaItem.calculateTax {
                val tax = it * craft.output[0].qty
                i.text(R.id.craftFleaFea, (-abs(tax)).getPrice("₽"))
                i.text(R.id.craftTotalProfit, (fleaItem.price * craft.output[0].qty - totalCostToCraft - tax).getPrice("₽"))
                i.text(R.id.craftProfitHour, ((fleaItem.price * craft.output[0].qty - totalCostToCraft - tax) / craft.time).roundToInt().getPrice("₽"))

                i.textColor(R.id.craftTotalProfit, if ((fleaItem.price * craft.output[0].qty - totalCostToCraft - tax) <= 0) redTextColor else greenTextColor)
                i.textColor(R.id.craftProfitHour, if (((fleaItem.price * craft.output[0].qty - totalCostToCraft - tax) / craft.time).roundToInt() <= 0) redTextColor else greenTextColor)
            }



        }.attachTo(mRecyclerView).updateData(viewModel.craftsList.value)
    }
}