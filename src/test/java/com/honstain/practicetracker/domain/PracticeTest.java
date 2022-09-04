package com.honstain.practicetracker.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.honstain.practicetracker.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PracticeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Practice.class);
        Practice practice1 = new Practice();
        practice1.setId(1L);
        Practice practice2 = new Practice();
        practice2.setId(practice1.getId());
        assertThat(practice1).isEqualTo(practice2);
        practice2.setId(2L);
        assertThat(practice1).isNotEqualTo(practice2);
        practice1.setId(null);
        assertThat(practice1).isNotEqualTo(practice2);
    }
}
