package brig.concord.actions;

import brig.concord.ConcordBundle;
import brig.concord.ConcordUtils;
import brig.concord.log.Logger;
import brig.concord.psi.FileUtils;
import brig.concord.run.ConcordCliRunConfiguration;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLLanguage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShowEffectiveYamlAction extends AnAction implements DumbAware {

    private static final Logger log = Logger.getInstance(ShowEffectiveYamlAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = ConcordActionUtil.getProject(event.getDataContext());
        if (project == null) {
            return;
        }

        VirtualFile root = findRootDir(event.getDataContext());
        if (root == null) {
            return;
        }

        Sdk sdk = ConcordCliRunConfiguration.defaultSdk(project).orElse(null);
        if (sdk == null) {
            return;
        }

        Sdk jre = ConcordCliRunConfiguration.defaultJre(project).orElse(null);
        if (jre == null) {
            return;
        }

        String effectiveYamlText = ConcordUtils.computeInBackground(project, ConcordBundle.message("effective.yaml.generate.task.title"), progressIndicator -> {
            String executable = sdk.getHomePath() + "/" + "concord-cli.jar";

            GeneralCommandLine commandLine = new GeneralCommandLine()
                    .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
                    .withExePath(JavaSdk.getInstance().getVMExecutablePath(jre))
                    .withParameters("-jar")
                    .withParameters(executable)
                    .withParameters("run")
                    .withParameters("-effective-yaml")
                    .withRedirectErrorStream(true);

            commandLine.setWorkDirectory(root.getPath());
            StringBuilder result = new StringBuilder();
            try {
                Process p = commandLine.createProcess();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }

                int exitCode = p.waitFor();
                if (exitCode != 0) {
                    log.error("execute error: code: {}, error: {}", exitCode, result.toString());
                    return null;
                }

                return result.toString();
            } catch (Exception e) {
                log.error("execute error: ", e);
                return null;
            }
        });

        if (effectiveYamlText == null) {
            new Notification(ConcordUtils.NOTIFICATION_GROUP,
                    ConcordBundle.message("effective.yaml.failed.title"),
                    ConcordBundle.message("effective.yaml.failed"),
                    NotificationType.ERROR).notify(project);
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) {
                return;
            }

            String fileName = "effective.concord.yml";
            PsiFile f = PsiFileFactory.getInstance(project).createFileFromText(fileName, YAMLLanguage.INSTANCE, effectiveYamlText);
            try {
                f.getVirtualFile().setWritable(false);
            } catch (IOException e) {
                log.error("error: ", e);
            }
            f.navigate(true);
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation p = e.getPresentation();

        boolean visible = findRootDir(e.getDataContext()) != null;
        p.setVisible(visible);
    }

    @Nullable
    private static VirtualFile findRootDir(@NotNull DataContext dataContext) {
        VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
        if (file == null) {
            return null;
        }

        return FileUtils.getRootYamlDir(ConcordActionUtil.getProject(dataContext), file);
    }
}
