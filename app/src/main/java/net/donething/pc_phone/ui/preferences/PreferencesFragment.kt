package net.donething.pc_phone.ui.preferences

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R
import net.donething.pc_phone.database.MyDatabase
import net.donething.pc_phone.database.PreferenceEntity

/**
 * 选项
 */
class PreferencesFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
    private val itag = this::class.simpleName

    private val viewModel by viewModels<PreferenceViewModel>()

    /**
     * 快速实现选择文件
     */
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { restoreFromUri(it) }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_fragment, rootKey)

        // 备份、恢复按钮
        findPreference<Preference>("backup_start")?.setOnPreferenceClickListener {
            try {
                viewModel.backupDB(requireContext())
            } catch (e: Exception) {
                Log.e(itag, "onCreatePreferences: 备份数据出错", e)
                Toast.makeText(requireContext(), "备份数据出错：${e}", Toast.LENGTH_LONG).show()
            }

            return@setOnPreferenceClickListener true
        }

        findPreference<Preference>("backup_restore")?.setOnPreferenceClickListener {
            // 启动文件选择器，以恢复数据
            getContent.launch("*/*")

            return@setOnPreferenceClickListener true
        }
    }

    /**
     * 恢复数据
     */
    private fun restoreFromUri(uri: Uri) {
        // 使用 ContentResolver 获取 InputStream 并进行处理
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
        if (inputStream == null) {
            Log.e(itag, "RestoreFromUri: 恢复数据失败：不能打开输入流")
            Toast.makeText(requireContext(), "恢复数据失败：不能打开输入流", Toast.LENGTH_LONG).show()
            return
        }

        try {
            MyDatabase.restoreDatabase(MyApp.ctx, inputStream)
        } catch (e: Exception) {
            Log.e(itag, "restoreFromUri: 恢复数据出错", e)
            Toast.makeText(requireContext(), "恢复数据出错：${e}", Toast.LENGTH_LONG).show()
        }

        Toast.makeText(requireContext(), "已恢复数据文件，将重启应用", Toast.LENGTH_LONG).show()

        // 重启应用
        restart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.preference.observe(viewLifecycleOwner) { preference ->
            // 使用 preference 更新 UI
            findPreference<EditTextPreference>("security_auth")!!.text = preference.securityAuth
            findPreference<EditTextPreference>("pc_lan_ip")!!.text = preference.pcLanIP
            findPreference<EditTextPreference>("pc_server_port")!!.text = preference.pcServerPort
            findPreference<EditTextPreference>("pc_mac")!!.text = preference.pcMAC
        }
    }

    /**
     * 选项值变化时，同步保存到数据库
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val prefs = preferenceManager.sharedPreferences!!
        // 先判断是否存在该键
        prefs.all[key] ?: run {
            Toast.makeText(context, "无法保存选项：'${key}'的值为空", Toast.LENGTH_LONG).show()
            return
        }

        if (prefs.getString(key, "").isNullOrBlank()) {
            return
        }

        // 设置对应选项的值
        val currentPreference: PreferenceEntity = viewModel.preference.value!!
        when (key) {
            "security_auth" -> currentPreference.securityAuth = prefs.getString(key, "").toString()
            "pc_lan_ip" -> currentPreference.pcLanIP = prefs.getString(key, "").toString()
            "pc_server_port" -> currentPreference.pcServerPort = prefs.getString(key, "").toString()
            "pc_mac" -> currentPreference.pcMAC = prefs.getString(key, "").toString()
        }

        // 在界面显示
        viewModel.preference.postValue(currentPreference)
        // 保存到数据库
        viewModel.updatePreference(currentPreference)

        Toast.makeText(context, "已保存选项 '${key}'", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()

        preferenceManager.sharedPreferences!!.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()

        preferenceScreen.sharedPreferences!!.unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * 恢复数据后，重启应用
     */
    private fun restart() {
        val intent = requireActivity().packageManager.getLaunchIntentForPackage(requireActivity().packageName)
        intent ?: return

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

        //杀掉以前进程
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
