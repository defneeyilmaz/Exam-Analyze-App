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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

public class Frame extends JFrame {
    Toolkit kit = Toolkit.getDefaultToolkit();
    Dimension screenSize = kit.getScreenSize();
    int width = screenSize.width;
    int height = screenSize.height;
    //------------------Left Panel---------------------
    LeftPanel leftPanel ;
    private class LeftPanel extends JPanel{
        //------------------User Information Panel---------------------
        UserInfoPanel userInfoPanel;
        private class UserInfoPanel extends JPanel{
            JPanel userNamePanel;
            JLabel userNameLabel;
            JLabel userName;
            JLabel department;

            public UserInfoPanel(){
                this.userNamePanel = new JPanel();
                this.userNameLabel = new JLabel("     Username:");
                this.userName = new JLabel("Ufuk Çelikkan");
                this.department = new JLabel("       Software Engineering");
                setLayout(new GridLayout(0,1));
                add(this.userNamePanel);
                this.userNamePanel.add(this.userNameLabel);
                this.userNamePanel.add(this.userName);
                this.userNamePanel.setLayout(new GridLayout(0,2));

                setBorder(BorderFactory.createTitledBorder("User"));
                this.department.setFont(this.department.getFont().deriveFont(Font.BOLD));
                add(this.department);
                setBorder(BorderFactory.createTitledBorder(""));
            }
        }
        //------------------Course List Panel---------------------
        CourseListPanel courseListPanel;
        private class CourseListPanel extends JPanel{
            DefaultListModel<String> listModel;
            JList<String> list;
            JScrollPane scrollPane;

            public CourseListPanel(){
                this.listModel = new DefaultListModel<>();
                this.list = new JList<>(this.listModel);
                this.scrollPane = new JScrollPane(this.list);

                this.list.setFixedCellHeight(75);
                this.list.setFixedCellWidth(width/10);
                this.list.setVisibleRowCount(5);

                this.scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                setLayout(new BorderLayout());
                add(this.scrollPane,BorderLayout.CENTER);
                setBorder(BorderFactory.createTitledBorder(""));
            }
        }
        //------------------Button Panel---------------------
        ButtonPanel buttonPanel;
        private class ButtonPanel extends JPanel{
            JButton addButton;
            JButton removeButton;

            public ButtonPanel(){
                this.addButton = new JButton("Add Course");
                this.removeButton = new JButton("Remove Chosen Course");

                setLayout(new GridLayout(0,1));
                add(this.addButton);
                add(this.removeButton);
                setBorder(BorderFactory.createTitledBorder(""));

                this.addButton.addActionListener(new ActionListener() {
                    final String[] SECourses = new String[]{"SE323", "SE321", "SE311", "SE375", "SE216",
                            "SE209","SE322"};

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Object selectedCourse = JOptionPane.showInputDialog(null,
                                "Choose the course to add","Add Course",JOptionPane.QUESTION_MESSAGE,
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
        public LeftPanel(){
            setLayout(new BorderLayout());
            this.userInfoPanel = new UserInfoPanel();
            this.userInfoPanel.setPreferredSize(new Dimension(width/8, (height/16*2)));
            this.courseListPanel = new CourseListPanel();
            this.courseListPanel.setPreferredSize(new Dimension(width/8, (height/16*8)));
            this.buttonPanel = new ButtonPanel();
            this.buttonPanel.setPreferredSize(new Dimension(width/8, (height/16*2)));
            add(this.userInfoPanel,BorderLayout.NORTH);add(this.courseListPanel,BorderLayout.CENTER);add(this.buttonPanel,BorderLayout.SOUTH);
        }
    }
    //------------------Right Panel---------------------
    RightPanel rightPanel ;
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
                    setPreferredSize(new Dimension(width / 16 * 14, (height/32*9)));
                }
            }
            //------------------ Learning Outcome Panel ---------------------
            StudentPanel studentPanel;
            private class StudentPanel extends JPanel {
                public StudentPanel() {
                    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                    setBorder(BorderFactory.createTitledBorder("Students"));
                    setPreferredSize(new Dimension(width / 5, (height/32*14)));

                    JTabbedPane tabbedPane = new JTabbedPane();
                    add(tabbedPane, BorderLayout.CENTER);

                    JPanel buttonPanel = new JPanel(); //---- Button Panel
                    add(buttonPanel, BorderLayout.SOUTH);
                    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

                    JButton addButton = new JButton("Add");
                    JButton removeButton = new JButton("Remove");
                    JButton seeButton = new JButton("See");
                    buttonPanel.add(addButton); buttonPanel.add(removeButton); buttonPanel.add(seeButton);

                    HashMap<String, ArrayList<Object[]>> sectionData = readStudentsFromCSV();

                    for (String section : sectionData.keySet()) {
                        StudentTableModel tableModel = new StudentTableModel(sectionData.get(section));
                        JTable table = new JTable(tableModel);
                        JScrollPane scrollPane = new JScrollPane(table);

                        table.setRowHeight(height / 16);
                        table.setAutoCreateRowSorter(true);

                        TableColumnModel columnModel = table.getColumnModel();
                        columnModel.getColumn(0).setPreferredWidth(width / 64 * 4);
                        columnModel.getColumn(1).setPreferredWidth(width / 64 * 6);
                        columnModel.getColumn(2).setPreferredWidth(width / 64 * 2);

                        JPanel panel = new JPanel(new BorderLayout());
                        panel.add(scrollPane, BorderLayout.CENTER);

                        tabbedPane.addTab("Section " + section, panel);
                    }
                }
                private HashMap<String, ArrayList<Object[]>> readStudentsFromCSV() {
                    HashMap<String, ArrayList<Object[]>> sectionData = new HashMap<>();
                    Scanner sc;
                    try {

                        sc = new Scanner(new File("students.csv"), "UTF-8");
                        System.out.println(sc.hasNextLine());
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
                }
            }
            //------------------Question Panel---------------------
            JPanel questionPanel;
            private class QuestionPanel extends JPanel {
                public QuestionPanel() {
                    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                    setBorder(BorderFactory.createTitledBorder("Questions"));
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

        public RightPanel(){
            setLayout(new BorderLayout());
            this.courseNamePanel = new CourseNamePanel();
            this.courseNamePanel.add(this.courseNamePanel.courseNameLabel);
            this.courseInfoPanel = new CourseInfoPanel();
            this.courseNamePanel.setPreferredSize(new Dimension(width/8*7, (height/32)));
            add(this.courseNamePanel, BorderLayout.NORTH);
            this.courseInfoPanel.setPreferredSize(new Dimension(width/8*7, (height/32*23)));
            add(this.courseInfoPanel, BorderLayout.CENTER);
        }
    }

    public Frame(){
        this.leftPanel = new LeftPanel();
        this.rightPanel = new RightPanel();
        add(this.leftPanel, BorderLayout.WEST);
        add(this.rightPanel, BorderLayout.CENTER);
        this.rightPanel.setVisible(false);

        setSize(width/4*3, height/4*3);
        setLocation(width/8,height/8);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
class Test {
    public static void main(String[] args) {
        Frame frame = new Frame();
    }
}
