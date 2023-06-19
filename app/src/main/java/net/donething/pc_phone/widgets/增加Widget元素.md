# 增加 Widget 元素

1. 在`widget_layout.xml`中增加新的组件
2. 在`MyWidget.kt`的`onUpdate`方法中，创建新组件的 `Intent`（新建并设置常量`action`）、`PendingIntent`，绑定组件和`PendingIntent`
3. 在`tasks`目录中，添加相应的代码文件，编写单实例对象`object Example {}`。需实现`ITask`抽象类，实现其`doTask`方法，完成所需的功能
4. 在`WidgetService.kt`的`onStartCommand`方法的`when`语句中，增加分支，指向上个步骤的单实例对象
