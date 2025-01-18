package org.example.files.db.entities;

import org.example.files.db.CreateTableQueryBuilder;

public class CommonData {
    public static CreateTableQueryBuilder.ForeignKey subjectCodeForeignKey = new CreateTableQueryBuilder.ForeignKey("subjects")
            .addReference("subject_code");
    public static CreateTableQueryBuilder.ForeignKey teacherNameForeignKey = new CreateTableQueryBuilder.ForeignKey("teachers")
            .addReference("teacher_name");
}
