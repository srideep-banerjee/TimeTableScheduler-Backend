package org.example.network.api.processors;

import org.example.network.api.processors.config.ConfigGlobalApiProcessor;
import org.example.network.api.processors.file.*;
import org.example.network.api.processors.schedule.*;
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
                new TeachersApiProcessor(),
                new TeacherNamesApiProcessor(),
                new SingleTeacherApiProcessor(),
                new SubjectsApiProcessor(),
                new SubjectCodesApiProcessor(),
                new SingleSubjectApiProcessor(),
                new ScheduleGeneratorApiProcessor(),
                new ScheduleSectionYearApiProcessor(),
                new ScheduleYearApiProcessor(),
                new ScheduleApiProcessor(),
                new ScheduleTeacherApiProcessor(),
                new ScheduleStructureApiProcessor(),
                new FileNewApiProcessor(),
                new FileLoadApiProcessor(),
                new FileSaveApiProcessor(),
                new FileDeleteApiProcessor(),
                new FileCurrentNameApiProcessor(),
                new FileListApiProcessor(),
                new FileIsSavedApiProcessor(),
                new ConfigGlobalApiProcessor()
        );
    }
}
