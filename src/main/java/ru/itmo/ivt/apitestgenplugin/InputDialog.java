package ru.itmo.ivt.apitestgenplugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@Getter
public class InputDialog extends DialogWrapper {
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JButton выбратьButton;
    private JButton выбратьButton1;
    private JButton выбратьButton2;
    private JRadioButton нетRadioButton;
    private JRadioButton JWTRadioButton;
    private JRadioButton basicRadioButton;
    private JButton ОКButton;
    private JButton отменаButton;

    protected InputDialog(Project project) {
        super(project);
        init();
        setTitle("Generate API Tests");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(9, 7, -1, -1));
        mainPanel.setPreferredSize(new Dimension(400, 284));
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        mainPanel.add(Box.createHorizontalGlue());

        mainPanel.add(new JLabel("OpenAPI спецификация"));
        textField1 = new JTextField();
        textField1.setPreferredSize(new Dimension(160, -1));
        mainPanel.add(textField1);
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell
        выбратьButton = new JButton("Выбрать");
        mainPanel.add(выбратьButton);
        mainPanel.add(Box.createHorizontalGlue()); // Horizontal spacer at row 1, column 6

        // Row 2: Test configuration
        mainPanel.add(new JLabel("Конфигурация тестовых проверок"));
        textField2 = new JTextField();
        textField2.setPreferredSize(new Dimension(160, -1));
        mainPanel.add(textField2);
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell
        выбратьButton1 = new JButton("Выбрать");
        mainPanel.add(выбратьButton1);
        mainPanel.add(new JLabel()); // Empty cell

        // Row 3: Data model configuration
        mainPanel.add(new JLabel("Конфигурация модели данных"));
        textField3 = new JTextField();
        textField3.setPreferredSize(new Dimension(160, -1));
        mainPanel.add(textField3);
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell
        выбратьButton2 = new JButton("Выбрать");
        mainPanel.add(выбратьButton2);
        mainPanel.add(new JLabel()); // Empty cell

        // Row 4: Base URL
        mainPanel.add(new JLabel("Базовый URL"));
        textField4 = new JTextField();
        textField4.setPreferredSize(new Dimension(160, -1));
        mainPanel.add(textField4);
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell

        // Row 5: Authentication
        mainPanel.add(new JLabel("Аутентификация"));
        нетRadioButton = new JRadioButton("Нет");
        JWTRadioButton = new JRadioButton("JWT");
        basicRadioButton = new JRadioButton("Basic");

        // Group the radio buttons
        ButtonGroup authGroup = new ButtonGroup();
        authGroup.add(нетRadioButton);
        authGroup.add(JWTRadioButton);
        authGroup.add(basicRadioButton);
        нетRadioButton.setSelected(true);

        mainPanel.add(нетRadioButton);
        mainPanel.add(JWTRadioButton);
        mainPanel.add(basicRadioButton);
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell

        // Row 6: Empty row with vertical spacer
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(Box.createVerticalGlue()); // Vertical spacer
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell

        // Row 7: Buttons
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell
        отменаButton = new JButton("Отмена");
        mainPanel.add(отменаButton);
        ОКButton = new JButton("ОК");
        mainPanel.add(ОКButton);
        mainPanel.add(new JLabel()); // Empty cell

        // Row 8: Empty row with vertical spacer
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(Box.createVerticalGlue()); // Vertical spacer
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell
        mainPanel.add(new JLabel()); // Empty cell

        return mainPanel;
    }
}