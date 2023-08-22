package org.cucumber.easyreport.assertions;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.assertj.core.api.SoftAssertionError;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.error.AssertJMultipleFailuresError;
import org.cucumber.easyreport.exception.EasyReportException;
import org.cucumber.easyreport.util.VersionHelper;
import org.cucumber.easyreport.util.dateutils.DateUtils;

import java.util.*;

@Slf4j
public class Assertions implements Assertion {

    private static final Set<String> knownFailureLabelSet = new HashSet<>();

    public final ThreadLocal<Map<String, Map<String, String>>> failures = ThreadLocal.withInitial(HashMap::new);

    private final ThreadLocal<SoftAssertions> assertions = ThreadLocal.withInitial(SoftAssertions::new);

    public Assertions() {}

    public Assertions addKnownFailureLabels(String... labels) {
        knownFailureLabelSet.addAll(List.of(labels));
        return this;
    }

    public static Set<String> getKnownFailureLabels() {
        return knownFailureLabelSet;
    }

    private Assertions assertCurrentStep(String label, SoftAssertions assertion) {
        try {
            assertion.assertAll();
        } catch (AssertJMultipleFailuresError e) {
            String failure = e.getFailures().stream().map(Throwable::getMessage).toList()
                    .get(0).replaceAll("\n", " ")
                    .replaceAll("\r", " ")
                    .replaceAll("\"", "'");
            this.addIntoFailures(label, failure);
        } catch (SoftAssertionError error) {
            String failure = error.getErrors()
                    .get(0).replaceAll("\r", " ")
                    .replaceAll("\n", " ")
                    .replaceAll("\"", "'");
            this.addIntoFailures(label, failure);
        } finally {
            assertions.set(new SoftAssertions());
        }
        return this;
    }

    private void addIntoFailures(String label, String failure) {
        if(getKnownFailureLabels().contains(label)) {
            if(failures.get().containsKey("knownFailures"))
                failures.get().get("knownFailures").put(label, failure);
            else {
                Map<String, String> f = new HashMap<>();
                f.put(label, failure);
                failures.get().put("knownFailures", f);
            }
        } else {
            if(failures.get().containsKey("failures"))
                failures.get().get("failures").put(label, failure);
            else {
                Map<String, String> f = new HashMap<>();
                f.put(label, failure);
                failures.get().put("failures", f);
            }
        }
    }

    /**
     * Verifies that the actual value is equal to given value.
     *
     * @param label       name of the field of assertion
     * @param actual      actual value to compare
     * @param expected    expected value to compare against
     * @param failureMsg  failure message to include in assertion exception
     * @param passMessage success message to display on console or log for debug
     * @return @{@code Assertions}
     */
    @SneakyThrows
    @Override
    public Assertions assertEqualsTo(String label, Object actual, Object expected, String failureMsg, String passMessage) {
        String actualValue = Objects.nonNull(actual) ? getLabelValue(actual.toString()) : StringUtils.EMPTY;
        String expectedValue = Objects.nonNull(expected) ? getLabelValue(expected.toString()) : StringUtils.EMPTY;

        if (NumberUtils.isCreatable(actualValue) && NumberUtils.isCreatable(expectedValue)) {
            if (VersionHelper.compare(NumberUtils.createDouble(actualValue).toString(), NumberUtils.createDouble(expectedValue).toString()) != 0)
                assertions.get().assertThat(actual).as(failureMsg).isEqualTo(expected);
            else
                log.info(passMessage);
        } else if (!actualValue.equals(expectedValue)) {
            assertions.get().assertThat(actual).as(failureMsg).isEqualTo(expected);
        } else {
            log.info(passMessage);
        }

        return assertCurrentStep(label, assertions.get());
    }

    /**
     * Verifies that the actual value is not equal to given value.
     *
     * @param label       name of the field of assertion
     * @param actual      actual value to compare
     * @param expected    expected value to compare against
     * @param failureMsg  failure message to include in assertion exception
     * @param passMessage success message to display on console or log for debug
     * @return @{@code Assertions}
     */
    @Override
    public Assertions assertNotEqualsTo(String label, Object actual, Object expected, String failureMsg, String passMessage) {
        String actualValue = Objects.nonNull(actual) ? getLabelValue(actual.toString()) : StringUtils.EMPTY;
        String expectedValue = Objects.nonNull(expected) ? getLabelValue(expected.toString()) : StringUtils.EMPTY;

        if (NumberUtils.isCreatable(actualValue) && NumberUtils.isCreatable(expectedValue)) {
            if (VersionHelper.compare(NumberUtils.createDouble(actualValue).toString(), NumberUtils.createDouble(expectedValue).toString()) == 0)
                assertions.get().assertThat(actual).as(failureMsg).isNotEqualTo(expected);
            else
                log.info(passMessage);
        } else if (actualValue.equals(expectedValue)) {
            assertions.get().assertThat(actual).as(failureMsg).isNotEqualTo(expected);
        } else {
            log.info(passMessage);
        }

        return assertCurrentStep(label, assertions.get());
    }

