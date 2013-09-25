package liquibase.diff.compare;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.structure.DatabaseObject;

import java.util.*;

public class DatabaseObjectComparatorChain implements Cloneable {
    private List<DatabaseObjectComparator> comparators;
    private int nextIndex = 0; //this class is used often enough that the overhead of an iterator adds up to a significant percentage of the execution time

    public DatabaseObjectComparatorChain(List<DatabaseObjectComparator> comparators) {
        this.comparators = comparators;
    }

    protected DatabaseObjectComparatorChain copy() {
        return new DatabaseObjectComparatorChain(comparators);
    }

    public boolean isSameObject(DatabaseObject object1, DatabaseObject object2, Database accordingTo) {
        if (object1 == null && object2 == null) {
            return true;
        }
        if (object1 == null && object2 != null) {
            return false;
        }

        if (object1 != null && object2 == null) {
            return false;
        }

        if (comparators == null) {
            return true;
        }

        DatabaseObjectComparator next = getNextComparator();

        if (next == null) {
            return true;
        }

        return next.isSameObject(object1, object2, accordingTo, this);
    }

    public String[] hash(DatabaseObject object, Database accordingTo) {
        if (object == null) {
            return null;
        }

        DatabaseObjectComparator next = getNextComparator();

        if (next == null) {
            return null;
        }

        return next.hash(object, accordingTo, this);
    }

    private DatabaseObjectComparator getNextComparator() {
        if (comparators == null) {
            return null;
        }

        if (nextIndex >= comparators.size()) {
            return null;
        }

        DatabaseObjectComparator next = comparators.get(nextIndex);
        nextIndex++;
        return next;
    }

    public ObjectDifferences findDifferences(DatabaseObject object1, DatabaseObject object2, Database accordingTo, CompareControl compareControl) {
        if (object1 == null && object2 == null) {
            return new ObjectDifferences(compareControl);
        }
        if (object1 == null && object2 != null) {
            return new ObjectDifferences(compareControl).addDifference("Reference value was null", "this", null, null);
        }

        if (object1 != null && object2 == null) {
            return new ObjectDifferences(compareControl).addDifference("Compared value was null", "this", null, null);
        }

        DatabaseObjectComparator next = getNextComparator();

        if (next == null) {
            return new ObjectDifferences(compareControl);
        }

        return next.findDifferences(object1, object2, accordingTo, compareControl, this);
    }
}
