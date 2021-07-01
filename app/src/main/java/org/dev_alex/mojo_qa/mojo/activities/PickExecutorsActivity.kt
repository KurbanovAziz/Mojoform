package org.dev_alex.mojo_qa.mojo.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.dev_alex.mojo_qa.mojo.R
import org.dev_alex.mojo_qa.mojo.fragments.create_task.PickExecutorsFragment
import org.dev_alex.mojo_qa.mojo.models.OrgUser
import org.dev_alex.mojo_qa.mojo.models.OrgUserGroup

class PickExecutorsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val data = (intent.getSerializableExtra(ARG_PRESET_DATA) as? PickExecutorsFragment.PreSetData) ?: return

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.container, PickExecutorsFragment.newInstance(
                    data.selectedUsers, data.selectedGroups
                )
            )
            .commit()
    }

    companion object {
        private const val ARG_PRESET_DATA = "arg preset data"

        fun getActivityIntent(context: Context?, selectedUsers: List<OrgUser>, selectedGroups: List<OrgUserGroup>): Intent {
            val data = PickExecutorsFragment.PreSetData(selectedUsers, selectedGroups)

            val intent = Intent(context, PickExecutorsActivity::class.java)
            intent.putExtra(ARG_PRESET_DATA, data)
            return intent
        }
    }
}