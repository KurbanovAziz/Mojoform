package org.dev_alex.mojo_qa.mojo.fragments.create_task

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_pick_executors.*
import org.dev_alex.mojo_qa.mojo.R
import org.dev_alex.mojo_qa.mojo.event.ExecutorsPickedEvent
import org.dev_alex.mojo_qa.mojo.models.Org
import org.dev_alex.mojo_qa.mojo.models.OrgUser
import org.dev_alex.mojo_qa.mojo.models.OrgUserGroup
import org.dev_alex.mojo_qa.mojo.models.response.OrgUsersAvailable
import org.dev_alex.mojo_qa.mojo.models.response.OrgUsersResponse
import org.dev_alex.mojo_qa.mojo.services.RequestService
import org.dev_alex.mojo_qa.mojo.services.Utils
import org.greenrobot.eventbus.EventBus
import java.io.Serializable


@Suppress("DEPRECATION")
class PickExecutorsFragment : Fragment() {
    private var loopDialog: ProgressDialog? = null

    private var loadDisposable: Disposable? = null

    private var selectedFilterOrgId: Int? = null
    private var selectedFilterGroupId: Int? = null

    val selectedUsers: MutableList<OrgUser> = ArrayList()
    val selectedGroups: MutableList<OrgUserGroup> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_pick_executors, container, false)

        Utils.setupCloseKeyboardUI(activity, rootView)
        initDialog()
        setupHeader()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (arguments?.getSerializable(ARG_PRESET_DATA) as? PreSetData)?.let {
            selectedUsers.clear()
            selectedUsers.addAll(it.selectedUsers)

            selectedGroups.clear()
            selectedGroups.addAll(it.selectedGroups)
        }

        loadUsers()

        btSelect.setOnClickListener {
            if (selectedUsers.isEmpty()) {
                Toast.makeText(context, getString(R.string.need_to_select_at_least_one_executor), Toast.LENGTH_SHORT).show()
            } else {
                activity?.finish()
                EventBus.getDefault().post(ExecutorsPickedEvent(selectedUsers, selectedGroups))
            }
        }

        btExit.setOnClickListener {
            activity?.finish()
        }
    }

    private fun setupHeader() {
        (activity?.findViewById<View>(R.id.title) as TextView).text = getString(R.string.assignments)
        activity?.findViewById<View>(R.id.back_btn)?.visibility = View.VISIBLE
        activity?.findViewById<View>(R.id.grid_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.sandwich_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.group_by_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.search_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.notification_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.qr_btn)?.visibility = View.GONE

        activity?.findViewById<View>(R.id.back_btn)?.setOnClickListener {
            activity?.finish()
        }
    }

    private fun initDialog() {
        loopDialog = ProgressDialog(context, R.style.ProgressDialogStyle).apply {
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setMessage(getString(R.string.loading_please_wait))
            isIndeterminate = true
            setCanceledOnTouchOutside(false)
            setCancelable(false)
        }
    }

    private fun loadUsers() {
        loadDisposable?.dispose()

        loopDialog?.show()
        loadDisposable = Observable.create<OrgUsersResponse> {
            val url = "/api/users"

            var needStop = false
            val totalUsers = ArrayList<OrgUser>()
            val totalGroups = ArrayList<OrgUserGroup>()
            val orgs = ArrayList<Org>()

            while (!needStop) {
                var finalUrl = url + "?size=50&offset=${totalUsers.size}"
                if (selectedFilterOrgId != null) {
                    finalUrl += "&orgID=$selectedFilterOrgId"
                }
                if (selectedFilterGroupId != null) {
                    finalUrl += "&groupID=$selectedFilterGroupId"
                }

                val response = RequestService.createGetRequest(finalUrl)

                if (response.code == 200) {
                    val responseJson = response.body?.string() ?: "{}"
                    val responseData = Gson().fromJson(responseJson, OrgUsersResponse::class.java)

                    totalUsers.addAll(responseData?.users.orEmpty())
                    totalGroups.addAll(responseData?.groups.orEmpty())
                    totalGroups.addAll(responseData?.available?.group.orEmpty())

                    orgs.addAll(responseData?.available?.organisation.orEmpty())

                    if (responseData?.users.isNullOrEmpty()) {
                        needStop = true
                    }
                } else {
                    it.onError(Exception("code = ${response.code}"))
                    needStop = true
                }
            }
            it.onNext(
                OrgUsersResponse(
                    OrgUsersAvailable(orgs.distinctBy { it.id }, totalGroups.distinctBy { it.id }),
                    totalGroups.distinctBy { it.id },
                    totalUsers.distinctBy { it.id })
            )
            it.onComplete()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                showUsers(it)
                loopDialog?.dismiss()
            }, {
                loopDialog?.dismiss()
                it.printStackTrace()
            })
    }

    private fun showUsers(response: OrgUsersResponse) {
        val adapter = GroupAdapter<GroupieViewHolder>()
        rvExecutors.adapter = adapter

        val selectionUserListener = object : UserItem.OrgUserDelegate {
            override fun onUserClick(user: OrgUser) {
                if (selectedUsers.contains(user)) {
                    selectedUsers.remove(user)
                } else {
                    selectedUsers.add(user)
                }
                adapter.notifyDataSetChanged()
            }

            override fun isUserSelected(user: OrgUser): Boolean {
                return selectedUsers.contains(user)
            }
        }

        val selectionGroupListener = object : UserGroupItem.UserGroupDelegate {
            override fun onGroupClick(group: OrgUserGroup) {
                if (selectedGroups.contains(group)) {
                    selectedGroups.remove(group)
                } else {
                    selectedGroups.add(group)
                }
                adapter.notifyDataSetChanged()
            }

            override fun isGroupSelected(group: OrgUserGroup): Boolean {
                return selectedGroups.contains(group)
            }
        }

        //adapter.addAll(response.groups?.map { UserGroupItem(it, selectionGroupListener) }.orEmpty())
        adapter.addAll(response.users.map { UserItem(it, selectionUserListener, false) })

        if (spOrg.getItems<Any>().isNullOrEmpty()) {
            showOrgsFilter(response.available?.organisation.orEmpty())
        }
        showGroupsFilter(response.available?.group.orEmpty())
    }

    fun showOrgsFilter(orgs: List<Org>) {
        val orgList = orgs.map { it.name }.toMutableList()
        orgList.add(0, getString(R.string.organizations))

        spOrg.setItems(orgList)
        spOrg.setOnItemSelectedListener { _, position, _, org ->
            if (position > 0) {
                selectedFilterOrgId = orgs[position - 1].id
                selectedFilterGroupId = null
            } else {
                selectedFilterOrgId = null
                selectedFilterGroupId = null
            }
            loadUsers()
        }
    }

    fun showGroupsFilter(groups: List<OrgUserGroup>) {
        val groupList = groups.map { it.name }.toMutableList()
        groupList.add(0, getString(R.string.groups))

        spGroup.setItems(groupList)
        spGroup.setOnItemSelectedListener { _, position, _, org ->
            selectedFilterGroupId = if (position > 0) {
                groups[position - 1].id
            } else {
                null
            }
            loadUsers()
        }
        spGroup.selectedIndex = groups.indexOfFirst { it.id == selectedFilterGroupId } + 1
    }

    override fun onDestroyView() {
        loadDisposable?.dispose()
        loopDialog?.dismiss()
        super.onDestroyView()
    }

    companion object {
        private const val ARG_PRESET_DATA = "arg preset data"

        @JvmStatic
        fun newInstance(selectedUsers: List<OrgUser>, selectedGroups: List<OrgUserGroup>): PickExecutorsFragment {
            val data = PreSetData(selectedUsers, selectedGroups)
            return PickExecutorsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PRESET_DATA, data)
                }
            }
        }
    }

    class PreSetData(
        val selectedUsers: List<OrgUser>,
        val selectedGroups: List<OrgUserGroup>
    ) : Serializable
}