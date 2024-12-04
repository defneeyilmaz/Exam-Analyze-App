import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
            JPanel loPanel;
            JPanel studentPanel;
            JPanel questionPanel;
            JPanel examPanel;

            public CourseInfoPanel() {
                this.loPanel = new JPanel();
                this.studentPanel = new JPanel();
                this.questionPanel = new JPanel();
                this.examPanel = new JPanel();

                setLayout(new BorderLayout());

                this.loPanel.setLayout(new BoxLayout(this.loPanel, BoxLayout.Y_AXIS));
                this.loPanel.setBorder(BorderFactory.createTitledBorder("Learning Outcomes"));
                this.loPanel.add(new JLabel("LO1"));
                this.loPanel.add(new JLabel("LO2"));

                this.loPanel.setPreferredSize(new Dimension(width/16*14, (height/3)));
                add(this.loPanel, BorderLayout.NORTH);

                this.studentPanel.setLayout(new BoxLayout(this.studentPanel, BoxLayout.Y_AXIS));
                this.studentPanel.setBorder(BorderFactory.createTitledBorder("Students"));
                this.studentPanel.add(new JLabel("Section 1: Hüs"));
                this.studentPanel.add(new JLabel("Section 2: Def"));
                this.studentPanel.setPreferredSize(new Dimension(width/5, (height/4)));
                add(this.studentPanel, BorderLayout.EAST);

                this.questionPanel.setLayout(new BoxLayout(this.questionPanel, BoxLayout.Y_AXIS));
                this.questionPanel.setBorder(BorderFactory.createTitledBorder("Questions"));
                this.questionPanel.add(new JLabel("Q1"));
                this.questionPanel.add(new JLabel("Q2"));

                this.examPanel.setLayout(new BoxLayout(this.examPanel, BoxLayout.Y_AXIS));
                this.examPanel.setBorder(BorderFactory.createTitledBorder("Exams"));
                this.examPanel.add(new JLabel("Exam 1"));
                this.examPanel.add(new JLabel("Exam 2"));

                JPanel leftColumn = new JPanel();
                leftColumn.setLayout(new GridLayout(0,1));
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
            this.courseInfoPanel.setPreferredSize(new Dimension(width/8*7, (height/32*31)));
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
