package net.petrikainulainen.spring.datajpa.todo.model;

import javax.persistence.*;

/**
 * @author Petri Kainulainen
 */
@Entity
@Table(name="todos")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
/*@OptimisticLocking(type = OptimisticLockType.DIRTY)
@DynamicUpdate*/
public class Todo {

    public static final int MAX_LENGTH_DESCRIPTION = 500;
    public static final int MAX_LENGTH_TITLE = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "description", nullable = true, length = MAX_LENGTH_DESCRIPTION)
    private String description;

    @Column(name = "title", nullable = false, length = MAX_LENGTH_TITLE)
    private String title;

    /*@Version
    private long version;*/

    public Todo() {

    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    /*public long getVersion() {
        return version;
    }*/

}
