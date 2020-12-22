package org.dev_alex.mojo_qa.mojo.fragments.create_task

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_select_task_executors.*
import org.dev_alex.mojo_qa.mojo.CreateTaskModel
import org.dev_alex.mojo_qa.mojo.R
import org.dev_alex.mojo_qa.mojo.models.OrgUser
import org.dev_alex.mojo_qa.mojo.models.response.OrgUsersResponse
import org.dev_alex.mojo_qa.mojo.services.RequestService
import org.dev_alex.mojo_qa.mojo.services.Utils


@Suppress("DEPRECATION")
class SelectTaskExecutorsFragment : Fragment() {
    private var loopDialog: ProgressDialog? = null

    private val model: CreateTaskModel
        get() = CreateTaskModel.instance!!

    private var loadDisposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_select_task_executors, container, false)

        Utils.setupCloseKeyboardUI(activity, rootView)
        initDialog()
        setupHeader()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUsers()

        btSelectRules.setOnClickListener {
            showNextFragment(SelectTaskRulesFragment.newInstance())
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
            activity?.supportFragmentManager?.popBackStack()
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
            val url = "/api/orgs/${model.orgId.orEmpty()}/users"
            val response = RequestService.createGetRequest(url)

            if (response.code == 200) {
                val responseJson = response.body?.string() ?: "{}"
                val responseData = Gson().fromJson(responseJson, OrgUsersResponse::class.java)
                it.onNext(responseData)
                it.onComplete()
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showUsers(it)
                    model.saveUsers(it)
                    loopDialog?.dismiss()
                }, {
                    loopDialog?.dismiss()
                    it.printStackTrace()
                })
    }

    private fun showUsers(response: OrgUsersResponse) {
        val adapter = GroupAdapter<GroupieViewHolder>()
        rvExecutors.adapter = adapter

        val selectionListener = object : UserItem.OrgUserDelegate {
            override fun onUserClick(user: OrgUser) {
                if (model.selectedUsers.contains(user)) {
                    model.selectedUsers.remove(user)
                } else {
                    model.selectedUsers.add(user)
                }
                adapter.notifyDataSetChanged()
            }

            override fun isUserSelected(user: OrgUser): Boolean {
                return model.selectedUsers.contains(user)
            }
        }

        response.groups.forEach { group ->
            val expandableGroup = ExpandableGroup(UserGroupItem(group), false).apply {
                val childrenItems = group.users.map { UserItem(it, selectionListener, true) }
                add(Section(childrenItems))
            }
            adapter.add(expandableGroup)
        }

        adapter.addAll(response.users.map { UserItem(it, selectionListener, false) })
    }

    private fun showNextFragment(fragment: Fragment) {
        activity
                ?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.container, fragment)
                ?.addToBackStack(null)
                ?.commit()
    }

    override fun onDestroyView() {
        loadDisposable?.dispose()
        loopDialog?.dismiss()
        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        fun newInstance(): SelectTaskExecutorsFragment {
            return SelectTaskExecutorsFragment()
        }
    }
}