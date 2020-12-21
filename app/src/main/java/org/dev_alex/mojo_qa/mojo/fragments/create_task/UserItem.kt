package org.dev_alex.mojo_qa.mojo.fragments.create_task

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.card_org_user.view.*
import org.dev_alex.mojo_qa.mojo.App
import org.dev_alex.mojo_qa.mojo.R
import org.dev_alex.mojo_qa.mojo.models.OrgUser

open class UserItem(val user: OrgUser, private val delegate: OrgUserDelegate, private val fromGroup: Boolean) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvUserName.text = user.firstName.takeIf { it.isNotBlank() } ?: user.fullname
        viewHolder.itemView.vUserContainer.setOnClickListener { delegate.onUserClick(user) }

        viewHolder.itemView.ivUserIcon.layoutParams?.apply {
            if (fromGroup) {
                (this as? ViewGroup.MarginLayoutParams)?.marginStart = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, App.displayMetrics).toInt()
            } else {
                (this as? ViewGroup.MarginLayoutParams)?.marginStart = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, App.displayMetrics).toInt()
            }
        }

        if (delegate.isUserSelected(user)) {
            viewHolder.itemView.ivSelectedTick.visibility = View.VISIBLE
            viewHolder.itemView.vUserContainer.setBackgroundResource(R.drawable.bg_org_user_selected)
        } else {
            viewHolder.itemView.ivSelectedTick.visibility = View.INVISIBLE
            viewHolder.itemView.vUserContainer.background = null
        }
    }

    override fun getLayout(): Int = R.layout.card_org_user

    interface OrgUserDelegate {
        fun onUserClick(user: OrgUser)
        fun isUserSelected(user: OrgUser): Boolean
    }
}