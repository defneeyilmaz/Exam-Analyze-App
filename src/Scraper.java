import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class Scraper {
    private ArrayList<String> courseCodes;
    private final static String[] faculties = {"CE", "SE"};
    private final static String baseCourseURL = "https://se.ieu.edu.tr/en/syllabus/type/read/id/";
    private ArrayList<Course> scrapedCourses;

    public Scraper() {
        courseCodes = new ArrayList<>();
        scrapedCourses = new ArrayList<>();
        scrapeAll();
    }

    public ArrayList<Course> getScrapedCourses() {
        return scrapedCourses;
    }

    class Course {
        private String courseName;
        private String courseCode;
        private ArrayList<String> LOs;
        private ArrayList<String> evaluationCriteria;

        public Course() {
            LOs = new ArrayList<>();
            evaluationCriteria = new ArrayList<>();
        }

        public String getCourseName() {
            return courseName;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public ArrayList<String> getLOs() {
            return LOs;
        }

        public ArrayList<String> getEvaluationCriteria() {
            return evaluationCriteria;
        }

        @Override
        public String toString() {
            return "------------------------" + "\nCourse Code:" + courseCode + "\nCourse Name:" + courseName +
                    "\nCourse Learning Outcomes:" + LOs + "\nCourse Evaluation Criteria:" + evaluationCriteria;
        }
    }

    /**
     * Scrapes the website to find all course codes starts with CE or SE.
     */
    private void getAllFacultyCourse() {
        for (String faculty : faculties) {
            try {
                Document doc = Jsoup.connect("https://" + faculty.toLowerCase() + ".ieu.edu.tr/en/curr").get();
                Elements courses = doc.getElementsByClass("ders");
                for (Element temp : courses) {
                    if (!courseCodes.contains(temp.text()) && temp.text().matches("^(SE|CE)\\s\\d+")) {
                        courseCodes.add(temp.text());
                    }

                }
            } catch (IOException e) {
                System.err.println("No Internet Connection.");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        System.out.println(courseCodes);
    }

    /**
     * Scrapes course code, course name, course learning outcomes and course
     * evaluation criteria for all the courses listed above in courseCodes.
     */
    private void scrapeAll() {
        getAllFacultyCourse();
        for (String code : courseCodes) {
            try {
                scrapedCourses.add(scrape(code));
            } catch (IOException e) {
                System.err.println("No Internet Connection.");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Supporting function for scrapeAll()
     *
     * @param courseCode
     * @return Course object with its corresponding attributes.
     * @throws IOException
     * @throws NullPointerException
     */
    private Course scrape(String courseCode) throws IOException, NullPointerException {

        Course temp = new Course();
        System.out.println(courseCode);
        Document doc = Jsoup.connect(baseCourseURL + courseCode.replace(" ", "+")).get();
        if (doc != null) {
            temp.courseName = doc.selectFirst("#course_name").text();
            temp.courseCode = courseCode;
            int i = 0;
            while (doc.selectFirst("#li" + i) != null) {
                temp.LOs.add(doc.selectFirst("#li" + i).text());
                i++;
            }
            String participation = doc.selectFirst("#attendance_per").text();
            if (participation != null)
                if (!participation.isEmpty()) temp.evaluationCriteria.add("Participation: %" + participation);
            String labApp = doc.selectFirst("#lab_per").text();
            if (labApp != null)
                if (!labApp.isEmpty()) temp.evaluationCriteria.add("Laboratory / Application: %" + labApp);
            String fieldWork = doc.selectFirst("#fieldwork_per").text();
            if (fieldWork != null) if (!fieldWork.isEmpty()) temp.evaluationCriteria.add("Field Work: %" + fieldWork);
            String quiz = doc.selectFirst("#quiz_per").text();
            if (quiz != null) if (!quiz.isEmpty()) temp.evaluationCriteria.add("Quizzes / Studio Critiques: %" + quiz);
            String portfolio = doc.selectFirst("#portfolioMed_per").text();
            if (portfolio != null) if (!portfolio.isEmpty()) temp.evaluationCriteria.add("Portfolio: %" + portfolio);
            String hw = doc.selectFirst("#homework_per").text();
            if (hw != null) if (!hw.isEmpty()) temp.evaluationCriteria.add("Homework / Assignments: %" + hw);
            String presentation = doc.selectFirst("#presentation_per").text();
            if (presentation != null)
                if (!presentation.isEmpty()) temp.evaluationCriteria.add("Presentation / Jury: %" + presentation);
            String project = doc.selectFirst("#project_per").text();
            if (project != null) if (!project.isEmpty()) temp.evaluationCriteria.add("Project: %" + project);
            String seminar = doc.selectFirst("#seminar_per").text();
            if (seminar != null) if (!seminar.isEmpty()) temp.evaluationCriteria.add("Seminar / Workshop: %" + seminar);
            String orEx = doc.selectFirst("#seminar_per").text();
            if (orEx != null) if (!orEx.isEmpty()) temp.evaluationCriteria.add("Oral Exams: %" + orEx);
            String midterm = doc.selectFirst("#midterm_per").text();
            if (midterm != null) if (!midterm.isEmpty()) temp.evaluationCriteria.add("Midterm: %" + midterm);
            String final_ = doc.selectFirst("#final_per").text();
            if (final_ != null) if (!final_.isEmpty()) temp.evaluationCriteria.add("Final: %" + final_);
        }
        return temp;
    }

    public void print() {
        for (Course c : scrapedCourses) {
            System.out.println(c);
        }
    }

}
