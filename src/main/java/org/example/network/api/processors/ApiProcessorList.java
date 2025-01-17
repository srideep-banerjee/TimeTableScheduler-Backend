package org.example.network.api.processors;

import org.example.network.api.processors.config.ConfigGlobalApiProcessor;
import org.example.network.api.processors.file.*;
import org.example.network.api.processors.schedule.*;
import org.example.network.api.processors.student.SingleStudentApiProcessor;
import org.example.network.api.processors.student.StudentRollsApiProcessor;
import org.example.network.api.processors.student.StudentsApiProcessor;
import org.example.network.api.processors.subject.SingleSubjectApiProcessor;
import org.example.network.api.processors.subject.SubjectCodesApiProcessor;
import org.example.network.api.processors.subject.SubjectsApiProcessor;
import org.example.network.api.processors.teacher.SingleTeacherApiProcessor;
import org.example.network.api.processors.teacher.TeacherNamesApiProcessor;
import org.example.network.api.processors.teacher.TeachersApiProcessor;

import java.util.Arrays;
import java.util.List;

public class ApiProcessorList {
    public static List<ApiProcessor> getAvailableApiProcessors() {
        return Arrays.asList(
                //Teacher Apis
                new TeachersApiProcessor(),
                new TeacherNamesApiProcessor(),
                new SingleTeacherApiProcessor(),

                //Subject Apis
                new SubjectsApiProcessor(),
                new SubjectCodesApiProcessor(),
                new SingleSubjectApiProcessor(),

                //Student Apis
                new StudentsApiProcessor(),
                new StudentRollsApiProcessor(),
                new SingleStudentApiProcessor(),

                //Schedule Apis
                new ScheduleGeneratorApiProcessor(),
                new ScheduleSectionYearApiProcessor(),
                new ScheduleYearApiProcessor(),
                new ScheduleApiProcessor(),
                new ScheduleTeacherApiProcessor(),
                new ScheduleStructureApiProcessor(),

                //File Apis
                new FileNewApiProcessor(),
                new FileLoadApiProcessor(),
                new FileSaveApiProcessor(),
                new FileDeleteApiProcessor(),
                new FileCurrentNameApiProcessor(),
                new FileListApiProcessor(),
                new FileIsSavedApiProcessor(),

                //Config Apis
                new ConfigGlobalApiProcessor()
        );
    }
}
