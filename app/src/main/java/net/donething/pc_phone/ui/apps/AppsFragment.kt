package net.donething.pc_phone.ui.apps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.donething.pc_phone.R
import net.donething.pc_phone.databinding.FragmentAppsBinding

// 应用
class AppsFragment : Fragment() {
    private val itag = this::class.simpleName

    private var _binding: FragmentAppsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel by viewModels<AppsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAppsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val bnBackupApps: Button = binding.bnBackupApps
        bnBackupApps.setOnClickListener {
            Toast.makeText(context, "开始备份用户应用的信息", Toast.LENGTH_LONG).show()
            viewModel.backupAppsIntoDatabase()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AppEntityAdapter(this)
        val recyclerView = view.findViewById<RecyclerView>(R.id.appListRecyclerView)
        // RecyclerView 必需一个 LayoutManager 来管理其子视图的布局。如果没有设置，它将无法显示任何内容
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.allApps.observe(viewLifecycleOwner) { it.let { adapter.submitList(it) } }

        // 观察LiveData以更新UI
        viewModel.filteredApps.observe(viewLifecycleOwner) { adapter.submitList(it) }
        // 设置开关监听器来更新是否显示预装应用
        binding.swAppDisPreinstall.apply {
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.setSwPreinstall(isChecked)
            }
        }
    }
}
