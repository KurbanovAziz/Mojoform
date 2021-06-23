package org.dev_alex.mojo_qa.mojo.fragments.create_task

import android.view.View
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.card_org.view.*
import org.dev_alex.mojo_qa.mojo.R
import org.dev_alex.mojo_qa.mojo.models.OrgUserGroup

open class UserGroupItem(val group: OrgUserGroup, private val delegate: UserGroupDelegate) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvOrgName.text = group.name
        viewHolder.itemView.vOrgContainer.setOnClickListener { delegate.onGroupClick(group) }

        if (delegate.isGroupSelected(group)) {
            viewHolder.itemView.ivSelectedTick.visibility = View.VISIBLE
            viewHolder.itemView.vOrgContainer.setBackgroundResource(R.drawable.bg_org_user_selected)
        } else {
            viewHolder.itemView.ivSelectedTick.visibility = View.INVISIBLE
            viewHolder.itemView.vOrgContainer.background = null
        }
    }

    override fun getLayout(): Int = R.layout.card_org

    interface UserGroupDelegate {
        fun onGroupClick(group: OrgUserGroup)
        fun isGroupSelected(group: OrgUserGroup): Boolean
    }
}