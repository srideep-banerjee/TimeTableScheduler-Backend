package org.example.files.db;

import java.util.*;

public class CreateTableQueryBuilder {
    private boolean ifNotExist = true;
    private final String name;
    private final HashSet<String> keyNames;
    private final ArrayList<KeyEntry> keys;
    private String[] primaryKeys = null;
    private final ArrayList<ForeignKey> foreignKeys;

    public CreateTableQueryBuilder(String name) {
        this.name = name;
        keyNames = new HashSet<>();
        keys = new ArrayList<>();
        foreignKeys = new ArrayList<>();
    }

    public CreateTableQueryBuilder ifNotExist(boolean ifNotExist) {
        this.ifNotExist = ifNotExist;
        return this;
    }

    public CreateTableQueryBuilder addKey(KeyEntry keyEntry) {
        if (keyNames.contains(keyEntry.name)) {
            throw new RuntimeException("Key "+keyEntry.name+" already added.");
        } else {
            keyNames.add(keyEntry.name);
        }
        keys.add(keyEntry);
        return this;
    }

    public CreateTableQueryBuilder addKey(String name, String type) {
        return addKey(new KeyEntry(name, type));
    }

    public CreateTableQueryBuilder addKey(String name, String type, boolean primary) {
        return addKey(new KeyEntry(name, type).primary(primary));
    }

    public RuntimeException validateForeignKey(ForeignKey foreignKey) {
        for (String key: foreignKey.keySet()) {
            if (!keyNames.contains(key)) {
                return new RuntimeException("Can't set non existent key: " + key + " as foreign key.");
            }
        }
        return null;
    }

    public CreateTableQueryBuilder primaryKeys(String first, String ...others) {
        String[] primaryKeys = new String[others.length + 1];
        int index = 0;
        if (!keyNames.contains(first)) {
            throw new RuntimeException("Can't set non existent key: " + first + " as primary key.");
        }
        primaryKeys[index++] = first;
        for (String key : others) {
            if (!keyNames.contains(key)) {
                throw new RuntimeException("Can't set non existent key: " + key + " as primary key.");
            }
            primaryKeys[index++] = key;
        }
        this.primaryKeys = primaryKeys;
        return this;
    }

    public CreateTableQueryBuilder addForeignKey(ForeignKey foreignKey) {
        RuntimeException ex = validateForeignKey(foreignKey);
        if (ex != null) {
            throw ex;
        }
        foreignKeys.add(foreignKey);
        return this;
    }

    public String build() {
        if (keys.isEmpty()) {
            throw new RuntimeException("Can't create table with no keys.");
        }
        StringBuilder stringBuilder = new StringBuilder("CREATE TABLE ");
        if (ifNotExist) {
            stringBuilder
                    .append("IF NOT EXISTS ");
        }
        stringBuilder.append(name);
        stringBuilder.append('(');
        Iterator<KeyEntry> keyIterator = keys.iterator();
        stringBuilder.append(keyIterator.next().toString());
        keyIterator.forEachRemaining((keyEntry -> stringBuilder
                .append(", ")
                .append(keyEntry.toString())));
        if (primaryKeys != null) {
            stringBuilder.append(", PRIMARY KEY (");
            stringBuilder.append(String.join(", ",primaryKeys));
            stringBuilder.append(")");
        }
        for (ForeignKey foreignKey: foreignKeys) {
            stringBuilder
                    .append(", ")
                    .append(foreignKey.toString());
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public static class KeyEntry {
        private final String name;
        private final String type;
        private boolean primary = false;
        private boolean unique = false;
        private boolean notNull = true;

        public KeyEntry(String name, String type) {
            this.name = name;
            this.type = type.toUpperCase();
        }

        public KeyEntry primary(boolean primary) {
            this.primary = primary;
            return this;
        }

        public KeyEntry unique(boolean unique) {
            this.unique = unique;
            return this;
        }

        public KeyEntry notNull(boolean notNull) {
            this.notNull = notNull;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder(name)
                    .append(' ')
                    .append(type);
            if (primary) {
                stringBuilder
                        .append(' ')
                        .append("PRIMARY KEY");
            }
            if (unique) {
                stringBuilder
                        .append(' ')
                        .append("UNIQUE");
            }
            if (notNull) {
                stringBuilder
                        .append(' ')
                        .append("NOT NULL");
            }
            return stringBuilder.toString();
        }
    }

    public static class ForeignKey {
        private final String referenceTableName;
        HashMap<String, String> referenceKeyMap;
        private final boolean cascadingDelete;

        public ForeignKey(String referenceTableName) {
            this(referenceTableName, true);
        }

        public ForeignKey(String referenceTableName, boolean cascadingDelete) {
            this.referenceTableName = referenceTableName;
            referenceKeyMap = new HashMap<>();
            this.cascadingDelete = cascadingDelete;
        }

        public Set<String> keySet() {
            return referenceKeyMap.keySet();
        }

        public ForeignKey addReference(String key) {
            return addReference(key, key);
        }

        // key is the name of the key in this table,
        // referencedKey is the name of the key in referenced table
        public ForeignKey addReference(String key, String referencedKey) {
            referenceKeyMap.put(key, referencedKey);
            return this;
        }

        @Override
        public String toString() {
            if (referenceKeyMap.isEmpty()) {
                return "";
            }
            StringBuilder stringBuilder = new StringBuilder();
            var iterator = referenceKeyMap.entrySet().iterator();
            HashMap.Entry<String, String> entry = iterator.next();
            StringBuilder referenceKeys = new StringBuilder(entry.getValue());
            StringBuilder currentKeys = new StringBuilder(entry.getKey());
            while (iterator.hasNext()) {
                entry = iterator.next();
                referenceKeys.append(", ").append(entry.getValue());
                currentKeys.append(", ").append(entry.getKey());
            }
            stringBuilder
                    .append("FOREIGN KEY (")
                    .append(currentKeys)
                    .append(") REFERENCES ")
                    .append(referenceTableName)
                    .append("(")
                    .append(referenceKeys)
                    .append(")");
            if (cascadingDelete) {
                stringBuilder.append(" ON DELETE CASCADE");
            }
            return stringBuilder.toString();
        }
    }
}