import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

public class Frame extends JFrame {
    Toolkit kit = Toolkit.getDefaultToolkit();
    Dimension screenSize = kit.getScreenSize();
    int width = screenSize.width;
    int height = screenSize.height;
    //------------------Left Panel---------------------
    LeftPanel leftPanel;

    private class LeftPanel extends JPanel {
        //------------------User Information Panel---------------------
        UserInfoPanel userInfoPanel;

        private class UserInfoPanel extends JPanel {
            JPanel userNamePanel;
            JLabel userNameLabel;
            JLabel userName;
            JLabel department;

            public UserInfoPanel() {
                this.userNamePanel = new JPanel();
                this.userNameLabel = new JLabel("     Username:");
                this.userName = new JLabel("Ufuk Çelikkan");
                this.department = new JLabel("       Software Engineering");
                setLayout(new GridLayout(0, 1));
                add(this.userNamePanel);
                this.userNamePanel.add(this.userNameLabel);
                this.userNamePanel.add(this.userName);
                this.userNamePanel.setLayout(new GridLayout(0, 2));

                setBorder(BorderFactory.createTitledBorder("User"));
                this.department.setFont(this.department.getFont().deriveFont(Font.BOLD));
                add(this.department);
                setBorder(BorderFactory.createTitledBorder(""));
            }
        }

        //------------------Course List Panel---------------------
        CourseListPanel courseListPanel;

        private class CourseListPanel extends JPanel {
            DefaultListModel<String> listModel;
            JList<String> list;
            JScrollPane scrollPane;

            public CourseListPanel() {
                this.listModel = new DefaultListModel<>();
                this.list = new JList<>(this.listModel);
                this.scrollPane = new JScrollPane(this.list);

                this.list.setFixedCellHeight(75);
                this.list.setFixedCellWidth(width / 10);
                this.list.setVisibleRowCount(5);

                this.scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                setLayout(new BorderLayout());
                add(this.scrollPane, BorderLayout.CENTER);
                setBorder(BorderFactory.createTitledBorder(""));
            }
        }

        //------------------Button Panel---------------------
        ButtonPanel buttonPanel;

        private class ButtonPanel extends JPanel {
            JButton addButton;
            JButton removeButton;

