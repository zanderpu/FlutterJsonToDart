package cn.dxy.app.dxyjsontodart;

import cn.dxy.app.dxyjsontodart.setting.FlutterJsonToDartSetting;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFileFactory;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.psi.DartFile;
import org.jetbrains.annotations.NotNull;


public class FlutterJsonToDartAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        if (project == null) {
            return;
        }

        PsiDirectory directory = getSelectedDirectory(event);
        if (directory == null) {
            return;
        }

        InputJsonDialog dialog = new InputJsonDialog(project);
        dialog.show();

        String inputClassName = dialog.getClassName();
        String inputJsonStr = dialog.getJsonText();

        if (inputClassName == null || inputClassName.isEmpty()){
            return;
        }
        if (inputJsonStr == null || inputJsonStr.isEmpty()) {
            return;
        }
        FlutterJsonToDartSetting instance = FlutterJsonToDartSetting.getInstance();
        String generatorClassContent = JsonHelper.generateDartClassesToString(inputClassName, inputJsonStr, instance);

        CommandProcessor.getInstance().executeCommand(project, () -> {
            ApplicationManager.getApplication().runWriteAction(() -> {
                String fileName = StringUtils.getFileName(inputClassName);
                PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
                DartFile file = (DartFile) psiFileFactory.createFileFromText(fileName + ".dart", DartFileType.INSTANCE, generatorClassContent);
                directory.add(file);
            });
        }, "FlutterJsonToDart", "FlutterJsonToDart");

        showNotify(project, "Dart Data Class file generated successful");
        CommandUtil.runFlutterPubRun(event);
    }

    private PsiDirectory getSelectedDirectory(@NotNull AnActionEvent event) {
        Navigatable data = LangDataKeys.NAVIGATABLE.getData(event.getDataContext());
        if (data instanceof PsiDirectory) {
            return (PsiDirectory) data;
        }
        return null;
    }

    private void showNotify(Project project, String content) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("FlutterJsonToDart")
                .createNotification(content, NotificationType.INFORMATION)
                .notify(project);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }
}