    /**
     * Verifies that the actual value greater than given value
     *
     * @param label       name of the field of assertion
     * @param actual      actual value to compare
     * @param expected    expected value to compare against
     * @param failureMsg  failure message to include in assertion exception
     * @param passMessage success message to display on console or log for debug
     * @return @{@code Assertions}
     */
    @SneakyThrows
    @Override
    public Assertions assertGreaterThan(String label, Object actual, Object expected, String failureMsg, String passMessage) {
        String actualValue = Objects.nonNull(actual) ? getLabelValue(actual.toString()) : StringUtils.EMPTY;
        String expectedValue = Objects.nonNull(expected) ? getLabelValue(expected.toString()) : StringUtils.EMPTY;

        if (NumberUtils.isCreatable(actualValue) && NumberUtils.isCreatable(expectedValue)) {
            Double actualNumber = NumberUtils.createDouble(actualValue);
            Double expectedNumber = NumberUtils.createDouble(expectedValue);
            if (VersionHelper.compare(actualNumber.toString(), expectedNumber.toString()) != 1) // version helper returns 1 if greater, else returns 0 or -1
                assertions.get().assertThat(actualNumber).as(failureMsg).isGreaterThan(expectedNumber);
            else
                log.info(passMessage);
        } else {
            throw new EasyReportException("Provided arguments are not number to perform 'GreaterThan' check");
        }

        return assertCurrentStep(label, assertions.get());
    }

    /**
     * Verifies that the actual value is less than given value
     *
     * @param label       name of the field of assertion
     * @param actual      actual value to compare
     * @param expected    expected value to compare against
     * @param failureMsg  failure message to include in assertion exception
     * @param passMessage success message to display on console or log for debug
     * @return @{@code Assertions}
     */
    @Override
    public Assertions assertLesserThan(String label, Object actual, Object expected, String failureMsg, String passMessage) {
        String actualValue = Objects.nonNull(actual) ? getLabelValue(actual.toString()) : StringUtils.EMPTY;
        String expectedValue = Objects.nonNull(expected) ? getLabelValue(expected.toString()) : StringUtils.EMPTY;

        if (NumberUtils.isCreatable(actualValue) && NumberUtils.isCreatable(expectedValue)) {
            Double actualNumber = NumberUtils.createDouble(actualValue);
            Double expectedNumber = NumberUtils.createDouble(expectedValue);
            if (VersionHelper.compare(actualNumber.toString(), expectedNumber.toString()) != -1) // version helper returns -1 if lesser, else returns 0 or 1
                assertions.get().assertThat(actualNumber).as(failureMsg).isLessThan(expectedNumber);
            else
                log.info(passMessage);
        } else {
            throw new EasyReportException("Provided arguments are not number to perform 'GreaterThan' check");
        }

        return assertCurrentStep(label, assertions.get());
    }

    /**
     * Fails the assertion with given failure message
     *
     * @param label      name of the field of assertion
     * @param failureMsg failure message to include in assertion exception
     * @return @{@code Assertions}
     */
    @Override
    public Assertions assertFail(String label, String failureMsg) {
        assertions.get().fail(failureMsg);
        return assertCurrentStep(label, assertions.get());
    }

    /**
     * Checks if given boolean is true
     *
     * @param label      name of the field of assertion
     * @param failureMsg failure message to include in assertion exception
     * @return @{@code Assertions}
     */
    @SneakyThrows
    @Override
    public Assertions isTrue(String label, boolean actual, String failureMsg) {
        assertions.get().assertThat(actual).as(failureMsg).isTrue();
        return assertCurrentStep(label, assertions.get());
    }

    /**
     * Checks if given boolean is false
     *
     * @param label      name of the field of assertion
     * @param failureMsg failure message to include in assertion exception
     * @return @{@code Assertions}
     */
    @Override
    public Assertions isFalse(String label, boolean actual, String failureMsg) {
        assertions.get().assertThat(actual).as(failureMsg).isFalse();
        return assertCurrentStep(label, assertions.get());
    }


    @SneakyThrows
    public void assertAll() {
        try {
            if (!failures.get().isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(failures.get());
                throw new SoftAssertionError(List.of(json));
            }
        } finally {
            failures.get().clear();
            knownFailureLabelSet.clear();
        }
    }

    private static String getLabelValue(String value) {
        try {
            if (value.length() >= 10 && DateUtils.isDateValue(value.substring(0, 10)))
                return value.substring(0, 10);

            else
                return value;
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            throw new EasyReportException("Exception while getting value for SoftAssertion");
        }
    }

}
