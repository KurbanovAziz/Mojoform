package org.dev_alex.mojo_qa.mojo.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.models.Panel;
import org.dev_alex.mojo_qa.mojo.services.Utils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GraphListFragment extends Fragment {
    private View rootView = null;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private Panel panel;

    public static GraphListFragment newInstance(Panel panel) {
        Bundle args = new Bundle();
        args.putSerializable("panel", panel);

        GraphListFragment fragment = new GraphListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        panel = (Panel) getArguments().getSerializable("panel");
        setupHeader();

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_graphs, container, false);
            setListeners();
            Utils.setupCloseKeyboardUI(getActivity(), rootView);


            viewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
            setupViewPager();
            tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
            tabLayout.setupWithViewPager(viewPager);
        }
        return rootView;
    }


    private void setListeners() {

    }

    private void setupHeader() {
        ((TextView) getActivity().findViewById(R.id.title)).setText(panel.name);
        getActivity().findViewById(R.id.back_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        getActivity().findViewById(R.id.grid_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.group_by_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.search_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.notification_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.qr_btn).setVisibility(View.GONE);
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());

        boolean isPercents = true;
        try {
            if (panel.config != null)
                isPercents = new JSONObject(panel.config).getInt("dataType") == 2;
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        adapter.addFragment(GraphFragment.newInstance(GraphFragment.DAY, panel.id, isPercents), "День");
        adapter.addFragment(GraphFragment.newInstance(GraphFragment.WEEK, panel.id, isPercents), "Неделя");
        adapter.addFragment(GraphFragment.newInstance(GraphFragment.MONTH, panel.id, isPercents), "Месяц");
        adapter.addFragment(GraphFragment.newInstance(GraphFragment.YEAR, panel.id, isPercents), "Год");
        viewPager.setAdapter(adapter);
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

     /*   @Override
        public long getItemId(int position) {
            return new Date().getTime();
        }*/

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
