package ru.itmo.ivt.apitestgenplugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import ru.itmo.ivt.apitestgenplugin.model.enums.AuthType;
import ru.itmo.ivt.apitestgenplugin.model.UserInput;

import javax.swing.*;
import java.awt.*;

@Getter
public class InputDialog extends DialogWrapper {
    private final JTextField openApiSpecTextField = new JTextField();
    private final JTextField testConfigField = new JTextField();
    private final JTextField dataConfigField = new JTextField();
    private final JTextField baseUrlField = new JTextField();
    private final JButton openApiSpecButton = new JButton("Выбрать");
    private final JButton testConfigButton = new JButton("Выбрать");
    private final JButton dataConfigButton = new JButton("Выбрать");
    private final JRadioButton noAuthRadioButton = new JRadioButton("Нет");
    private final JRadioButton tokenAuthRadioButton = new JRadioButton("JWT");
    private final JRadioButton basicAuthRadioButton = new JRadioButton("Basic");

    protected InputDialog(Project project) {
        super(project);
        init();
        setTitle("Generate API Tests");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(6, 4, -1, -1));
        mainPanel.setPreferredSize(new Dimension(400, 284));
        mainPanel.setBorder(JBUI.Borders.empty());

        mainPanel.add(new JLabel("OpenAPI спецификация"));
        openApiSpecTextField.setPreferredSize(new Dimension(50, -1));
        mainPanel.add(openApiSpecTextField);
        mainPanel.add(openApiSpecButton);
        mainPanel.add(new Label());

        mainPanel.add(new JLabel("Конфигурация тестов"));
        testConfigField.setPreferredSize(new Dimension(50, -1));
        mainPanel.add(testConfigField);
        mainPanel.add(testConfigButton);
        mainPanel.add(new Label());

        mainPanel.add(new JLabel("Конфигурация модели данных"));
        dataConfigField.setPreferredSize(new Dimension(50, -1));
        mainPanel.add(dataConfigField);
        mainPanel.add(dataConfigButton);
        mainPanel.add(new Label());

        mainPanel.add(new JLabel("Базовый URL"));
        baseUrlField.setPreferredSize(new Dimension(50, -1));
        mainPanel.add(baseUrlField);
        mainPanel.add(new Label());
        mainPanel.add(new Label());

        mainPanel.add(new JLabel("Аутентификация"));
        ButtonGroup authGroup = new ButtonGroup();
        authGroup.add(noAuthRadioButton);
        authGroup.add(tokenAuthRadioButton);
        authGroup.add(basicAuthRadioButton);
        noAuthRadioButton.setSelected(true);

        mainPanel.add(noAuthRadioButton);
        mainPanel.add(tokenAuthRadioButton);
        mainPanel.add(basicAuthRadioButton);

        mainPanel.add(new Label());
        mainPanel.add(new Label());
        mainPanel.add(new Label());
        mainPanel.add(new Label());

        return mainPanel;
    }

    protected UserInput getUserInput() {
        return UserInput.builder()
                .openApiPath(openApiSpecTextField.getText())
                .dataConfigPath(dataConfigField.getText())
                .testConfigPath(testConfigField.getText())
                .authType(noAuthRadioButton.isSelected() ? AuthType.NO : basicAuthRadioButton.isSelected() ? AuthType.BASIC : AuthType.TOKEN)
                .baseUrl(baseUrlField.getText())
                .build();
    }
}