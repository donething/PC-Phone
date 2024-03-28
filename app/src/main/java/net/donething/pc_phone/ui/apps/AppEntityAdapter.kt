package net.donething.pc_phone.ui.apps

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import net.donething.pc_phone.R
import net.donething.pc_phone.database.AppEntity


/**
 * 对未安装的应用增加透明度，以标识
 */
val AppEntity.alpha: Float
    get() = if (this.installed) 1.0f else 0.6f

/**
 * 应用列表适配器
 */
class AppEntityAdapter(private val fragment: AppsFragment) :
    ListAdapter<AppEntity, AppEntityAdapter.AppEntityViewHolder>(DIFF_CALLBACK) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AppEntity>() {
            override fun areItemsTheSame(oldItem: AppEntity, newItem: AppEntity): Boolean {
                return oldItem.packageName == newItem.packageName
            }

            override fun areContentsTheSame(oldItem: AppEntity, newItem: AppEntity): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppEntityViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.app_entity_item, parent, false)
        return AppEntityViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AppEntityViewHolder, position: Int) {
        val currentAppEntity = getItem(position)

        // 将ByteArray转换为Bitmap
        val bitmap = BitmapFactory.decodeByteArray(currentAppEntity.appIcon, 0, currentAppEntity.appIcon.size)
        holder.imageAppIcon.setImageBitmap(bitmap)
        holder.textAppName.text = currentAppEntity.appName
        holder.textPackageName.text = currentAppEntity.packageName
        holder.textVersionName.text = currentAppEntity.versionName

        // 根据应用的存在状态设置不同的透明度
        holder.apply {
            imageAppIcon.alpha = currentAppEntity.alpha
            textAppName.alpha = currentAppEntity.alpha
            textPackageName.alpha = currentAppEntity.alpha
            textVersionName.alpha = currentAppEntity.alpha
        }

        // 根据是否已备份设置不同的标识
        val color = if (currentAppEntity.backuped) R.color.apps_title_had_backuped else R.color.apps_title_no_backup
        holder.textAppName.setTextColor(fragment.requireActivity().getColor(color))
        // 预装应用增加删除线
        holder.textAppName.paint.isStrikeThruText = currentAppEntity.preInstalled

        // 点击打开应用市场以便安装
        holder.itemView.setOnClickListener {
            val appName = currentAppEntity.packageName
            try {
                // 使用 Uri.parse 跳转到应用在 Google Play 上的页面
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appName"))

                // 选中了使用 Google play 下载应用
                val swGooglePlay = fragment.view?.findViewById<SwitchMaterial>(R.id.sw_apps_on_googleplay)
                if (swGooglePlay?.isChecked == true) {
                    intent.setPackage("com.android.vending")
                }

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                it.context.startActivity(intent)
            } catch (anfe: ActivityNotFoundException) {
                // Google Play 商店可能未安装，改为在网页中打开
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appName")
                )

                it.context.startActivity(intent)
            }
        }
    }

    class AppEntityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageAppIcon: ImageView = itemView.findViewById(R.id.imageAppIcon)
        val textAppName: TextView = itemView.findViewById(R.id.textAppName)
        val textPackageName: TextView = itemView.findViewById(R.id.textPackageName)
        val textVersionName: TextView = itemView.findViewById(R.id.textVersionName)
    }
}
