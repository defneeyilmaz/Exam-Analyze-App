import java.time.LocalDateTime;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.*;

public class Frame extends JFrame {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ObjectInputStream objectInput;

    Toolkit kit = Toolkit.getDefaultToolkit();
    Dimension screenSize = kit.getScreenSize();
    int width = screenSize.width;
    int height = screenSize.height;
    //------------------Login Panel---------------------
    LoginSignupPanel loginSignupPanel;

    public class LoginSignupPanel extends JPanel {
        LoginScreen loginScreen;
        Boolean success = false;
        String[] faculties = new String[]{"Software Engineering", "Computer Engineering"};
        String name;
        String faculty;

        private class LoginScreen extends JPanel {
            JTextField usernameField;
            JPasswordField passwordField;
            JButton loginButton, signUpButton;

            public LoginScreen() {
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

                JLabel titleLabel = new JLabel("Login");
                titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
                titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
                JLabel usernameLabel = new JLabel("Username:");
                this.usernameField = new JTextField(10);
                JLabel passwordLabel = new JLabel("Password:");
                this.passwordField = new JPasswordField(10);

                inputPanel.add(usernameLabel);
                inputPanel.add(this.usernameField);
                inputPanel.add(passwordLabel);
                inputPanel.add(this.passwordField);

                this.loginButton = new JButton("Login");
                this.loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                this.signUpButton = new JButton("Sign Up");
                this.signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);

