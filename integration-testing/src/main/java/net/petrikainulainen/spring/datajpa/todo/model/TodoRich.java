package net.petrikainulainen.spring.datajpa.todo.model;

import javax.persistence.*;

/**
 * @author Siarhei Shchahratsou <s.siarhei@gmail.com>
 * @since 07.12.2014
 */
@Entity
public class TodoRich extends Todo {
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "creator"))
    })
    private UserRef createdRef;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "updater"))
    })
    private UserRef updatedRef;

    public UserRef getCreatedRef() {
        if (createdRef == null) {
            createdRef = new UserRef();
        }
        return createdRef;
    }

    public void setCreatedRef(UserRef createdRef) {
        this.createdRef = createdRef;
    }

    public UserRef getUpdatedRef() {
        if (updatedRef == null) {
            updatedRef = new UserRef();
        }
        return updatedRef;
    }

    public void setUpdatedRef(UserRef updatedRef) {
        this.updatedRef = updatedRef;
    }
}
