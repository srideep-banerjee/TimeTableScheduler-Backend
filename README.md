A Java  + HTML, CSS and Javascript application to find optimal time schedule of lectures, maintaining the constraints of professor's availability. The application consists of three codebases:-
1. A java based web server(backend) that contains the genetic time scheduling algorithm,
2. UI(frontend) written in HTML, CSS and Javascript that displays the time table and communicates using REST API endpoints,<br>Source repository: <a href="https://github.com/Super7000/Sem_Time_Table_Designer">https://github.com/Super7000/Sem_Time_Table_Designer</h>,
3. A CEF based custom browser written in java to display the UI without browser restrictions,<br>Source repository: <a href="https://github.com/srideep-banerjee/TTSBrowserComponent">https://github.com/srideep-banerjee/TTSBrowserComponent</h>.


# How to set up the project (without any released version of this app)?

**Step 1:**
First Fork this repositories:<br/>
[Time-Table-Creator-ReactJS (UI)](https://github.com/Super7000/Time-Table-Creator-ReactJS) <br/>
[TTSBrowserComponent (Browser Component)](https://github.com/srideep-banerjee/TTSBrowserComponent) <br/>
[TimeTableScheduler-Backend (Bankend Java Code)](https://github.com/srideep-banerjee/TimeTableScheduler-Backend) <br/>

**Step 2:**
Then use Intellij IDEA or any other framework to build a jar file of the TTSBrowserComponent.

**Step 3:**
Then copy the jar file of TTSBrowserComponent to Forked TimeTableScheduler-Backend's main directory.

![image](https://github.com/Super7000/Time-Table-Creator-ReactJS/assets/86580414/ceab5a00-2620-4746-bdb9-92ac73caa114)

**Step 4:**
Now open forked Time-Table-Creator-ReactJS and build the product using command like `npm run build`

**Step 5:**
Then Copy the build product to TimeTableScheduler-Backend's `web` directory. **(If web directory is not present in TimeTableScheduler-Backend then create one)**

**Step 6:**
Now you just need to run the TimeTableScheduler-Backend's `main.java` file using Intellij IDEA or any code editor or framework.

# These are the Constraints that are used in our AI system to generate the time table:
**Hard (Mandatory) Constraints include :**
1. One practical subject class per week
2. All teachers alloted within availability
3. Subjects alloted their required no. of lectures in all sections
4. Practical subjects not allocated same room at same time
5. Two teachers not teaching a semester-section same subject (theory only)
6. Two teachers/subject not at a semester-section at same time (theory only)
7. Teachers only teaching one semster-section at a time
8. Practical subjects assigned consecutive periods
9. Practical teachers must be available during all practical subjects
10. All teachers assigned atleast one subject in a section class
11. All teachers get assigned subjects known to them
12. Theory teachers also teaching practical
13. Teachers count for practical class equals lecture count

**Soft (Preffered) Constraints include :**
1. Practical Lab in end of second half
