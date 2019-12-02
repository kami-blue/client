package team.stiff.pomelo.handler;

/**
 * Designates the order within a listener of event distribution.  This
 * is not globally sorted as the current structure of stored event listeners is
 * too complex to properly sort without major code refactoring.
 *
 * todo: hint hint...
 */
public enum ListenerPriority {
    LOWEST(-750),
    LOWER(-500),
    LOW(-250),
    NORMAL(0),
    HIGH(250),
    HIGHER(500),
    HIGHEST(750);

    private final int priorityLevel;

    ListenerPriority(final int priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public int getPriorityLevel() {
        return priorityLevel;
    }
}
