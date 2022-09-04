package com.honstain.practicetracker.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A PracticeSession.
 */
@Table("practice_session")
public class PracticeSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Column("start")
    private ZonedDateTime start;

    @Column("jhi_end")
    private ZonedDateTime end;

    @Transient
    @JsonIgnoreProperties(value = { "practiceSession" }, allowSetters = true)
    private Set<Practice> practices = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public PracticeSession id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getStart() {
        return this.start;
    }

    public PracticeSession start(ZonedDateTime start) {
        this.setStart(start);
        return this;
    }

    public void setStart(ZonedDateTime start) {
        this.start = start;
    }

    public ZonedDateTime getEnd() {
        return this.end;
    }

    public PracticeSession end(ZonedDateTime end) {
        this.setEnd(end);
        return this;
    }

    public void setEnd(ZonedDateTime end) {
        this.end = end;
    }

    public Set<Practice> getPractices() {
        return this.practices;
    }

    public void setPractices(Set<Practice> practices) {
        if (this.practices != null) {
            this.practices.forEach(i -> i.setPracticeSession(null));
        }
        if (practices != null) {
            practices.forEach(i -> i.setPracticeSession(this));
        }
        this.practices = practices;
    }

    public PracticeSession practices(Set<Practice> practices) {
        this.setPractices(practices);
        return this;
    }

    public PracticeSession addPractice(Practice practice) {
        this.practices.add(practice);
        practice.setPracticeSession(this);
        return this;
    }

    public PracticeSession removePractice(Practice practice) {
        this.practices.remove(practice);
        practice.setPracticeSession(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PracticeSession)) {
            return false;
        }
        return id != null && id.equals(((PracticeSession) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PracticeSession{" +
            "id=" + getId() +
            ", start='" + getStart() + "'" +
            ", end='" + getEnd() + "'" +
            "}";
    }
}