                this.loginButton.addActionListener(e -> {
                    String username = usernameField.getText().trim();
                    String password = new String(passwordField.getPassword()).trim();

                    String query = "SELECT * FROM Lecturers WHERE username = '" + username + "' AND password = '" + password + "'";
                    out.println(query);

                    try {

                        Object response = objectInput.readObject();

                        if (response instanceof List<?> && !((List<?>) response).isEmpty()) {
                            List<?> responseList = (List<?>) response;

                            // Assuming the server sends rows as maps or objects
                            if (responseList.getFirst() instanceof Map<?, ?>) {
                                @SuppressWarnings("unchecked")
                                Map<String, String> rowData = (Map<String, String>) responseList.getFirst();

                                String dbUsername = rowData.get("username");
                                String dbFaculty = rowData.get("faculty");

                                JOptionPane.showMessageDialog(this, "Login Successful!\nWelcome " + dbUsername);
                                loginSignupPanel.name = dbUsername;
                                loginSignupPanel.faculty = dbFaculty;
                                this.removeAll();
                                success = true;
                            } else {
                                JOptionPane.showMessageDialog(this, "Unexpected response format.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "No account found. Please sign up.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "An error occurred. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });

                this.signUpButton.addActionListener(e -> {
                    removeAll();
                    add(new SignupScreen());
                    revalidate();
                    repaint();
                });
                add(Box.createVerticalGlue());
                add(Box.createVerticalGlue());
                add(Box.createVerticalGlue());
                add(titleLabel);
                add(Box.createVerticalStrut(20));
                add(inputPanel);
                add(Box.createVerticalStrut(20));
                add(this.loginButton);
                add(Box.createVerticalStrut(10));
                add(this.signUpButton);
                add(Box.createVerticalGlue());
                add(Box.createVerticalGlue());
                add(Box.createVerticalGlue());
            }
        }

        SignupScreen signupScreen;

        private class SignupScreen extends JPanel {
            JTextField usernameField;
            JPasswordField passwordField, confirmPasswordField;
            JButton signupButton;
            JButton cancelButton;
            JComboBox<String> facultyField;

            public SignupScreen() {
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

                JLabel titleLabel = new JLabel("Sign Up");
                titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
                titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                // Input Fields
                JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
                JLabel usernameLabel = new JLabel("Username:");
                this.usernameField = new JTextField(10);
                JLabel passwordLabel = new JLabel("Password:");
                this.passwordField = new JPasswordField(10);
                JLabel confirmLabel = new JLabel("Confirm:");
                this.confirmPasswordField = new JPasswordField(10);
                JLabel facultyLabel = new JLabel("Faculty:");
                this.facultyField = new JComboBox<>(faculties);
                this.facultyField.setSelectedIndex(-1);

                inputPanel.add(usernameLabel);
                inputPanel.add(usernameField);
                inputPanel.add(passwordLabel);
                inputPanel.add(passwordField);
                inputPanel.add(confirmLabel);
                inputPanel.add(confirmPasswordField);
                inputPanel.add(facultyLabel);
                inputPanel.add(facultyField);

                // Signup Button
                this.signupButton = new JButton("Sign Up");
                this.signupButton.setAlignmentX(Component.CENTER_ALIGNMENT);

                this.cancelButton = new JButton("Cancel");
                this.cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);

                //TODO: regex
                this.signupButton.addActionListener(e -> {
                    String username = this.usernameField.getText();
                    String password = this.passwordField.getText();
                    String confirmPassword = this.confirmPasswordField.getText();
                    String faculty = (String) this.facultyField.getSelectedItem();

                    if (!password.equals(confirmPassword)) {
                        JOptionPane.showMessageDialog(null, "Passwords don't match.\n Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                        this.passwordField.setText("");
                        this.confirmPasswordField.setText("");
                        return;
                    } else {
                        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
                        attributes.put("username", username);
                        attributes.put("password", password);
                        attributes.put("faculty", faculty);
                        String query = insertInto("Lecturers", attributes);
                        out.println(query);
                        try {
                            String response = in.readLine();
                            System.out.println(response);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                    JOptionPane.showMessageDialog(this, "Account created! Please log in.");
                    removeAll();
                    add(new LoginScreen());
                    revalidate();
                    repaint();
                });

                this.cancelButton.addActionListener(e -> {
                    removeAll();
                    add(new LoginScreen());
                    revalidate();
                    repaint();
                });

                // Add Components
                add(Box.createVerticalGlue());
                add(Box.createVerticalGlue());
                add(Box.createVerticalGlue());
                add(titleLabel);
                add(Box.createVerticalStrut(20));
                add(inputPanel);
                add(Box.createVerticalStrut(20));
                add(this.signupButton);
                add(this.cancelButton);
                add(Box.createVerticalStrut(10));
                add(Box.createVerticalGlue());
                add(Box.createVerticalGlue());
                add(Box.createVerticalGlue());
            }
        }

        public LoginSignupPanel() {
            this.loginScreen = new LoginScreen();
            setLayout(new BorderLayout());
            add(this.loginScreen, BorderLayout.CENTER);
        }
    }

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
                this.userName = new JLabel("   " + loginSignupPanel.name);
                this.department = new JLabel("     " + loginSignupPanel.faculty);
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

            void setCourses() {
                String setCourses = "SELECT coursename FROM Courses WHERE lecturername = '" + loginSignupPanel.name + "'";
                try {
                    out.println(setCourses);
                    Object response = objectInput.readObject();

                    if (response instanceof List<?> && !((List<?>) response).isEmpty()) {
                        List<?> responseList = (List<?>) response;

                        for (Object course : responseList) {
                            @SuppressWarnings("unchecked")
                            Map<String, String> courseMap = (Map<String, String>) course;
                            String courseName = courseMap.get("coursename");
                            this.listModel.addElement(courseName);
                        }
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }


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
            Boolean selected = false;

            public ButtonPanel() {
                this.addButton = new JButton("Add Course");
                this.removeButton = new JButton("Remove Chosen Course");

                setLayout(new GridLayout(0, 1));
                add(this.addButton);
                add(this.removeButton);
                setBorder(BorderFactory.createTitledBorder(""));

                this.addButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            String query = "SELECT coursecode FROM CourseInfo";
                            out.println(query);
                            Object response = objectInput.readObject();
                            String[] courses;
                            if (response instanceof List<?>) {
                                List<?> responseList = (List<?>) response;
                                courses = responseList.stream().map(obj -> {
                                    String raw = obj.toString();
                                    return raw.substring(raw.indexOf("=") + 1, raw.length() - 1).trim();
                                }).toArray(String[]::new);
                            } else {
                                throw new IllegalArgumentException("Unexpected response format");
                            }
                            Object selectedCourse = JOptionPane.showInputDialog(null, "Choose the course to add",
                                    "Add Course", JOptionPane.QUESTION_MESSAGE, null, courses, courses.length > 0 ? courses[0] : null);
                            if (selectedCourse != null && !courseListPanel.listModel.contains(selectedCourse)) {
                                courseListPanel.listModel.addElement(selectedCourse.toString());
                                LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
                                attributes.put("lecturername", loginSignupPanel.name);
                                attributes.put("coursename", selectedCourse.toString());
                                String insert = insertInto("Courses", attributes);
                                try {
                                    out.println(insert);
                                    String response_ = in.readLine();
                                    System.out.println(response_);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            } else if (selectedCourse != null) {
                                JOptionPane.showMessageDialog(Frame.super.rootPane, "You already added this course.");
                            }
                        } catch (IOException | ClassNotFoundException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(Frame.super.rootPane,
                                    "Failed to fetch courses. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                this.removeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int selectedIndex = courseListPanel.list.getSelectedIndex();
                        String selectedCourse = courseListPanel.list.getSelectedValue();
                        if (selectedIndex != -1) {
                            String delete = "DELETE FROM Courses WHERE coursename = \"" + selectedCourse + "\" AND lecturername = \"" + loginSignupPanel.name + "\"";
                            System.out.println(delete);
                            try {
                                out.println(delete);
                                String response = in.readLine();
                                System.out.println("Server response: " + response);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Please select a valid course to remove.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        courseListPanel.listModel.remove(selectedIndex);
                        rightPanel.setVisible(false);
                    }
                });
                courseListPanel.list.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent event) {
                        String selectedCourse = courseListPanel.list.getSelectedValue();
                        if (selectedCourse != null) {
                            updateRightPanel(selectedCourse);
                            selected = true;
                        }
                    }
                });
            }

            private void updateRightPanel(String selectedCourse) {
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
                if (parentFrame != null) {
                    parentFrame.proceedToCoursePage(selectedCourse);
                }
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

            public CourseNamePanel(String courseName) {
                this.courseNameLabel = new JLabel();
                updateCourseName(courseName);
                add(courseNameLabel);
            }

            public void updateCourseName(String courseCode) {
                String getCourseName = "SELECT coursename FROM CourseInfo WHERE coursecode = \"" + courseCode + "\"";
                try {
                    out.println(getCourseName);
                    Object response = objectInput.readObject();
                    if (response instanceof List<?> && !((List<?>) response).isEmpty()) {
                        List<?> responseList = (List<?>) response;

                        if (responseList.getFirst() instanceof Map<?, ?>) {
                            @SuppressWarnings("unchecked")
                            Map<String, String> rowData = (Map<String, String>) responseList.getFirst();
                            String courseName = rowData.get("coursename");
                            this.courseNameLabel.setText(courseCode + " | " + courseName);
                        }
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        }

        //------------------Course Information Panel---------------------
        CourseInfoPanel courseInfoPanel;

        private class CourseInfoPanel extends JPanel {
            //------------------Learning Outcome Panel---------------------
            LoPanel loPanel;

            private class LoPanel extends JPanel {
                LOTablePanel tablePanel;
                EvaluationPanel evaluationPanel;

                private class LOTablePanel extends JPanel {
                    LOTableModel tableModel;
                    JTable table;
                    JScrollPane scrollPane;

                    private class LOTableModel extends AbstractTableModel {
                        private String[] columnNames = {"#", "Learning Outcome"};
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

                        public void addRow(Object[] row) {
                            data.add(row);
                            fireTableRowsInserted(data.size() - 1, data.size() - 1);
                        }
                    }

                    void getLOs() {
                        String selectedCourse = leftPanel.courseListPanel.list.getSelectedValue();
                        String query = "SELECT number, text FROM LOs WHERE coursecode = \"" + selectedCourse + "\"";
                        try {
                            out.println(query);
                            Object response = objectInput.readObject();
                            if (response instanceof List<?> && !((List<?>) response).isEmpty()) {
                                List<?> responseList = (List<?>) response;
                                for (Object course : responseList) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> courseMap = (Map<String, Object>) course;
                                    int number = (int) courseMap.get("number");
                                    String lo = (String) courseMap.get("text");
                                    lo = lo.substring(0);
                                    Object[] row = new Object[]{number, lo};
                                    this.tableModel.addRow(row);
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "No learning outcomes found for the selected course.",
                                        "Info", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (IOException | ClassNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }

                    public LOTablePanel() {
                        this.tableModel = new LOTableModel();
                        this.table = new JTable(this.tableModel);

                        JTableHeader header = this.table.getTableHeader();
                        header.setFont(new Font("Arial", Font.BOLD, 16));

                        TableColumnModel columnModel = table.getColumnModel();
                        int tableWidth = width / 32 * 20;
                        columnModel.getColumn(0).setPreferredWidth(tableWidth / 56);
                        columnModel.getColumn(1).setPreferredWidth(tableWidth / 56 * 54);

                        this.table.setRowHeight(height / 64 * 2);
                        this.table.setAutoCreateRowSorter(true);
                        this.table.getTableHeader().setReorderingAllowed(false);

                        this.scrollPane = new JScrollPane(table);
                        this.scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                        setLayout(new BorderLayout());
                        add(scrollPane, BorderLayout.CENTER);

                        this.getLOs();
                    }
                }

                private class EvaluationPanel extends JPanel {
                    JLabel evaluationCriteria;

                    String getEvaluationCriteria() {
                        String selectedCourse = leftPanel.courseListPanel.list.getSelectedValue();
                        String query = "SELECT evaluationcriteria FROM CourseInfo WHERE coursecode = \"" + selectedCourse + "\"";
                        String evaluationCrt = "";
                        try {
                            out.println(query);
                            Object response = objectInput.readObject();
                            List<?> evaluation = (List<?>) response;
                            @SuppressWarnings("unchecked")
                            Map<String, String> evaluationMap = (Map<String, String>) evaluation.get(0);
                            if (evaluationMap != null) {
                                String evaluationCriteria = evaluationMap.get("evaluationcriteria");
                                evaluationCrt = evaluationCriteria.substring(1, evaluationCriteria.length() - 1);
                                evaluationCrt = " " + evaluationCrt;
                            } else {
                                JOptionPane.showMessageDialog(null, "No evaluation criteria found for the selected course.",
                                        "Info", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (IOException | ClassNotFoundException ex) {
                            ex.printStackTrace();
                        }
                        return evaluationCrt;
                    }

                    public EvaluationPanel() {
                        this.evaluationCriteria = new JLabel(this.getEvaluationCriteria());
                        this.evaluationCriteria.setOpaque(true);
                        this.evaluationCriteria.setBackground(Color.WHITE);
                        this.evaluationCriteria.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                        setLayout(new BorderLayout());
                        add(evaluationCriteria, BorderLayout.CENTER);
                    }
                }

                public LoPanel() {
                    this.tablePanel = new LOTablePanel();
                    this.evaluationPanel = new EvaluationPanel();
                    this.tablePanel.setPreferredSize(new Dimension(width / 32 * 20, height / 32 * 7));
                    setLayout(new BorderLayout());
                    this.evaluationPanel.setPreferredSize(new Dimension(width / 32 * 20, height / 32));
                    add(tablePanel, BorderLayout.CENTER);
                    add(evaluationPanel, BorderLayout.SOUTH);

                    setBorder(BorderFactory.createTitledBorder("Course Information"));
                }
            }


            //------------------ Learning Outcome Panel ---------------------
            StudentPanel studentPanel;

            private class StudentPanel extends JPanel {
                JTabbedPane tabbedPane;
                ArrayList<JTable> tables;
                ArrayList<StudentTableModel> tableModels;
                JPanel buttonPanel;
                JButton addButton;
                JButton removeButton;
                JButton seeButton;
                JButton addAllStudentsButton;

                public StudentPanel() {
                    tables = new ArrayList<>();
                    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                    setBorder(BorderFactory.createTitledBorder("Students"));
                    setPreferredSize(new Dimension(width / 32 * 6, height / 32 * 15));

                    tabbedPane = new JTabbedPane();
                    add(tabbedPane, BorderLayout.CENTER);

                    buttonPanel = new JPanel(); //---- Button Panel
                    add(buttonPanel, BorderLayout.SOUTH);
                    buttonPanel.setLayout(new GridLayout(0, 2));

                    addButton = new JButton("Add");
                    removeButton = new JButton("Delete");
                    seeButton = new JButton("See");
                    addAllStudentsButton = new JButton("Add from csv file");

                    buttonPanel.add(addButton);
                    buttonPanel.add(removeButton);
                    buttonPanel.add(seeButton);
                    buttonPanel.add(addAllStudentsButton);
                    buttonPanel.setPreferredSize(new Dimension(width / 32 * 6, height / 32 * 2));

                    populateTable();

                    addAllStudentsButton.addActionListener(e -> {

                        JFrame popUpFrame = new JFrame("Add Students");

                        popUpFrame.setSize(300, 200);

                        JPanel container = new JPanel();
                        JButton fileSelector = new JButton("Select CSV File");
                        JButton cancel = new JButton("Cancel");
                        container.add(fileSelector);
                        container.add(cancel);
                        container.setLayout(new FlowLayout(FlowLayout.LEFT));

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

                                addNewStudent(readStudentsFromCSV(selectedFile.getAbsolutePath()));

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
                        //TODO: Sectionı kontrol et
                        addStudentButton.addActionListener(u -> {
                            String schoolID;
                                schoolID = idField.getText().matches("^[1-9]\\d{10}$") ? idField.getText() : "";
                                if (schoolID.isEmpty() ) {
                                    JOptionPane.showMessageDialog(popUpFrame, "Correctly input the ID");
                                    idField.setText("");
                                }else {
                                    String nameSurname = nameField.getText();
                                    String section = sectionField.getText();
                                    HashMap<String, ArrayList<Object[]>> temp = new HashMap<>();
                                    ArrayList<Object[]> a = new ArrayList<>();
                                    a.add(new Object[]{schoolID, nameSurname, section});
                                    temp.put(section, a);
                                    addNewStudent(temp);
                                    popUpFrame.dispose();
                                }
                        });
                        cancelButton.addActionListener(a -> {
                            popUpFrame.dispose();
                        });
                    });
                    removeButton.addActionListener(e -> {
                        int selectedIndex = tabbedPane.getSelectedIndex();
                        JTable tempTable = tables.get(selectedIndex);
                        StudentTableModel tempModel = (StudentTableModel) tables.get(selectedIndex).getModel();
                        int selectedRow = tempTable.getSelectedRow();
                        if (selectedRow != -1) {
                            int confirm = JOptionPane.showConfirmDialog(this,
                                    "Are you sure about removing the selected row?",
                                    "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (confirm == JOptionPane.YES_OPTION) {
                                String ID = (String) tempModel.getValueAt(selectedRow, 0);
                                tempModel.removeSelectedRow(selectedRow);
                                String delete = "DELETE FROM Enrollments WHERE studentID = \"" + ID + "\"";
                                try {
                                    out.println(delete);
                                    String response = in.readLine();
                                    System.out.println("Server response: " + response);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "No row selected to remove.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    seeButton.addActionListener(new SeeButtonListener());
                }

                private class SeeButtonListener implements ActionListener {
                    JFrame popUpFrame;
                    JTable table;
                    JScrollPane scrollPane;
                    TableModel examTableModel;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int selectedIndex = tabbedPane.getSelectedIndex();
                        JTable tempTable = tables.get(selectedIndex);
                        StudentTableModel tableModel = (StudentTableModel) tables.get(selectedIndex).getModel();
                        int selectedRow = tempTable.getSelectedRow();
                        if (selectedRow != -1) {
                            String studentID = "";
                            String name = "";
                            String section = "";
                            try {
                                Object[] info = (Object[]) tableModel.getRow(selectedRow);
                                studentID = (String) info[0];
                                name = (String) info[1];
                                section = (String) info[2];
                            } catch (ClassCastException ex) {
                                ex.printStackTrace();
                            }

                            popUpFrame = new JFrame("Student Information");
                            popUpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            popUpFrame.setSize(width / 5 * 2, height / 5 * 2);
                            popUpFrame.setLocationRelativeTo(null);

                            JPanel container = new JPanel(new GridLayout(1, 2));

                            JPanel left = new JPanel(new BorderLayout());
                            examTableModel = new TableModel(new String[]{"Exam ID", "Exam Name"});

                            table = new JTable(examTableModel);
                            scrollPane = new JScrollPane(table);
                            left.add(scrollPane, BorderLayout.CENTER);

                            JButton addScoreButton = new JButton("Add Score");
                            addScoreButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                            left.add(addScoreButton, BorderLayout.SOUTH);

                            JPanel right = new JPanel();
                            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

                            JPanel info = new JPanel();
                            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
                            info.setAlignmentX(Component.LEFT_ALIGNMENT);
                            info.add(new JLabel("Name: " + name));
                            info.add(new JLabel("Student ID: " + studentID));
                            info.add(new JLabel("Section: " + section));

                            JPanel LO = new JPanel(new GridLayout(0, 2, 3, 3));
                            LO.setBorder(BorderFactory.createTitledBorder("Learning Outcomes"));
                            ArrayList<JTextField> textFields = new ArrayList<>();
                            for (int i = 1; i <= loPanel.tablePanel.tableModel.getRowCount(); i++) {
                                LO.add(new JLabel("LO" + i + ":"));
                                JTextField textField = new JTextField();
                                textField.setEnabled(false);
                                textField.setText("");
                                textFields.add(textField);
                                LO.add(textField);
                            }

                            right.add(info);

                            right.add(LO);
                            populateTable();
                            container.add(left);
                            container.add(right);

                            addScoreButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {

                                int selectedRowExam = table.getSelectedRow();
                                JFrame popUpFrameScore;
                                JTable tableQuestions;
                                JScrollPane scrollPane;
                                TableModel examQuestionTableModel;

                                if (selectedRowExam != -1) {

                                    popUpFrameScore = new JFrame("Student Grades");
                                    popUpFrameScore.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                                    popUpFrameScore.setSize(400, 300);

                                    JPanel panel = new JPanel();
                                    panel.setLayout(new BorderLayout());

                                    examQuestionTableModel = new TableModel(new String[]{"Question ID", "Question", "Student's Score", "Possible Score"});
                                    tableQuestions = new JTable(examQuestionTableModel);
                                    tableQuestions.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()));
                                    tableQuestions.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer());
                                    scrollPane = new JScrollPane(tableQuestions);
                                    panel.add(scrollPane, BorderLayout.CENTER);

                                    String query = "SELECT questionID , question, possiblepoint FROM Questions WHERE examID = \"" +
                                            examTableModel.getValueAt(selectedIndex,0)+ "\" ";
                                    try {
                                        out.println(query);
                                        Object response = objectInput.readObject();

                                        if (response instanceof List<?> && !((List<?>) response).isEmpty()) {
                                            List<?> responseList = (List<?>) response;

                                            for (Object exam : responseList) {
                                                @SuppressWarnings("unchecked")
                                                Map<String, String> questionMap = (Map<String, String>) exam;
                                                examQuestionTableModel.addRow(new Object[]{questionMap.get("questionID"), questionMap.get("question"),"" ,questionMap.get("possiblepoint")});
                                            }
                                        }
                                    } catch (IOException | ClassNotFoundException ex) {
                                        ex.printStackTrace();
                                    }

                                    for (Object[] temp: examQuestionTableModel.data){
                                        String query1 = "SELECT point FROM Grades WHERE studentID = \"" +
                                                tableModel.getValueAt(selectedRow,0)+"\" AND questionID = \""+temp[0]+"\"";
                                        try {
                                            out.println(query1);
                                            Object response = objectInput.readObject();

                                            if (response instanceof List<?> && !((List<?>) response).isEmpty()) {
                                                List<?> responseList = (List<?>) response;

                                                for (Object point : responseList) {
                                                    @SuppressWarnings("unchecked")
                                                    Map<String, String> pointMap = (Map<String, String>) point;
                                                    temp[2]=pointMap.get("point");
                                                }
                                            }
                                        } catch (IOException | ClassNotFoundException ex) {
                                            ex.printStackTrace();
                                        }

                                    }



                                    JPanel buttonPanel = new JPanel();
                                    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));


                                    JButton okButton = new JButton("OK");
                                    okButton.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            int flag =0;
                                            for (Object[] temp: examQuestionTableModel.data){
                                                if (temp[2].equals("")) flag++;
                                            }
                                            if(flag!=0){
                                                JOptionPane.showMessageDialog(popUpFrameScore,
                                                        "Scores cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                                            }else{
                                                for (Object[] temp: examQuestionTableModel.data){
                                                    //LinkedHashMap<String,String> insert = new LinkedHashMap<>();
                                                    //insert.put("studentID",(String)tableModel.getValueAt(selectedRow,0));
                                                    //insert.put("questionID",(String)temp[0]);
                                                    //insert.put("point",(String)temp[2]);
                                                    try {
                                                        out.println("UPDATE Grades SET point = \""+temp[2]+"\" WHERE studentID = \""+tableModel.getValueAt(selectedRow,0)+"\" AND questionID = \""+temp[0]+"\"");
                                                        String response_ = in.readLine();
                                                        System.out.println(response_);
                                                    } catch (IOException ex) {
                                                        ex.printStackTrace();
                                                    }
                                                }
                                                popUpFrameScore.dispose();
                                            }
                                        }
                                    });


                                    JButton cancelButton = new JButton("Cancel");
                                    cancelButton.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            popUpFrameScore.dispose();
                                        }
                                    });

                                    buttonPanel.add(okButton);
                                    buttonPanel.add(cancelButton);

                                    panel.add(buttonPanel, BorderLayout.SOUTH);
                                    popUpFrameScore.add(panel);
                                    popUpFrameScore.setLocationRelativeTo(null);
                                    popUpFrameScore.setVisible(true);
                                } else {
                                    JOptionPane.showMessageDialog(popUpFrame,
                                            "No row selected.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                }
                            });

                            popUpFrame.add(container, BorderLayout.CENTER);
                            popUpFrame.setVisible(true);

                        } else {
                            JOptionPane.showMessageDialog(popUpFrame,
                                    "No row selected.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    private void populateTable() {
                        String setCourses = "SELECT examID ,examname FROM Exams WHERE coursecode = \"" +
                                leftPanel.courseListPanel.list.getSelectedValue() + "\" ";
                        try {
                            out.println(setCourses);
                            Object response = objectInput.readObject();

                            if (response instanceof List<?> && !((List<?>) response).isEmpty()) {
                                List<?> responseList = (List<?>) response;

                                for (Object exam : responseList) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, String> examMap = (Map<String, String>) exam;
                                    examTableModel.addRow(new Object[]{examMap.get("examID"), examMap.get("examname")});
                                }
                            }
                        } catch (IOException | ClassNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }

                    class TableModel extends AbstractTableModel {

                        private String[] columnNames;
                        private ArrayList<Object[]> data;

                        public TableModel(String[] array) {
                            columnNames = array;
                            data = new ArrayList<>();
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

                        public Object getRow(int row) {
                            return data.get(row);
                        }

                        public String getColumnName(int col) {
                            return columnNames[col];
                        }

                        public void setValueAt(int row,int col, String value){
                            data.get(row)[col] = value;
                            fireTableCellUpdated(row, col);
                        }
                        public void setValueAt(Object value, int row, int col) {
                            if (col == 2) {
                                try {
                                    int pointValue = Integer.parseInt((String) value);
                                    if (pointValue < 0 || pointValue > Integer.parseInt((String)data.get(row)[3])) {
                                        throw new NumberFormatException("Points must be between 0 and 100.");
                                    }
                                    data.get(row)[col] = value;
                                    fireTableCellUpdated(row, col);
                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(null, "Invalid point value. Please enter a valid integer between 0 and 100.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                        public boolean isCellEditable(int row, int col) {
                            return col == 2;
                        }
                        public void addRow(Object[] rowData) {
                            data.add(rowData);
                            fireTableRowsInserted(data.size() - 1, data.size() - 1);
                        }

                        public void removeSelectedRow(int row) {
                            if (row >= 0 && row < data.size()) {
                                data.remove(row);
                                fireTableRowsDeleted(row, row);

                            }
                        }
                    }
                }

                private void populateTable() {
                    String query1 = "SELECT studentID, coursecode, section  FROM Enrollments WHERE coursecode = \"" + leftPanel.courseListPanel.list.getSelectedValue() + "\"";
                    try {

                        out.println(query1);
                        Object response1 = objectInput.readObject();
                        if (response1 instanceof List<?> && !((List<?>) response1).isEmpty()) {
                            List<?> responseList1 = (List<?>) response1;
                            for (Object student1 : responseList1) {
                                @SuppressWarnings("unchecked")
                                Map<String, String> studentMap1 = (Map<String, String>) student1;
                                String query2 = "SELECT name FROM Students WHERE studentID = \"" + studentMap1.get("studentID") + "\"";
                                try {
                                    out.println(query2);
                                    Object response2 = objectInput.readObject();

                                    if (response2 instanceof List<?> && !((List<?>) response2).isEmpty()) {
                                        List<?> responseList2 = (List<?>) response2;

                                        for (Object student2 : responseList2) {
                                            @SuppressWarnings("unchecked")
                                            Map<String, String> studentMap2 = (Map<String, String>) student2;
                                            addNewStudentToTable(new Object[]{studentMap1.get("studentID"), studentMap2.get("name"), studentMap1.get("section")}, studentMap1.get("section"));
                                        }
                                    }
                                } catch (IOException | ClassNotFoundException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    } catch (IOException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }

                private void addNewStudentToTable(Object[] student, String section) {
                    if (tables.size() >= Integer.parseInt(section)) {
                        String tab = tabbedPane.getTitleAt(Integer.parseInt(section) - 1);
                        String tabSection = tab.split(" ")[1];
                        StudentTableModel temp = (StudentTableModel) tables.get(Integer.parseInt(tabSection) - 1).getModel();
                        temp.addRow(student);
                    } else {
                        ArrayList<Object[]> temp = new ArrayList<>();
                        temp.add(student);
                        addNewTab(temp, section);
                    }
                }

                private void addNewStudent(HashMap<String, ArrayList<Object[]>> data) {
                    String ID;
                    String name;
                    for (String section : data.keySet()) {
                        for (Object[] temp : data.get(section)) {
                            ID = (String) temp[0];
                            name = (String) temp[1];
                            String query1 = "SELECT COUNT(*) AS student FROM Students WHERE studentID = \"" + ID + "\"";
                            try {
                                out.println(query1);
                                Object response1 = objectInput.readObject();
                                if (response1 instanceof List<?> && !((List<?>) response1).isEmpty()) {
                                    List<?> responseList1 = (List<?>) response1;

                                    for (Object result1 : responseList1) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Integer> countMap = (Map<String, Integer>) result1;
                                        if (countMap.get("student") == 1) {
                                            String query2 = "SELECT COUNT(*) AS enrollment FROM Enrollments WHERE studentID = \"" + ID + "\"" + "AND coursecode = \"" + leftPanel.courseListPanel.list.getSelectedValue() + "\"";

                                            try {
                                                out.println(query2);
                                                Object response2 = objectInput.readObject();
                                                if (response2 instanceof List<?> && !((List<?>) response2).isEmpty()) {
                                                    List<?> responseList2 = (List<?>) response2;
                                                    for (Object result2 : responseList2) {
                                                        @SuppressWarnings("unchecked")
                                                        Map<String, Integer> countMap2 = (Map<String, Integer>) result2;
                                                        System.out.println(countMap2.get("enrollment"));
                                                        if (countMap2.get("enrollment") == 0) {
                                                            LinkedHashMap<String, String> student = new LinkedHashMap<>();
                                                            student.put("studentID", ID);
                                                            student.put("coursecode", leftPanel.courseListPanel.list.getSelectedValue());
                                                            student.put("section", section);
                                                            String insert = insertInto("Enrollments", student);
                                                            try {
                                                                out.println(insert);
                                                                String response_ = in.readLine();
                                                                System.out.println(response_);
                                                            } catch (IOException ex) {
                                                                ex.printStackTrace();
                                                            }
                                                            addNewStudentToTable(new Object[]{ID, name, section}, section);
                                                        } else {
                                                            System.out.println("aynı var");
                                                        }
                                                    }
                                                }
                                            } catch (IOException | ClassNotFoundException ex) {
                                                ex.printStackTrace();
                                            }
                                        } else {
                                            LinkedHashMap<String, String> student = new LinkedHashMap<>();
                                            student.put("studentID", ID);
                                            student.put("name", name);
                                            String insert1 = insertInto("Students", student);
                                            try {
                                                out.println(insert1);
                                                String response_ = in.readLine();
                                                System.out.println(response_);
                                            } catch (IOException ex) {
                                                ex.printStackTrace();
                                            }
                                            LinkedHashMap<String, String> studentEnrollment = new LinkedHashMap<>();
                                            studentEnrollment.put("studentID", ID);
                                            studentEnrollment.put("coursecode", leftPanel.courseListPanel.list.getSelectedValue());
                                            studentEnrollment.put("section", section);
                                            String insert2 = insertInto("Enrollments", studentEnrollment);
                                            try {
                                                out.println(insert2);
                                                String response_ = in.readLine();
                                                System.out.println(response_);
                                            } catch (IOException ex) {
                                                ex.printStackTrace();
                                            }
                                            addNewStudentToTable(new Object[]{ID, name, section}, section);
                                        }
                                    }
                                }
                            } catch (IOException | ClassNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }

                private void addNewTab(ArrayList<Object[]> sectionData, String section) {
                    StudentTableModel tableModel = new StudentTableModel(sectionData);
                    JTable table = new JTable(tableModel);
                    JScrollPane scrollPane = new JScrollPane(table);
                    tables.add(table);


                    table.setRowHeight(height / 16);
                    table.setAutoCreateRowSorter(false);
                    table.getTableHeader().setReorderingAllowed(false);

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
                        sc = new Scanner(new File(absolutePath), "UTF-8");

                        while (sc.hasNextLine()) {
                            String[] line = sc.nextLine().split(",");
                            if (line[0].trim().replace(" ", "").matches("^[1-9]\\d{10}$")) {
                                String ID = line[0].trim();
                                String nameSurname = line[1].trim();
                                String section = line[2].trim();
                                sectionData.putIfAbsent(section, new ArrayList<>());
                                sectionData.get(section).add(new Object[]{ID, nameSurname, section});
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

                    public Object getRow(int row) {
                        return data.get(row);
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

                        }
                    }
                }
            }

            //------------------Question Panel---------------------
            JPanel questionPanel;

            private class QuestionPanel extends JPanel {
                JButton addButton;
                JButton removeButton;
                JButton seeButton;
                JPanel buttonPanel;
                QuestionTableModel tableModel;
                JTable table;
                JScrollPane scrollPane;

                public QuestionPanel() {
                    tableModel = new QuestionTableModel();
                    table = new JTable(tableModel);

                    String setCourses = "SELECT questionID ,question ,answer ,LO  FROM Questions WHERE coursecode = \"" +
                            leftPanel.courseListPanel.list.getSelectedValue() + "\" " + "AND possiblepoint = \"0\";";
                    try {
                        out.println(setCourses);
                        Object response = objectInput.readObject();

                        if (response instanceof List<?> && !((List<?>) response).isEmpty()) {
                            List<?> responseList = (List<?>) response;

                            for (Object question : responseList) {
                                @SuppressWarnings("unchecked")
                                Map<String, String> questionMap = (Map<String, String>) question;
                                tableModel.addRow(new Object[]{questionMap.get("questionID"), questionMap.get("question"),
                                        questionMap.get("answer"), questionMap.get("LO")});
                            }
                        }
                    } catch (IOException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }

                    setPreferredSize(new Dimension(width / 32 * 14, height / 32 * 8));
                    int tableWidth = width / 32 * 14;
                    TableColumnModel columnModel = table.getColumnModel();
                    columnModel.getColumn(0).setPreferredWidth(tableWidth / 28);
                    columnModel.getColumn(1).setPreferredWidth(tableWidth / 28 * 21);
                    columnModel.getColumn(2).setPreferredWidth(tableWidth / 28 * 6);
                    table.setRowHeight(height / 64 * 4);
                    table.setAutoCreateRowSorter(true);
                    table.getTableHeader().setReorderingAllowed(false);

                    scrollPane = new JScrollPane(table);

                    addButton = new JButton("Add New Question");
                    removeButton = new JButton("Remove Question");
                    seeButton = new JButton("See Selected Question");
                    buttonPanel = new JPanel();

                    buttonPanel.add(addButton);
                    buttonPanel.add(removeButton);
                    buttonPanel.add(seeButton);
                    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
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
                                String ID = (String) tableModel.getValueAt(selectedRow, 0);

                                String delete = "DELETE FROM Questions WHERE questionID = \"" + ID + "\"";
                                try {
                                    out.println(delete);
                                    String response = in.readLine();
                                    System.out.println("Server response: " + response);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                tableModel.removeSelectedRow(selectedRow);
                            }
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "No row selected to remove.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });

                    seeButton.addActionListener(e -> {
                        int selectedRow = table.getSelectedRow();
                        if (selectedRow != -1) {
                            String question = "";
                            String answer = "";
                            String LOs = "";
                            try {
                                Object[] info = (Object[]) tableModel.getRow(selectedRow);
                                question = (String) info[1];
                                answer = (String) info[2];
                                LOs = (String) info[3];
                            } catch (ClassCastException ex) {
                                ex.printStackTrace();
                            }
                            JFrame popUpFrame = new JFrame("Question");
                            popUpFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                            popUpFrame.setSize(640, 480);
                            popUpFrame.setLocationRelativeTo(null);
                            popUpFrame.setLayout(new BoxLayout(popUpFrame.getContentPane(), BoxLayout.Y_AXIS));
                            JPanel textPanel = new JPanel();

                            JTextArea textArea1 = new JTextArea();
                            textArea1.setEditable(false);
                            textArea1.setLineWrap(true);
                            textArea1.setWrapStyleWord(true);
                            textPanel.add(new JLabel("Question"));
                            textPanel.add(textArea1);
                            textArea1.setText(question);
                            textArea1.setBorder(BorderFactory.createTitledBorder(""));

                            JTextArea textArea2 = new JTextArea();
                            textArea2.setEditable(false);
                            textArea2.setLineWrap(true);
                            textArea2.setWrapStyleWord(true);
                            textPanel.add(new JLabel("Answer"));
                            textPanel.add(textArea2);
                            textArea2.setText(answer);
                            textArea2.setBorder(BorderFactory.createTitledBorder(""));

                            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                            textPanel.setPreferredSize(new Dimension(640, 380));

                            JButton okButton = new JButton("OK");
                            okButton.addActionListener(o -> popUpFrame.dispose());

                            popUpFrame.add(textPanel);
                            popUpFrame.add(new Label("Learning Outcomes: " + LOs));
                            popUpFrame.add(okButton);

                            popUpFrame.setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "No row selected.", "Error", JOptionPane.ERROR_MESSAGE);
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
                        addCheckBox();


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
                                String questionID = createID();

                                if (question.isEmpty() || correctAnswer.isEmpty() || LOs.isEmpty()) {
                                    JOptionPane.showMessageDialog(popUpFrame, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    LinkedHashMap<String, String> temp = new LinkedHashMap<>();
                                    temp.put("coursecode", leftPanel.courseListPanel.list.getSelectedValue());
                                    temp.put("question", question);
                                    temp.put("answer", correctAnswer);
                                    temp.put("possiblepoint", "0");
                                    temp.put("LO", LOs);
                                    temp.put("questionID", questionID);
                                    temp.put("examID", "");
                                    String insert = insertInto("Questions", temp);
                                    try {
                                        out.println(insert);
                                        String response_ = in.readLine();
                                        System.out.println(response_);
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                    tableModel.addRow(new Object[]{questionID, question, correctAnswer, LOs});
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

                    private void addCheckBox() {
                        String query = "SELECT COUNT(*) AS LOCount FROM LOs WHERE coursecode = \"" + leftPanel.courseListPanel.list.getSelectedValue() + "\"";
                        int LOCount = 0;
                        try {
                            out.println(query);
                            Object response = objectInput.readObject();

                            if (response instanceof List<?> && !((List<?>) response).isEmpty()) {
                                List<?> responseList = (List<?>) response;
                                for (Object temp : responseList) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Integer> count = (Map<String, Integer>) temp;
                                    System.out.println(count.get("LOCount"));
                                    LOCount = count.get("LOCount");
                                }
                            }
                        } catch (IOException | ClassNotFoundException ex) {
                            ex.printStackTrace();
                        }

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

                    private String[] columnNames = {"ID", "Question", "Answer", "Learning Outcomes"};
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

                    public Object getRow(int row) {
                        return data.get(row);
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

            //------------------Exam Panel---------------------
            JPanel examPanel;

            private class ExamPanel extends JPanel {
                ExamTableModel tableModel;
                JTable table;
                JScrollPane scrollPane;
                ButtonPanel buttonPanel;

                public ExamPanel() {
                    this.tableModel = new ExamTableModel();
                    this.table = new JTable(this.tableModel);
                    this.scrollPane = new JScrollPane(this.table);
                    this.buttonPanel = new ButtonPanel();

                    int tableWidth = width / 32 * 14;
                    TableColumnModel columnModel = this.table.getColumnModel();
                    columnModel.getColumn(0).setPreferredWidth(tableWidth / 28 * 2);
                    columnModel.getColumn(1).setPreferredWidth(tableWidth / 28 * 10);
                    columnModel.getColumn(2).setPreferredWidth(tableWidth / 28 * 10);
                    columnModel.getColumn(3).setPreferredWidth(tableWidth / 28 * 6);
                    this.table.setRowHeight(tableWidth / 64 * 4);
                    this.table.setAutoCreateRowSorter(true);
                    this.table.getTableHeader().setReorderingAllowed(false);

                    setPreferredSize(new Dimension(width / 32 * 14, height / 32 * 8));
                    setLayout(new BorderLayout());
                    setBorder(BorderFactory.createTitledBorder("Exams"));

                    add(this.scrollPane, BorderLayout.CENTER);
                    add(this.buttonPanel, BorderLayout.SOUTH);
                }

                private class ButtonPanel extends JPanel {
                    JButton createButton;
                    JButton removeButton;
                    JButton viewExamButton;

                    public ButtonPanel() {
                        this.createButton = new JButton("Create Exam");
                        this.removeButton = new JButton("Remove Exam");
                        this.viewExamButton = new JButton("View Exam");

                        this.createButton.addActionListener(new CreateExamButtonListener());
                        this.removeButton.addActionListener(new RemoveExamButtonListener());
                        this.viewExamButton.addActionListener(new ViewExamButtonListener());

                        setLayout(new FlowLayout(FlowLayout.CENTER));
                        add(this.createButton);
                        add(this.removeButton);
                        add(this.viewExamButton);
                    }
                }

                private class CreateExamButtonListener implements ActionListener {
                    private int selectedQuestionRow = -1;  // This will store the selected row index

                    @Override
                    public void actionPerformed(ActionEvent event) {
                        JFrame popUpFrame = new JFrame("Create Exam");
                        JPanel mainPanel = new JPanel(new BorderLayout());

                        QuestionTableModel questionTableModel = new QuestionTableModel();
                        ButtonPanelInPopUp buttonPanelInPopUp = new ButtonPanelInPopUp(this, questionTableModel);
                        JTable questionTable = new JTable(questionTableModel);
                        questionTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JTextField()));
                        questionTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer());
                        questionTable.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JCheckBox()));
                        questionTable.getColumnModel().getColumn(5).setCellRenderer(new QuestionTableModel().new CheckboxRenderer(Color.WHITE));

                        questionTable.getSelectionModel().addListSelectionListener(e -> {
                            selectedQuestionRow = questionTable.getSelectedRow();
                        });
                        JScrollPane questionScrollPane = new JScrollPane(questionTable);
                        questionScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                        questionTable.setRowHeight(width / 32);
                        questionTable.getTableHeader().setReorderingAllowed(false);

                        mainPanel.setLayout(new BorderLayout());
                        mainPanel.add(questionScrollPane, BorderLayout.CENTER);
                        mainPanel.add(buttonPanelInPopUp, BorderLayout.SOUTH);

                        popUpFrame.add(mainPanel);
                        popUpFrame.setSize(720, 405);
                        popUpFrame.setResizable(false);
                        popUpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        popUpFrame.setLocation(width / 4, height / 4);
                        popUpFrame.setVisible(true);
                    }

                    public int getSelectedQuestionRow() {
                        return selectedQuestionRow;
                    }
                }

                //Button panel inside the pop-up frame
                private class ButtonPanelInPopUp extends JPanel {
                    JButton createExamButton;
                    JButton showQuestionButton;
                    JButton cancelButton;
                    private CreateExamButtonListener createExamButtonListener;
                    private QuestionTableModel questionTableModel;

                    public ButtonPanelInPopUp(CreateExamButtonListener createExamButtonListener, QuestionTableModel questionTableModel) {
                        this.createExamButtonListener = createExamButtonListener;
                        this.questionTableModel = questionTableModel;

                        this.createExamButton = new JButton("Create");
                        this.showQuestionButton = new JButton("See Selected Question");
                        this.cancelButton = new JButton("Cancel");

                        setLayout(new FlowLayout(FlowLayout.CENTER));
                        add(this.createExamButton);
                        add(this.showQuestionButton);
                        add(this.cancelButton);


                        this.createExamButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                List<Object[]> selectedQuestions = new ArrayList<>();

                                for (int row = 0; row < questionTableModel.getRowCount(); row++) {
                                    Boolean isSelected = (Boolean) questionTableModel.getValueAt(row, 5);
                                    String pointString = (String) questionTableModel.getValueAt(row, 4);
                                    if (isSelected != null && isSelected) {
                                        if (pointString == null || pointString.isEmpty()) {
                                            JOptionPane.showMessageDialog(null, "Please enter points for all selected questions.", "Error", JOptionPane.ERROR_MESSAGE);
                                            return;
                                        }

                                        try {
                                            Integer.parseInt(pointString);
                                            selectedQuestions.add((Object[]) questionTableModel.getRow(row));
                                        } catch (NumberFormatException ex) {
                                            JOptionPane.showMessageDialog(null, "Invalid point value. Please enter a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
                                            return;
                                        }
                                    }
                                }

                                if (!selectedQuestions.isEmpty()) {
                                    JPanel panel = new JPanel();
                                    panel.setLayout(new GridLayout(0, 1));

                                    JTextField examNameField = new JTextField(20);
                                    JTextField examTypeField = new JTextField(20);
                                    panel.add(new JLabel("Enter Exam Name:"));
                                    panel.add(examNameField);
                                    panel.add(Box.createVerticalStrut(10));
                                    panel.add(new JLabel("Enter Exam Type:"));
                                    panel.add(examTypeField);

                                    int option = JOptionPane.showConfirmDialog(null, panel, "Create Exam", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                                    if (option == JOptionPane.OK_OPTION) {
                                        String examName = examNameField.getText().trim();
                                        String examType = examTypeField.getText().trim();

                                        if (examName.isEmpty()) {
                                            JOptionPane.showMessageDialog(null, "Exam name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                                            return;
                                        }

                                        if (examType.isEmpty()) {
                                            JOptionPane.showMessageDialog(null, "You must select an Exam Type.", "Error", JOptionPane.ERROR_MESSAGE);
                                            return;
                                        } else {
                                            createExamFromSelectedQuestions(examName, examType, selectedQuestions);
                                            ((JFrame) SwingUtilities.getWindowAncestor(ButtonPanelInPopUp.this)).dispose();
                                        }
                                    }

                                } else {
                                    JOptionPane.showMessageDialog(null, "No questions selected!", "Error", JOptionPane.ERROR_MESSAGE);
                                }

                            }
                        });


                        this.showQuestionButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                int selectedRow = createExamButtonListener.getSelectedQuestionRow();
                                if (selectedRow >= 0) {
                                    String question = "";
                                    String answer = "";
                                    String LOs = "";
                                    try {
                                        Object[] info = (Object[]) questionTableModel.getRow(selectedRow);
                                        question = (String) info[1];
                                        answer = (String) info[2];
                                        LOs = (String) info[3];
                                    } catch (ClassCastException ex) {
                                        ex.printStackTrace();
                                    }

                                    JFrame popUpFrame = new JFrame("Question");
                                    popUpFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                                    popUpFrame.setSize(640, 480);
                                    popUpFrame.setLocationRelativeTo(null);
                                    popUpFrame.setLayout(new BoxLayout(popUpFrame.getContentPane(), BoxLayout.Y_AXIS));
                                    JPanel textPanel = new JPanel();

                                    JTextArea textArea1 = new JTextArea();
                                    textArea1.setEditable(false);
                                    textArea1.setLineWrap(true);
                                    textArea1.setWrapStyleWord(true);
                                    textPanel.add(new JLabel("Question"));
                                    textPanel.add(textArea1);
                                    textArea1.setText(question);
                                    textArea1.setBorder(BorderFactory.createTitledBorder(""));

                                    JTextArea textArea2 = new JTextArea();
                                    textArea2.setEditable(false);
                                    textArea2.setLineWrap(true);
                                    textArea2.setWrapStyleWord(true);
                                    textPanel.add(new JLabel("Answer"));
                                    textPanel.add(textArea2);
                                    textArea2.setText(answer);
                                    textArea2.setBorder(BorderFactory.createTitledBorder(""));

                                    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                                    textPanel.setPreferredSize(new Dimension(640, 380));

                                    JButton okButton = new JButton("OK");
                                    okButton.addActionListener(o -> popUpFrame.dispose());

                                    popUpFrame.add(textPanel);
                                    popUpFrame.add(new Label("Learning Outcomes: " + LOs));
                                    popUpFrame.add(okButton);

                                    popUpFrame.setVisible(true);
                                } else {
                                    JOptionPane.showMessageDialog(Frame.super.rootPane,
                                            "No row selected.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        });

                        this.cancelButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                ((JFrame) SwingUtilities.getWindowAncestor(ButtonPanelInPopUp.this)).dispose();
                            }
                        });
                    }
                }

                private void createExamFromSelectedQuestions(String name, String type, List<Object[]> selectedQuestions) {
                    String examName = name;
                    String examType = type;
                    String ID = createID();
                    Set<String> los = new HashSet<>();

                    int totalPoints = 0;
                    for (Object[] question : selectedQuestions) {
                        String losString = (String) question[3];
                        String pointsString = (String) question[4];

                        try {
                            int point = Integer.parseInt(pointsString);
                            totalPoints += point;
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            return;
                        }

                        String[] individualLOs = losString.split(",");
                        for (String lo : individualLOs) {
                            los.add(lo.trim());
                        }
                    }
                    if (totalPoints != 100) {
                        JOptionPane.showMessageDialog(null, "Total points must equal 100!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    List<String> sortedLOs = new ArrayList<>(los);
                    Collections.sort(sortedLOs);
                    String losString = String.join(", ", sortedLOs);

                    LinkedHashMap<String, String> temp = new LinkedHashMap<>();
                    temp.put("coursecode", leftPanel.courseListPanel.list.getSelectedValue());
                    temp.put("examtype", examType);
                    temp.put("examname", examName);
                    temp.put("examID", ID);
                    temp.put("los", losString);
                    String createExam = insertInto("Exams", temp);
                    try {
                        out.println(createExam);
                        String response = in.readLine();
                        System.out.println(response);
                        tableModel.addRow(new Object[]{ID, examName, examType, losString});
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    List<String> studentIDs = new ArrayList<>();
                    String getStudents = "SELECT studentID FROM Enrollments WHERE coursecode = \"" + leftPanel.courseListPanel.list.getSelectedValue() + "\"";
                    try {
                        out.println(getStudents);
                        Object response = objectInput.readObject();

                        if (response instanceof List<?> && !((List<?>) response).isEmpty()) {
                            List<?> responseList = (List<?>) response;

                            for (Object students : responseList) {
                                @SuppressWarnings("unchecked")
                                Map<String, String> student = (Map<String, String>) students;
                                studentIDs.add(student.get("studentID"));
                            }
                        }
                    } catch (IOException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }

                    //"ID", "Question", "Answer", "Learning Outcomes", "Point", "Select"
                    LinkedHashMap<String, String> addQuestion = new LinkedHashMap<>();

                    for (Object[] question : selectedQuestions) {
                        addQuestion.put("coursecode", leftPanel.courseListPanel.list.getSelectedValue());
                        addQuestion.put("question", question[1].toString());
                        addQuestion.put("answer", question[2].toString());
                        addQuestion.put("possiblepoint", question[4].toString());
                        addQuestion.put("LO", question[3].toString());
                        String questionID = createID();
                        addQuestion.put("questionID", questionID);
                        addQuestion.put("examID", ID);
                        String newQuestion = insertInto("Questions", addQuestion);
                        try {
                            out.println(newQuestion);
                            String response = in.readLine();
                            System.out.println(response);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        LinkedHashMap<String, String> setGrade = new LinkedHashMap<>();
                        for (String studentID : studentIDs) {
                            setGrade.put("studentID", studentID);
                            setGrade.put("questionID", questionID);
                            setGrade.put("point", "0");
                            String setGrades = insertInto("Grades", setGrade);
                            try {
                                out.println(setGrades);
                                String response = in.readLine();
                            }   catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Table model for the list of exams (shown in the main ExamPanel)
                private class ExamTableModel extends AbstractTableModel {
                    private String[] columnNames = {"ID", "Name", "Exam Type", "LO's"};
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
                        }
                    }

                    public ExamTableModel() {
                        String getExams = "SELECT * FROM Exams WHERE coursecode = \"" + leftPanel.courseListPanel.list.getSelectedValue() + "\"";
                        try {
                            out.println(getExams);
                            Object response = objectInput.readObject();
                            if (response instanceof List<?> && !((List<?>) response).isEmpty()) {
                                List<?> responseList = (List<?>) response;
                                for (Object exam : responseList) {
                                     @SuppressWarnings("unchecked")
                                    Map<String, String> examMap = (Map<String, String>) exam;
                                    addRow(new Object[]{examMap.get("examID"), examMap.get("examname"), examMap.get("examtype"), examMap.get("los")});
                                }
                            }
                        } catch (IOException | ClassNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                // Table model for the questions inside the pop-up "Create Exam" frame
                private class QuestionTableModel extends AbstractTableModel {
                    private String[] columnNames = {"ID", "Question", "Answer", "Learning Outcomes", "Point", "Select"};
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

                    public void setValueAt(Object value, int row, int col) {
                        if (col == 4) {
                            try {
                                int pointValue = Integer.parseInt((String) value);
                                if (pointValue < 0 || pointValue > 100) {
                                    throw new NumberFormatException("Points must be between 0 and 100.");
                                }
                                data.get(row)[col] = value;
                                fireTableCellUpdated(row, col);
                            } catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(null, "Invalid point value. Please enter a valid integer between 0 and 100.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else if (col == 5) {
                            data.get(row)[col] = value;
                            fireTableCellUpdated(row, col);
                        }
                    }

                    public boolean isCellEditable(int row, int col) {
                        if (col == 4) {
                            Boolean isSelected = (Boolean) data.get(row)[5];
                            return isSelected != null && isSelected;
                        }
                        return col == 5;
                    }

                    public String getColumnName(int col) {
                        return columnNames[col];
                    }

                    public void addRow(Object[] rowData) {
                        data.add(rowData);
                        fireTableRowsInserted(data.size() - 1, data.size() - 1);
                    }

                    public QuestionTableModel() {
                        String getQuestions = "SELECT questionID ,question ,answer ,LO FROM Questions WHERE coursecode = \""
                                + leftPanel.courseListPanel.list.getSelectedValue() + "\"" + " AND possiblepoint = \"0\";";
                        System.out.println(getQuestions);
                        try {
                            out.println(getQuestions);
                            Object response = objectInput.readObject();

                            if (response instanceof List<?> && !((List<?>) response).isEmpty()) {
                                List<?> responseList = (List<?>) response;
                                for (Object question : responseList) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, String> questionMap = (Map<String, String>) question;
                                    addRow(new Object[]{
                                            questionMap.get("questionID"),
                                            questionMap.get("question"),
                                            questionMap.get("answer"),
                                            questionMap.get("LO"),
                                            "",
                                            false
                                    });
                                }
                            }
                        } catch (IOException | ClassNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }

                    public class CheckboxRenderer extends JCheckBox implements TableCellRenderer {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                            setSelected(value != null && (Boolean) value);
                            setBackground(backgroundColor);

                            if (isSelected) {
                                setBackground(table.getSelectionBackground());
                            }
                            return this;
                        }

                        private Color backgroundColor;

                        public CheckboxRenderer(Color backgroundColor) {
                            this.backgroundColor = backgroundColor;
                        }

                    }

                    public Class<?> getColumnClass(int columnIndex) {
                        if (columnIndex == 4) {
                            return String.class;
                        } else if (columnIndex == 5) {
                            return Boolean.class;
                        }
                        return super.getColumnClass(columnIndex);
                    }

                    public Object getRow(int selectedRow) {
                        if (selectedRow >= 0 && selectedRow < data.size()) {
                            return data.get(selectedRow);
                        }
                        return null;
                    }
                }

                private class ViewExamButtonListener implements ActionListener {
                    int selectedRow = -1;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int selectedRow = table.getSelectedRow();
                        if (selectedRow >= 0) {
                            String examID = (String) tableModel.getValueAt(selectedRow, 0);
                            String query = "SELECT * FROM Questions WHERE examID = \"" + examID + "\"";
                            try {
                                out.println(query);
                                Object response = objectInput.readObject();

                                if (response instanceof List<?> && !((List<?>) response).isEmpty()) {
                                    JFrame popUpFrame = new JFrame();
                                    JPanel panel = new JPanel();
                                    ViewQuestionTableModel questionTableModel = new ViewQuestionTableModel(response);
                                    JTable questionTable = new JTable(questionTableModel);
                                    JScrollPane questionScrollPane = new JScrollPane(questionTable);
                                    questionScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                                    questionTable.setRowHeight(width / 32);

                                    panel.setLayout(new BorderLayout());
                                    popUpFrame.setLayout(new BorderLayout());
                                    panel.add(questionScrollPane, BorderLayout.CENTER);
                                    popUpFrame.add(panel, BorderLayout.CENTER);
                                    popUpFrame.setSize(720, 405);
                                    popUpFrame.setResizable(false);
                                    popUpFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                                    popUpFrame.setLocation(width / 4, height / 4);
                                    popUpFrame.setVisible(true);
                                }
                            } catch (IOException | ClassNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(examPanel),
                                    "Choose an exam to view.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

                //TODO have to delete the questions too
                private class RemoveExamButtonListener implements ActionListener {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int selectedRow = table.getSelectedRow();
                        if (selectedRow >= 0) {
                            int confirm = JOptionPane.showConfirmDialog(examPanel,
                                    "Are you sure about removing the selected exam?",
                                    "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (confirm == JOptionPane.YES_OPTION) {
                                String ID = (String) tableModel.getValueAt(selectedRow, 0);
                                String delete = "DELETE FROM Exams WHERE examID = \"" + ID + "\"";
                                String questionDelete = "DELETE FROM Questions WHERE examID = \"" + ID + "\"";
                                try {
                                    out.println(delete);
                                    String response = in.readLine();
                                    System.out.println("Server response: " + response);
                                    out.println(questionDelete);
                                    response = in.readLine();
                                    System.out.println("Server response: " + response);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                tableModel.removeSelectedRow(selectedRow);
                            }
                        } else {
                            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(examPanel),
                                    "No row selected to remove.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

                private class ViewQuestionTableModel extends AbstractTableModel {
                    private String[] columnNames = {"Question", "Answer", "Point ", "Learning Outcomes"};
                    private ArrayList<Object[]> data = new ArrayList<>();

                    public ViewQuestionTableModel(Object response) {
                        if (response instanceof List<?>) {
                            List<?> responseList = (List<?>) response;
                            for (Object question : responseList) {
                                @SuppressWarnings("unchecked")
                                Map<String, String> questionMap = (Map<String, String>) question;
                                addRow(new Object[]{
                                        questionMap.get("question"),
                                        questionMap.get("answer"),
                                        questionMap.get("possiblepoint"),
                                        questionMap.get("LO")
                                });
                            }
                        }
                    }

                    @Override
                    public int getRowCount() {
                        return data.size();
                    }

                    @Override
                    public int getColumnCount() {
                        return columnNames.length;
                    }

                    @Override
                    public Object getValueAt(int rowIndex, int columnIndex) {
                        Object[] rowData = data.get(rowIndex);
                        return rowData[columnIndex];
                    }

                    @Override
                    public String getColumnName(int column) {
                        return columnNames[column];
                    }

                    public void addRow(Object[] rowData) {
                        data.add(rowData);
                        fireTableRowsInserted(data.size() - 1, data.size() - 1);
                    }
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

        public RightPanel(String initialCourse) {
            setLayout(new BorderLayout());
            this.courseNamePanel = new CourseNamePanel(initialCourse);
            this.courseNamePanel.add(this.courseNamePanel.courseNameLabel);
            this.courseInfoPanel = new CourseInfoPanel();
            this.courseNamePanel.setPreferredSize(new Dimension(width / 32 * 20, (height / 36)));
            add(this.courseNamePanel, BorderLayout.NORTH);
            this.courseInfoPanel.setPreferredSize(new Dimension(width / 32 * 20, (height / 32 * 18)));
            add(this.courseInfoPanel, BorderLayout.CENTER);
        }
    }

    public Frame() {
        this.loginSignupPanel = new LoginSignupPanel();
        add(this.loginSignupPanel, BorderLayout.CENTER);

        Timer timer = new Timer(100, e -> {
            if (loginSignupPanel.success) {
                ((Timer) e.getSource()).stop(); // Stop the timer
                proceedToMainUI(); // Load the main UI
            }
        });
        timer.start();

        setupConnection();

        out.println("SELECT * FROM Lecturers");
        try {
            List<Object> response = (List<Object>) objectInput.readObject();
            System.out.println(response.toString());

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        setSize(width / 4 * 3, height / 4 * 3);
        setLocation(width / 8, height / 8);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void proceedToMainUI() {
        getContentPane().remove(loginSignupPanel);
        this.leftPanel = new LeftPanel();
        this.leftPanel.courseListPanel.setCourses();
        add(this.leftPanel, BorderLayout.WEST);
        revalidate();
        repaint();
    }

    public void proceedToCoursePage(String selectedCourse) {
        if (this.rightPanel != null) {
            remove(this.rightPanel);
        }
        this.rightPanel = new RightPanel(selectedCourse);
        add(this.rightPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void setupConnection() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            objectInput = new ObjectInputStream(socket.getInputStream());

            System.out.println("Connected to the server!");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to the server.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public String insertInto(String tableName, LinkedHashMap<String, String> attributes) {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO ").append(tableName).append(" (");
        for (String key : attributes.keySet()) {
            query.append(key).append(", ");
        }
        if (!query.isEmpty()) {
            query.setLength(query.length() - 2);
        }
        query.append(") VALUES ");
        query.append(formatValues(attributes));
        return query.toString();
    }

    private String formatValues(LinkedHashMap<String, String> attributes) {
        StringBuilder formattedValues = new StringBuilder("(");
        for (String value : attributes.values()) {
            formattedValues.append("\"").append(value).append("\", ");
        }
        if (formattedValues.length() > 2) {
            formattedValues.setLength(formattedValues.length() - 2);
        }
        formattedValues.append(")");
        return formattedValues.toString();
    }

    private String createID() {
        return LocalDateTime.now().toString().replace("-", "").replace("T", "").replace(":", "").replace(".","");
    }
}

class Test {
    public static void main(String[] args) {
        Frame frame = new Frame();
    }
}
