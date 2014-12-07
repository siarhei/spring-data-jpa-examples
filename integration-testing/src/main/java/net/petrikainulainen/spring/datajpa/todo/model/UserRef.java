package net.petrikainulainen.spring.datajpa.todo.model;

import javax.persistence.Embeddable;

/**
 * @author Siarhei Shchahratsou <s.siarhei@gmail.com>
 * @since 07.12.2014
 */
@Embeddable
public class UserRef {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
