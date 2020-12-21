package org.dev_alex.mojo_qa.mojo.fragments.create_task

import android.animation.ObjectAnimator
import android.view.View
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.card_org_view.view.*
import org.dev_alex.mojo_qa.mojo.R
import org.dev_alex.mojo_qa.mojo.models.OrgUserGroup


open class UserGroupItem(val group: OrgUserGroup) : Item(), ExpandableItem {
    private var expandableGroup: ExpandableGroup? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvGroupName.text = group.name
        rotateExpandableItemIfNeeded(viewHolder.itemView.ivExpandableIcon)

        viewHolder.itemView.setOnClickListener {
            expandableGroup?.onToggleExpanded()
            rotateExpandableItemIfNeeded(viewHolder.itemView.ivExpandableIcon)
        }
    }

    private fun rotateExpandableItemIfNeeded(item: View) {
        val destAngle = if (expandableGroup?.isExpanded == true) 90f else 0f
        if (item.rotation != destAngle) {
            ObjectAnimator.ofFloat(item, View.ROTATION, destAngle).apply {
                setAutoCancel(true)
                duration = 400
            }.start()
        }
    }

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        expandableGroup = onToggleListener
    }

    override fun getLayout(): Int = R.layout.card_org_view
}