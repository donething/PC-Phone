# 添加 shortcut

# 创建 shortcut 的 id

先在`values/strings.xml`中创建该快捷方式的 id 值

```xml
<string name="shortcut_id_wakeup_pc">shortcut_wakeup_pc</string>
```

# 创建 shortcut 快捷菜单项

在`xml/shortcuts.xml`文件中创建新的`shortcut`快捷菜单项

注意：

1. shortcutId 不能通过'@string/id'的型式指定为第一步的 id（长按图标将不显示该快捷菜单），但需要设为和其一致
2. 其中的`intent`的`extra`属性中，`name`必须为`id`，`value`的值需和`shortcutId`一致

```xml
<shortcuts xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- shortcutId 不能通过'@string/shortcut_wakeup_pc_id'的型式指定，但需要设为和其一致 -->
    <shortcut
        android:enabled="true"
        android:icon="@drawable/ic_shortcut_wakeup_pc"
        android:shortcutId="shortcut_wakeup_pc"
        android:shortcutShortLabel="@string/shortcut_label_wakeup_pc_short">
        <intent
            android:action="android.intent.action.VIEW"
            android:targetClass="net.donething.pc_phone.ShortcutActivity"
            android:targetPackage="net.donething.pc_phone">
            <!-- 传递的数据：name 必须为"id"，value 必须和上面的 android:shortcutId 一致 -->
            <extra
                android:name="id"
                android:value="shortcut_wakeup_pc" />
        </intent>
    </shortcut>
</shortcuts>
```
# 编写新的 shortcut 对应的功能代码

在`shortcuts`目录下创建代码文件，创建类，需实现`ITask`抽象类

# 匹配 shortcutId 和功能代码

参加 [增加任务新.md](%E5%A2%9E%E5%8A%A0%E4%BB%BB%E5%8A%A1%E6%96%B0.md)
