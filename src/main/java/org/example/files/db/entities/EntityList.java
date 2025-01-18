package org.example.files.db.entities;

import org.example.files.db.entities.implementation.*;

import java.util.Arrays;
import java.util.List;

public class EntityList {

    /**
     * Returns all available entities in the order of their dependency from
     * most independent to most dependent.
     */
    public static List<Entity> getEntityList() {
        return Arrays.asList(
                new ConfigEntity(),
                new ScheduleStructureEntity(),
                new SubjectEntity(),
                new TeacherEntity(),
                new ScheduleSolutionEntity()
        );
    }
}
