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
            userPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

            buttonsPanel.setPreferredSize(new Dimension(width/8, height/16));
            buttonsPanel.add(addButton);
            buttonsPanel.add(removeButton);
            buttonsPanel.setLayout(new GridLayout(0,2));
            buttonsPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
            //temporary array, like get(department.courses)
            String[] SECourses = new String[]{"SE323", "SE321", "SE311", "SE375", "SE216"};

            list.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent event) {
                    //showing course information
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
            courseListPanel.add(scrollPane);
            courseListPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

            add(userPanel);add(courseListPanel); add(buttonsPanel);
            setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        }
    }
    //------------------Course Panel---------------------
    CoursePanel coursePanel ;
    private class CoursePanel extends JPanel{
        JPanel infoPanel = new JPanel();
        JLabel label = new JLabel("Course Page");

        public CoursePanel(){
            add(infoPanel);
            infoPanel.add(label);
            setVisible(true);
        }
    }


    public Frame(){
        leftPanel = new LeftPanel();
        coursePanel = new CoursePanel();
        add(leftPanel, BorderLayout.WEST);
        add(coursePanel, BorderLayout.CENTER);


        setSize(width/4*3, height/4*3);
        setLocation(width/8,height/8);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
