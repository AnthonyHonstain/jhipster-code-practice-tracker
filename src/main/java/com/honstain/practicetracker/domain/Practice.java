package com.honstain.practicetracker.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.honstain.practicetracker.domain.enumeration.PracticeResult;
import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Practice.
 */
@Table("practice")
public class Practice implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("problem_name")
    private String problemName;

    @NotNull(message = "must not be null")
    @Column("problem_link")
    private String problemLink;

    @Column("start")
    private ZonedDateTime start;

    @Column("jhi_end")
    private ZonedDateTime end;

    @Column("result")
    private PracticeResult result;

    @Transient
    @JsonIgnoreProperties(value = { "practices" }, allowSetters = true)
    private PracticeSession practiceSession;

    @Column("practice_session_id")
    private Long practiceSessionId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Practice id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProblemName() {
        return this.problemName;
    }

    public Practice problemName(String problemName) {
        this.setProblemName(problemName);
        return this;
    }

    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    public String getProblemLink() {
        return this.problemLink;
    }

    public Practice problemLink(String problemLink) {
        this.setProblemLink(problemLink);
        return this;
    }

    public void setProblemLink(String problemLink) {
        this.problemLink = problemLink;
    }

    public ZonedDateTime getStart() {
        return this.start;
    }

    public Practice start(ZonedDateTime start) {
        this.setStart(start);
        return this;
    }

    public void setStart(ZonedDateTime start) {
        this.start = start;
    }

    public ZonedDateTime getEnd() {
        return this.end;
    }

    public Practice end(ZonedDateTime end) {
        this.setEnd(end);
        return this;
    }

    public void setEnd(ZonedDateTime end) {
        this.end = end;
    }

    public PracticeResult getResult() {
        return this.result;
    }

    public Practice result(PracticeResult result) {
        this.setResult(result);
        return this;
    }

    public void setResult(PracticeResult result) {
        this.result = result;
    }

    public PracticeSession getPracticeSession() {
        return this.practiceSession;
    }

    public void setPracticeSession(PracticeSession practiceSession) {
        this.practiceSession = practiceSession;
        this.practiceSessionId = practiceSession != null ? practiceSession.getId() : null;
    }

    public Practice practiceSession(PracticeSession practiceSession) {
        this.setPracticeSession(practiceSession);
        return this;
    }

    public Long getPracticeSessionId() {
        return this.practiceSessionId;
    }

    public void setPracticeSessionId(Long practiceSession) {
        this.practiceSessionId = practiceSession;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Practice)) {
            return false;
        }
        return id != null && id.equals(((Practice) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Practice{" +
            "id=" + getId() +
            ", problemName='" + getProblemName() + "'" +
            ", problemLink='" + getProblemLink() + "'" +
            ", start='" + getStart() + "'" +
            ", end='" + getEnd() + "'" +
            ", result='" + getResult() + "'" +
            "}";
    }
}
