package com.honstain.practicetracker.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.honstain.practicetracker.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PracticeSessionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(PracticeSession.class);
        PracticeSession practiceSession1 = new PracticeSession();
        practiceSession1.setId(1L);
        PracticeSession practiceSession2 = new PracticeSession();
        practiceSession2.setId(practiceSession1.getId());
        assertThat(practiceSession1).isEqualTo(practiceSession2);
        practiceSession2.setId(2L);
        assertThat(practiceSession1).isNotEqualTo(practiceSession2);
        practiceSession1.setId(null);
        assertThat(practiceSession1).isNotEqualTo(practiceSession2);
    }
}
