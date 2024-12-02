import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Frame extends JFrame {
    Toolkit kit = Toolkit.getDefaultToolkit();
    Dimension screenSize = kit.getScreenSize();
    int width = screenSize.width;
    int height = screenSize.height;
    //------------------Left Panel---------------------
    LeftPanel leftPanel ;
    private class LeftPanel extends JPanel{
        JPanel userPanel = new JPanel();
        JPanel userNamePanel = new JPanel();
        JLabel userNameLabel = new JLabel("     Username:");
        JLabel userName = new JLabel("Ufuk Çelikkan");
        JLabel department = new JLabel("       Software Engineering");
        JPanel courseListPanel = new JPanel();
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> list = new JList<>(listModel);
        public String course;
        JScrollPane scrollPane = new JScrollPane(list);
        JPanel buttonsPanel = new JPanel();
        JButton addButton = new JButton("Add Course");
        JButton removeButton = new JButton("Remove Chosen Course");

        public LeftPanel(){
            userNamePanel.add(userNameLabel); userNamePanel.add(userName);
            userNamePanel.setLayout(new GridLayout(0,2));
            //userNamePanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY,2));
            userPanel.setPreferredSize(new Dimension(width/8, height/16*2));
            department.setFont(department.getFont().deriveFont(Font.BOLD));
            userPanel.add(userNamePanel);userPanel.add(department);
            //userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
            userPanel.setLayout(new GridLayout(2,0));
            userPanel.setBorder(BorderFactory.createTitledBorder(""));

            buttonsPanel.setPreferredSize(new Dimension(width/8, height/16));
            buttonsPanel.add(addButton);
            buttonsPanel.add(removeButton);
            buttonsPanel.setLayout(new GridLayout(0,1));
            buttonsPanel.setBorder(BorderFactory.createTitledBorder(""));
            //temporary array, like get(department.courses)
            String[] SECourses = new String[]{"SE323", "SE321", "SE311", "SE375", "SE216"};

            list.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent event) {
                    course = list.getSelectedValue();
                    rightPanel.setCourseName(course);
                    rightPanel.setVisible(true);
                }
            });

            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Object selectedCourse = JOptionPane.showInputDialog(null,
                            "Choose the course to add","Add Course",JOptionPane.QUESTION_MESSAGE,
                            null, SECourses, SECourses[0]);
                    if (!listModel.contains(selectedCourse)) {
                        listModel.addElement(selectedCourse.toString());
                    } else {
                        JOptionPane.showMessageDialog(Frame.super.rootPane, "You already add this course");
                    }
                }
            });
            removeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int selectedIndex = list.getSelectedIndex();
                    if (selectedIndex != -1) {
                        listModel.remove(selectedIndex);
                    } else {
                        JOptionPane.showMessageDialog(Frame.super.rootPane, "Choose a course to remove");
                    }
                }
            });

            list.setFixedCellHeight(75);
            list.setFixedCellWidth(width/8);
            list.setVisibleRowCount(5);

            scrollPane.setPreferredSize(new Dimension(width/8, (height/16*8)));
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            courseListPanel.add(scrollPane);
            courseListPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

            add(userPanel);add(courseListPanel); add(buttonsPanel);
            setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        }
    }
    //------------------Right Panel---------------------
    RightPanel rightPanel ;
    private class RightPanel extends JPanel {
        //------------------Course Name Panel---------------------
        void setCourseName(String courseName){
            //courseNamePanel = new CourseNamePanel(courseName);
        }
        private class CourseNamePanel extends JPanel {
            JLabel courseNameLabel = new JLabel("Course Name: ");

            public CourseNamePanel(/*String courseName*/){
                //courseNameLabel = new JLabel(courseNameLabel.getText() + courseName);
                add(courseNameLabel);
            }
        }
        //------------------Course Information Panel---------------------
        private class CourseInfoPanel extends JPanel {
            JPanel loPanel = new JPanel();
            JPanel studentPanel = new JPanel();
            JPanel questionPanel = new JPanel();
            JPanel examPanel = new JPanel();

            public CourseInfoPanel() {
                setLayout(new BorderLayout());

                loPanel.setLayout(new BoxLayout(loPanel, BoxLayout.Y_AXIS));
                loPanel.setBorder(BorderFactory.createTitledBorder("Learning Outcomes"));
                loPanel.add(new JLabel("LO1"));
                loPanel.add(new JLabel("LO2"));

                loPanel.setPreferredSize(new Dimension(getWidth(), 310));
                add(loPanel, BorderLayout.NORTH);

                studentPanel.setLayout(new BoxLayout(studentPanel, BoxLayout.Y_AXIS));
                studentPanel.setBorder(BorderFactory.createTitledBorder("Students"));
                studentPanel.add(new JLabel("Section 1: Hüs"));
                studentPanel.add(new JLabel("Section 2: Def"));

                studentPanel.setPreferredSize(new Dimension(300, getHeight()));
                add(studentPanel, BorderLayout.EAST);

                questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
                questionPanel.setBorder(BorderFactory.createTitledBorder("Questions"));
                questionPanel.add(new JLabel("Q1"));
                questionPanel.add(new JLabel("Q2"));

                examPanel.setLayout(new BoxLayout(examPanel, BoxLayout.Y_AXIS));
                examPanel.setBorder(BorderFactory.createTitledBorder("Exams"));
                examPanel.add(new JLabel("Exam 1"));
                examPanel.add(new JLabel("Exam 2"));

                JPanel leftColumn = new JPanel();
                leftColumn.setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.BOTH;

                gbc.weightx = 1.0;
                gbc.weighty = 0.5;
                gbc.gridx = 0;
                gbc.gridy = 0;
                leftColumn.add(questionPanel, gbc);

                gbc.weightx = 1.0;
                gbc.weighty = 0.5;
                gbc.gridx = 0;
                gbc.gridy = 1;
                leftColumn.add(examPanel, gbc);

                //they're sharing the remaining part 50-50

                add(leftColumn, BorderLayout.CENTER);
            }
        }

        public RightPanel(){
            setLayout(new BorderLayout());
            JLabel courseNameLabel = new JLabel();
            JPanel courseNamePanel = new CourseNamePanel();
            courseNamePanel.add(courseNameLabel);
            JPanel courseInfoPanel = new CourseInfoPanel();
            courseNamePanel.setPreferredSize(new Dimension(getWidth(),25));
            add(courseNamePanel, BorderLayout.NORTH);
            courseInfoPanel.setPreferredSize(new Dimension(getWidth(),690));
            add(courseInfoPanel, BorderLayout.CENTER);
        }
    }

    public Frame(){
        leftPanel = new LeftPanel();
        rightPanel = new RightPanel();
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        rightPanel.setVisible(false);

        setSize(width/4*3, height/4*3);
        setLocation(width/8,height/8);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
