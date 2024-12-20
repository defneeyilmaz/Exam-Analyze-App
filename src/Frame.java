import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.function.DoubleToIntFunction;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.text.TableView;

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
        String name; String faculty;
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

                // Buttons
                this.loginButton = new JButton("Login");
                this.loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                this.signUpButton = new JButton("Sign Up");
                this.signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);

                // Button Listeners
                this.loginButton.addActionListener(e -> {
                    String username = usernameField.getText().trim();
                    String password = new String(passwordField.getPassword()).trim();

                    // Update the query to select all columns
                    String query = "SELECT * FROM Lecturers WHERE username = '" + username + "' AND password = '" + password + "'";
                    out.println(query);

                    try {
                        // Read the response from the server
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
                    String faculty = (String)this.facultyField.getSelectedItem();

                    if(!password.equals(confirmPassword)){
                        JOptionPane.showMessageDialog(null, "Passwords don't match.\n Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                        this.passwordField.setText("");
                        this.confirmPasswordField.setText("");
                        return;
                    }else{
                        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();                        attributes.put("username", username);
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
                this.userName = new JLabel("   "+ loginSignupPanel.name);
                this.department = new JLabel("     "+loginSignupPanel.faculty);
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
                                    return raw.substring(raw.indexOf("=") + 1, raw.length() - 1).trim();}).toArray(String[]::new);
                            } else {
                                throw new IllegalArgumentException("Unexpected response format");
                            }
                            Object selectedCourse = JOptionPane.showInputDialog(null,"Choose the course to add",
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

            public void updateCourseName(String courseName) {
                this.courseNameLabel.setText("Course Name: " + courseName);
            }
        }

        //------------------Course Information Panel---------------------
        CourseInfoPanel courseInfoPanel;

        private class CourseInfoPanel extends JPanel {
            //------------------Learning Outcome Panel---------------------
            LoPanel loPanel;
            private class LoPanel extends JPanel {
                LOTablePanel tablePanel;
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
                                    int number = (int)courseMap.get("number");
                                    String lo = (String)courseMap.get("text");
                                    lo = lo.substring(0, lo.length() - 1);
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
                        header.setFont(new Font(getFont().toString(),Font.PLAIN, 15));
                        TableColumnModel columnModel = table.getColumnModel();
                        int tableWidth = width / 32 * 20;
                        columnModel.getColumn(0).setPreferredWidth(tableWidth / 28);
                        columnModel.getColumn(1).setPreferredWidth(tableWidth / 28 * 27);
                        this.table.setRowHeight(height / 64 * 2);
                        this.table.setAutoCreateRowSorter(true);
                        this.scrollPane = new JScrollPane(table);
                        this.scrollPane.setPreferredSize(new Dimension(width / 32 * 20, height / 32 * 7));
                        this.getLOs();
                        add(scrollPane);
                    }
                }

                EvaluationPanel evaluationPanel;
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
                            Map<String, String> evaluationMap = (Map<String, String>) evaluation.getFirst();
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
                        this.evaluationCriteria.setPreferredSize(new Dimension(width / 32 * 20, height / 32));
                        add(evaluationCriteria);
                    }
                }

                public LoPanel() {
                    this.tablePanel = new LOTablePanel();
                    this.evaluationPanel = new EvaluationPanel();
                    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                    add(tablePanel);
                    add(evaluationPanel);
                    this.tablePanel.setPreferredSize(new Dimension(width / 32 * 20, height / 32 * 7));
                    this.evaluationPanel.setPreferredSize(new Dimension(width / 32 * 20, height / 32));
                    setBorder(BorderFactory.createTitledBorder("Course Information"));
                    setPreferredSize(new Dimension(width / 32 * 20, (height / 32 * 8)));
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
                                //System.out.println(LOs); LO1,LO2
                                if (question.isEmpty() || correctAnswer.isEmpty() || LOs.isEmpty()) {
                                    JOptionPane.showMessageDialog(popUpFrame, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
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

        public RightPanel(String initialCourse) {
            setLayout(new BorderLayout());
            this.courseNamePanel = new CourseNamePanel(initialCourse);
            this.courseNamePanel.add(this.courseNamePanel.courseNameLabel);
            this.courseInfoPanel = new CourseInfoPanel();
            this.courseNamePanel.setPreferredSize(new Dimension(width / 32 * 20, (height / 32)));
            add(this.courseNamePanel, BorderLayout.NORTH);
            this.courseInfoPanel.setPreferredSize(new Dimension(width / 32 * 20, (height / 32 * 15)));
            add(this.courseInfoPanel, BorderLayout.CENTER);
        }
        public void updatePanel(String newCourse) {
            this.courseNamePanel.updateCourseName(newCourse);
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

    public void proceedToMainUI(){
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

    private void setupConnection(){
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            objectInput = new ObjectInputStream(socket.getInputStream());

            System.out.println("Connected to the server!");
        }catch (IOException e) {
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
}

class Test {
    public static void main(String[] args) {
        Frame frame = new Frame();
    }
}