            public ButtonPanel() {
                this.addButton = new JButton("Add Course");
                this.removeButton = new JButton("Remove Chosen Course");

                setLayout(new GridLayout(0, 1));
                add(this.addButton);
                add(this.removeButton);
                setBorder(BorderFactory.createTitledBorder(""));

                this.addButton.addActionListener(new ActionListener() {
                    final String[] SECourses = new String[]{"SE323", "SE321", "SE311", "SE375", "SE216",
                            "SE209", "SE322"};

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Object selectedCourse = JOptionPane.showInputDialog(null,
                                "Choose the course to add", "Add Course", JOptionPane.QUESTION_MESSAGE,
                                null, this.SECourses, this.SECourses[0]);
                        if (!courseListPanel.listModel.contains(selectedCourse)) {
                            courseListPanel.listModel.addElement(selectedCourse.toString());
                        } else {
                            JOptionPane.showMessageDialog(Frame.super.rootPane, "You already add this course");
                        }
                    }
                });
                this.removeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int selectedIndex = courseListPanel.list.getSelectedIndex();
                        if (selectedIndex != -1) {
                            courseListPanel.listModel.remove(selectedIndex);
                            rightPanel.setVisible(false);
                        } else {
                            JOptionPane.showMessageDialog(Frame.super.rootPane, "Choose a course to remove");
                        }
                    }
                });
                courseListPanel.list.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        String selectedCourse = courseListPanel.list.getSelectedValue();
                        if (selectedCourse != null) {
                            rightPanel.courseNamePanel.updateCourseName(selectedCourse);
                            rightPanel.setVisible(true);
                        }
                    }
                });
            }
        }

        public LeftPanel() {
            setLayout(new BorderLayout());
            this.userInfoPanel = new UserInfoPanel();
            this.userInfoPanel.setPreferredSize(new Dimension(width / 32 * 4, (height / 16 * 2)));
            this.courseListPanel = new CourseListPanel();
            this.courseListPanel.setPreferredSize(new Dimension(width / 32 * 4, (height / 16 * 8)));
            this.buttonPanel = new ButtonPanel();
            this.buttonPanel.setPreferredSize(new Dimension(width / 32 * 4, (height / 16 * 2)));
            add(this.userInfoPanel, BorderLayout.NORTH);
            add(this.courseListPanel, BorderLayout.CENTER);
            add(this.buttonPanel, BorderLayout.SOUTH);
        }
    }

    //------------------Right Panel---------------------
    RightPanel rightPanel;

    private class RightPanel extends JPanel {
        //------------------Course Name Panel---------------------
        CourseNamePanel courseNamePanel;

        private class CourseNamePanel extends JPanel {
            JLabel courseNameLabel;

            public CourseNamePanel() {
                this.courseNameLabel = new JLabel();
                add(courseNameLabel);
            }

            public void updateCourseName(String courseName) {
                courseNameLabel.setText("Course Name: " + courseName);
            }
        }

        //------------------Course Information Panel---------------------
        CourseInfoPanel courseInfoPanel;

        private class CourseInfoPanel extends JPanel {
            //------------------Learning Outcome Panel---------------------
            LoPanel loPanel;

            private class LoPanel extends JPanel {
                public LoPanel() {
                    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                    setBorder(BorderFactory.createTitledBorder("Learning Outcomes"));
                    setPreferredSize(new Dimension(width / 32 * 20, (height / 32 * 9)));
                }
            }

            //------------------ Learning Outcome Panel ---------------------
            StudentPanel studentPanel;

            private class StudentPanel extends JPanel {
                JTabbedPane tabbedPane;
                ArrayList<JTable> tables = new ArrayList<>();
                ArrayList<StudentTableModel> tableModels = new ArrayList<>();
                public StudentPanel() {
                    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                    setBorder(BorderFactory.createTitledBorder("Students"));
                    setPreferredSize(new Dimension(width / 32 * 6, height / 32 * 15));

                    tabbedPane = new JTabbedPane();
                    add(tabbedPane, BorderLayout.CENTER);

                    JPanel buttonPanel = new JPanel(); //---- Button Panel
                    add(buttonPanel, BorderLayout.SOUTH);
                    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));

                    JButton addButton = new JButton(" + ");
                    addButton.setEnabled(false);
                    JButton removeButton = new JButton(" x ");
                    removeButton.setEnabled(false);
                    JButton seeButton = new JButton("See");
                    seeButton.setEnabled(false);
                    JButton addAllStudentsButton = new JButton("Add From csv File");
                    buttonPanel.add(addButton);
                    buttonPanel.add(removeButton);
                    buttonPanel.add(seeButton);
                    buttonPanel.add(addAllStudentsButton);

                    addAllStudentsButton.addActionListener(e -> {

                        JFrame popUpFrame = new JFrame("Add Students");

                        popUpFrame.setSize(285, 150);

                        JPanel container = new JPanel();
                        JButton fileSelector = new JButton("Select CSV File");
                        JButton cancel = new JButton("Cancel");
                        container.add(fileSelector);
                        container.add(cancel);
                        container.setLayout(new FlowLayout(FlowLayout.CENTER));

                        JLabel info = new JLabel("Please choose only \nUTF-8 formatted .csv files.");

                        popUpFrame.add(info);
                        popUpFrame.add(container);
                        popUpFrame.setLayout(new GridLayout(2, 0));

                        popUpFrame.setLocationRelativeTo(null);
                        popUpFrame.setVisible(true);
                        popUpFrame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

                        fileSelector.addActionListener(u -> {
                            JFileChooser fileChooser = new JFileChooser();

                            fileChooser.setFileFilter(new FileFilter() {
                                @Override
                                public boolean accept(File file) {
                                    return file.isDirectory() || file.getName().toLowerCase().endsWith(".csv");
                                }

                                @Override
                                public String getDescription() {
                                    return "CSV Files (*.csv)";
                                }
                            });
                            int result = fileChooser.showOpenDialog(popUpFrame);

                            if (result == JFileChooser.APPROVE_OPTION) {
                                File selectedFile = fileChooser.getSelectedFile();
                                JOptionPane.showMessageDialog(popUpFrame, "Selected File: " + selectedFile.getAbsolutePath());
                                HashMap<String, ArrayList<Object[]>> sectionData = readStudentsFromCSV(selectedFile.getAbsolutePath());

                                for (String section : sectionData.keySet()) {
                                    addNewTab(sectionData.get(section), section);
                                }
                                addButton.setEnabled(true);
                                removeButton.setEnabled(true);
                                seeButton.setEnabled(true);
                                popUpFrame.dispose();
                            } else {
                                JOptionPane.showMessageDialog(popUpFrame, "No file selected.");
                                popUpFrame.dispose();
                            }
                        });
                        cancel.addActionListener(a -> {
                            popUpFrame.dispose();
                        });
                    });
                    addButton.addActionListener(e -> {
                        JFrame popUpFrame = new JFrame("Add Student");

                        popUpFrame.setSize(250, 150);
                        popUpFrame.setLayout(new GridLayout(4, 2));
                        popUpFrame.setLocationRelativeTo(null);
                        popUpFrame.setVisible(true);
                        popUpFrame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

                        JLabel idLabel = new JLabel("Student ID:");
                        JTextField idField = new JTextField();

                        JLabel nameLabel = new JLabel("Student Name:");
                        JTextField nameField = new JTextField();

                        JLabel sectionLabel = new JLabel("Section:");
                        JTextField sectionField = new JTextField();

                        JButton addStudentButton = new JButton("Add Student");
                        JButton cancelButton = new JButton("Cancel");

                        popUpFrame.add(idLabel);
                        popUpFrame.add(idField);
                        popUpFrame.add(nameLabel);
                        popUpFrame.add(nameField);
                        popUpFrame.add(sectionLabel);
                        popUpFrame.add(sectionField);
                        popUpFrame.add(addStudentButton);
                        popUpFrame.add(cancelButton);

                        addStudentButton.addActionListener(u -> {
                            String schoolID;
                            while(true) {
                                schoolID = idField.getText().matches("^[1-9]\\d{10}$") ? idField.getText() : "";
                                if (schoolID.isEmpty()) {
                                    JOptionPane.showMessageDialog(popUpFrame, "Correctly input the ID");
                                    idField.setText("");
                                    continue;
                                }
                                break;
                            }
                            String nameSurname = nameField.getText();
                            String section = sectionField.getText();
                            addNewStudent(schoolID,nameSurname,section);
                            popUpFrame.dispose();
                        });
                        cancelButton.addActionListener(a -> {
                            popUpFrame.dispose();
                        });
                    });
                    removeButton.addActionListener(e -> {
                        int selectedIndex = tabbedPane.getSelectedIndex();
                        StudentTableModel tempModel = tableModels.get(selectedIndex);
                        JTable tempTable = tables.get(selectedIndex);
                        int selectedRow = tempTable.getSelectedRow();
                        if (selectedRow != -1) {
                            int confirm = JOptionPane.showConfirmDialog(this,
                                    "Are you sure about removing the selected row?",
                                    "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (confirm == JOptionPane.YES_OPTION) {
                                tempModel.removeSelectedRow(selectedRow);
                            }
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "No row selected to remove.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
                private void addNewStudent(String ID, String name, String section){
                    if (tableModels.size()>=Integer.parseInt(section)) {
                        String tab = tabbedPane.getTitleAt(Integer.parseInt(section) - 1);
                        String tabSection = tab.split(" ")[1];
                        StudentTableModel temp = tableModels.get(Integer.parseInt(tabSection)-1);
                        temp.addRow(new Object[]{ID, name, section});
                    }else{
                        ArrayList<Object[]> temp = new ArrayList<>();
                        temp.add(new Object[]{ID, name, section});
                        addNewTab(temp,section);
                    }

                }
                private void addNewTab(ArrayList<Object[]> sectionData, String section) {
                    StudentTableModel tableModel = new StudentTableModel(sectionData);
                    JTable table = new JTable(tableModel);
                    JScrollPane scrollPane = new JScrollPane(table);
                    tableModels.add(tableModel);
                    tables.add(table);
                    table.setRowHeight(height / 16);
                    table.setAutoCreateRowSorter(true);

                    TableColumnModel columnModel = table.getColumnModel();
                    columnModel.getColumn(0).setPreferredWidth(width / 64 * 4);
                    columnModel.getColumn(1).setPreferredWidth(width / 64 * 5);
                    columnModel.getColumn(2).setPreferredWidth(width / 64 * 3);

                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(scrollPane, BorderLayout.CENTER);

                    tabbedPane.addTab("Section " + section, panel);
                }
                private HashMap<String, ArrayList<Object[]>> readStudentsFromCSV(String absolutePath) {
                    HashMap<String, ArrayList<Object[]>> sectionData = new HashMap<>();
                    Scanner sc;
                    try {

                        sc = new Scanner(new File("students.csv"), "UTF-8");
                        // System.out.println(sc.hasNextLine());
                        while (sc.hasNextLine()) {
                            String[] line = sc.nextLine().split(",");
                            if (line[0].trim().replace(" ", "").matches("^[1-9]\\d{10}$")) {
                                String schoolID = line[0].trim();
                                String nameSurname = line[1].trim();
                                String section = line[2].trim();

                                sectionData.putIfAbsent(section, new ArrayList<>());
                                sectionData.get(section).add(new Object[]{schoolID, nameSurname, section});
                            }
                        }
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                    return sectionData;
                }

                private class StudentTableModel extends AbstractTableModel {

                    private String[] columnNames = {"Student ID", "Name Surname", "Section"};
                    private ArrayList<Object[]> data;

                    public StudentTableModel(ArrayList<Object[]> data) {
                        this.data = data != null ? data : new ArrayList<>();
                    }

                    public int getColumnCount() {
                        return columnNames.length;
                    }

                    public int getRowCount() {
                        return data.size();
                    }

                    public Object getValueAt(int row, int col) {
                        return data.get(row)[col];
                    }

                    public String getColumnName(int col) {
                        return columnNames[col];
                    }
                    public void addRow(Object[] rowData) {
                        data.add(rowData);
                        fireTableRowsInserted(data.size() - 1, data.size() - 1);
                    }

                    public void removeSelectedRow(int row) {
                        if (row >= 0 && row < data.size()) {
                            data.remove(row);
                            fireTableRowsDeleted(row, row);
                            fireTableDataChanged();
                        }
                    }
                }
            }

            //------------------Question Panel---------------------
            JPanel questionPanel;

            private class QuestionPanel extends JPanel {
                JButton addButton;
                JButton removeButton;
                JPanel buttonPanel;
                QuestionTableModel tableModel;
                JTable table;
                JScrollPane scrollPane;

                public QuestionPanel() {
                    tableModel = new QuestionTableModel();
                    table = new JTable(tableModel);
                    setPreferredSize(new Dimension(width / 32 * 14, height / 32 * 8));
                    int tableWidth = width / 32 * 14;
                    TableColumnModel columnModel = table.getColumnModel();
                    columnModel.getColumn(0).setPreferredWidth(tableWidth / 28);
                    columnModel.getColumn(1).setPreferredWidth(tableWidth / 28 * 21);
                    columnModel.getColumn(2).setPreferredWidth(tableWidth / 28 * 6);
                    table.setRowHeight(height / 64 * 4);
                    table.setAutoCreateRowSorter(true);

                    scrollPane = new JScrollPane(table);

                    addButton = new JButton("Add New Question");
                    removeButton = new JButton("Remove Question");
                    buttonPanel = new JPanel();

                    buttonPanel.add(addButton);
                    buttonPanel.add(removeButton);
                    buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                    add(scrollPane);
                    add(buttonPanel);

                    addButton.addActionListener(new AddButtonListener());

                    removeButton.addActionListener(e -> {
                        int selectedRow = table.getSelectedRow();
                        if (selectedRow != -1) {
                            int confirm = JOptionPane.showConfirmDialog(this,
                                    "Are you sure about removing the selected row?",
                                    "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (confirm == JOptionPane.YES_OPTION) {
                                tableModel.removeSelectedRow(selectedRow);
                            }
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "No row selected to remove.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });

                    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                    setBorder(BorderFactory.createTitledBorder("Questions"));
                }

                private class AddButtonListener implements ActionListener {
                    ArrayList<JCheckBox> checkBoxes;
                    JFrame popUpFrame;
                    //-------- Panels
                    JPanel mainPanel;
                    JPanel inputPanel;
                    JPanel LOPanel;
                    JPanel checkBoxPanel;
                    JPanel questionPanelContainer;
                    JPanel questionPanel;
                    JPanel answerPanel;
                    JPanel buttonPanel;
                    //-------- Labels
                    JLabel questionLabel;
                    JLabel answerLabel;
                    JLabel LOLabel;
                    //-------- Buttons
                    JButton saveButton;
                    JButton cancelButton;

                    //-------- TextAreas
                    JTextArea questionArea;
                    JTextArea answerArea;

                    //-------- ScrollPanes
                    JScrollPane questionScrollPane;
                    JScrollPane answerScrollPane;

                    public void actionPerformed(ActionEvent event) {
                        JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(table);
                        mainFrame.setEnabled(false);

                        checkBoxes = new ArrayList<>();
                        popUpFrame = new JFrame("Add New Question");
                        mainPanel = new JPanel();
                        inputPanel = new JPanel();
                        LOPanel = new JPanel();
                        checkBoxPanel = new JPanel();
                        questionPanelContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                        questionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                        answerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

                        questionLabel = new JLabel("Question:");
                        answerLabel = new JLabel("Correct Answer:");
                        LOLabel = new JLabel("Learning Outcomes:");
                        saveButton = new JButton("Save");
                        cancelButton = new JButton("Cancel");

                        questionArea = new JTextArea(5, 30);
                        answerArea = new JTextArea(5, 30);

                        questionScrollPane = new JScrollPane(questionArea);
                        answerScrollPane = new JScrollPane(answerArea);

                        popUpFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                        popUpFrame.setSize(720, 405);
                        popUpFrame.setResizable(false);

                        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
                        questionPanelContainer.setLayout(new FlowLayout(FlowLayout.LEFT));
                        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

                        //---------- QuestionPanel
                        questionLabel.setPreferredSize(new Dimension(120, 30));
                        questionArea.setLineWrap(true);
                        questionArea.setWrapStyleWord(true);
                        questionScrollPane.setPreferredSize(new Dimension(400, 150));
                        questionPanel.add(questionLabel);
                        questionPanel.add(questionScrollPane);

                        //---------- AnswerPanel
                        answerLabel.setPreferredSize(new Dimension(120, 30));
                        answerArea.setLineWrap(true);
                        answerArea.setWrapStyleWord(true);
                        answerScrollPane.setPreferredSize(new Dimension(400, 150));
                        answerPanel.add(answerLabel);
                        answerPanel.add(answerScrollPane);

                        //---------- CheckBoxLOPanel
                        LOPanel.setLayout(new BoxLayout(LOPanel, BoxLayout.Y_AXIS));
                        LOPanel.add(LOLabel);
                        LOPanel.add(checkBoxPanel);
                        checkBoxPanel.setLayout(new GridLayout(0, 1));
                        //TODO derse göre oluştur
                        addCheckBox(3);


                        buttonPanel.add(saveButton);
                        buttonPanel.add(cancelButton);
                        inputPanel.add(questionPanel);
                        inputPanel.add(answerPanel);
                        questionPanelContainer.add(inputPanel);
                        questionPanelContainer.add(LOPanel);
                        mainPanel.add(questionPanelContainer);
                        mainPanel.add(buttonPanel);

                        //------- Button Listeners
                        saveButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                String question = questionArea.getText();
                                String correctAnswer = answerArea.getText();
                                String LOs = getSelectedCheckBoxes();
                                if (question.isEmpty() || correctAnswer.isEmpty() || LOs.isEmpty()) {
                                    JOptionPane.showMessageDialog(popUpFrame, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, question, LOs});
                                    JOptionPane.showMessageDialog(popUpFrame, "Question saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                                    mainFrame.setEnabled(true);
                                    popUpFrame.dispose();
                                }
                            }
                        });

                        cancelButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                mainFrame.setEnabled(true);
                                popUpFrame.dispose();
                            }
                        });
                        popUpFrame.setLocation(width / 4, height / 4);
                        popUpFrame.add(mainPanel);
                        popUpFrame.setVisible(true);
                    }

                    private void addCheckBox(int LOCount) {
                        checkBoxPanel.removeAll();
                        for (int i = 0; i < LOCount; i++) {
                            JCheckBox temp = new JCheckBox("LO" + (i + 1));
                            checkBoxes.add(temp);
                            checkBoxPanel.add(temp);
                        }
                    }

                    private String getSelectedCheckBoxes() {
                        StringBuilder LOs = new StringBuilder();
                        for (JCheckBox checkBox : checkBoxes) {
                            if (checkBox.isSelected()) {
                                LOs.append(checkBox.getText() + ",");
                            }
                        }
                        if (LOs.isEmpty()) return "";
                        return LOs.replace(LOs.length() - 1, LOs.length(), "").toString();
                    }
                }

                private class QuestionTableModel extends AbstractTableModel {

                    private String[] columnNames = {"#", "Question", "Learning Outcomes"};
                    //private Object[][] data = {{"1","Selam ","LO1"}};
                    private ArrayList<Object[]> data = new ArrayList<>();

                    public int getColumnCount() {
                        return columnNames.length;
                    }

                    public int getRowCount() {
                        return data.size();
                    }

                    public Object getValueAt(int row, int col) {
                        return data.get(row)[col];
                    }

                    public String getColumnName(int col) {
                        return columnNames[col];
                    }

                    public void addRow(Object[] rowData) {
                        data.add(rowData);
                        fireTableRowsInserted(data.size() - 1, data.size() - 1);
                    }

                    public void removeSelectedRow(int row) {
                        if (row >= 0 && row < data.size()) {
                            data.remove(row);
                            fireTableRowsDeleted(row, row);
                            for (int i = row; i < data.size(); i++) {
                                data.get(i)[0] = i + 1;
                            }
                            fireTableDataChanged();
                        }
                    }
                }
            }

            //------------------Exam Panel---------------------
            JPanel examPanel;

            private class ExamPanel extends JPanel {
                public ExamPanel() {
                    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                    setBorder(BorderFactory.createTitledBorder("Exams"));
                }
            }

            public CourseInfoPanel() {
                this.loPanel = new LoPanel();
                this.studentPanel = new StudentPanel();
                this.examPanel = new ExamPanel();
                this.questionPanel = new QuestionPanel();
                setLayout(new BorderLayout());
                add(this.loPanel, BorderLayout.NORTH);
                add(this.studentPanel, BorderLayout.EAST);
                JPanel leftColumn = new JPanel();
                leftColumn.setLayout(new GridLayout(0, 1));
                leftColumn.add(this.questionPanel);
                leftColumn.add(this.examPanel);
                add(leftColumn, BorderLayout.CENTER);
            }
        }

        public RightPanel() {
            setLayout(new BorderLayout());
            this.courseNamePanel = new CourseNamePanel();
            this.courseNamePanel.add(this.courseNamePanel.courseNameLabel);
            this.courseInfoPanel = new CourseInfoPanel();
            this.courseNamePanel.setPreferredSize(new Dimension(width / 32 * 20, (height / 32)));
            add(this.courseNamePanel, BorderLayout.NORTH);
            this.courseInfoPanel.setPreferredSize(new Dimension(width / 32 * 20, (height / 32 * 15)));
            add(this.courseInfoPanel, BorderLayout.CENTER);
        }
    }

    public Frame() {
        this.leftPanel = new LeftPanel();
        this.rightPanel = new RightPanel();
        add(this.leftPanel, BorderLayout.WEST);
        add(this.rightPanel, BorderLayout.CENTER);
        this.rightPanel.setVisible(false);

        setSize(width / 4 * 3, height / 4 * 3);
        setLocation(width / 8, height / 8);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}

class Test {
    public static void main(String[] args) {
        Frame frame = new Frame();
    }
}
